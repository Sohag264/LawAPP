package com.example.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.FavoriteEntity
import com.example.data.entity.LawEntity
import com.example.data.entity.LawVerificationReportEntity
import com.example.data.entity.PersonalNoteEntity
import com.example.data.entity.ReadingHistoryEntity
import com.example.data.entity.VerificationStatus
import com.example.data.importer.BdlawsHtmlParser
import com.example.data.importer.LawRecoveryManager
import com.example.data.importer.LegalTextParser
import com.example.data.importer.ParsedChapter
import com.example.data.importer.ParsedLawPackage
import com.example.data.importer.ParsedSection
import com.example.data.importer.PdfTextExtractor
import com.example.data.importer.RecoveryImportResult
import com.example.data.repository.DatabaseStats
import com.example.data.repository.IntegrityReport
import com.example.data.repository.LawRepository
import com.example.data.repository.SearchResult
import com.example.data.verification.GlobalDataQualitySummary
import com.example.utils.BanglaNumberUtils
import com.example.utils.HashUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class LawViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = LawRepository(
        database.lawDao(),
        database.sectionDao(),
        database.userDataDao(),
        database.verificationDao()
    )
    private val recoveryManager = LawRecoveryManager(application, database, repository)

    // Data lists
    val allLaws: StateFlow<List<LawEntity>> = repository.allLaws.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val popularLaws: StateFlow<List<LawEntity>> = repository.popularLaws.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allBookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFavorites: StateFlow<List<FavoriteEntity>> = repository.allFavorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allNotes: StateFlow<List<PersonalNoteEntity>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val readingHistory: StateFlow<List<ReadingHistoryEntity>> = repository.readingHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stats: StateFlow<DatabaseStats> = repository.getDatabaseStats().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DatabaseStats(0, 0, 0, 0, 0, 0)
    )

    val dataQualitySummary: StateFlow<GlobalDataQualitySummary> = repository.getGlobalDataQualitySummary().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GlobalDataQualitySummary(0, 0, 0, 0, 0, 0, 0, 0, 0, null)
    )

    val allVerificationReports: StateFlow<List<LawVerificationReportEntity>> = repository.allVerificationReports.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResult: StateFlow<SearchResult> = _searchQuery
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.trim().isEmpty()) {
                flowOf(SearchResult("", emptyList(), emptyList()))
            } else {
                repository.search(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchResult("", emptyList(), emptyList())
        )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // Selected Law for Reader
    private val _selectedLawId = MutableStateFlow<Long?>(null)
    val selectedLawId: StateFlow<Long?> = _selectedLawId.asStateFlow()

    fun selectLaw(lawId: Long) {
        _selectedLawId.value = lawId
    }

    // Reader UI customizations
    private val _readerTextScale = MutableStateFlow(1.0f)
    val readerTextScale: StateFlow<Float> = _readerTextScale.asStateFlow()

    fun increaseFontSize() {
        if (_readerTextScale.value < 1.35f) {
            _readerTextScale.value += 0.1f
        }
    }

    fun decreaseFontSize() {
        if (_readerTextScale.value > 0.85f) {
            _readerTextScale.value -= 0.1f
        }
    }

    // Category Filter in AllLaws screen (uses real SQLite CrossRef join)
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    val filteredLawsByCategory: StateFlow<List<LawEntity>> = _selectedCategoryId
        .flatMapLatest { catId ->
            if (catId == null) repository.allLaws else repository.getLawsByCategory(catId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    // User actions
    fun toggleBookmark(lawId: Long, sectionId: Long, currentlyBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(lawId, sectionId, currentlyBookmarked)
        }
    }

    fun toggleLawFavorite(lawId: Long, currentlyFav: Boolean) {
        viewModelScope.launch {
            repository.toggleLawFavorite(lawId, currentlyFav)
        }
    }

    fun toggleSectionFavorite(lawId: Long, sectionId: Long, currentlyFav: Boolean) {
        viewModelScope.launch {
            repository.toggleSectionFavorite(lawId, sectionId, currentlyFav)
        }
    }

    fun recordHistory(lawId: Long, sectionId: Long) {
        viewModelScope.launch {
            repository.recordReadingHistory(lawId, sectionId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun saveSectionExplanation(sectionId: Long, newExplanation: String) {
        viewModelScope.launch {
            repository.updateSectionExplanation(sectionId, newExplanation)
        }
    }

    fun savePersonalNote(sectionId: Long, lawId: Long, noteText: String) {
        viewModelScope.launch {
            repository.savePersonalNote(sectionId, lawId, noteText)
        }
    }

    fun deletePersonalNote(sectionId: Long) {
        viewModelScope.launch {
            repository.deletePersonalNote(sectionId)
        }
    }

    // Verification & Recovery state
    private val _integrityReport = MutableStateFlow<IntegrityReport?>(null)
    val integrityReport: StateFlow<IntegrityReport?> = _integrityReport.asStateFlow()

    private val _importResult = MutableStateFlow<RecoveryImportResult?>(null)
    val importResult: StateFlow<RecoveryImportResult?> = _importResult.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    fun verifyDatabaseIntegrity() {
        viewModelScope.launch {
            _integrityReport.value = withContext(Dispatchers.IO) {
                repository.verifyDatabaseIntegrity()
            }
        }
    }

    fun clearIntegrityReport() {
        _integrityReport.value = null
    }

    fun clearImportResult() {
        _importResult.value = null
    }

    /**
     * Imports an official HTML, JSON, or PDF document chosen by the user from device storage.
     * Runs strictly on Dispatchers.IO inside a rollback-safe transaction after backing up user data.
     */
    fun importOfficialDocumentUri(uri: Uri, targetLawId: Long? = null) {
        viewModelScope.launch {
            _isImporting.value = true
            val result = withContext(Dispatchers.IO) {
                try {
                    val resolver = getApplication<Application>().contentResolver
                    val mimeType = resolver.getType(uri) ?: ""
                    val fileName = uri.lastPathSegment?.lowercase() ?: ""

                    if (mimeType.contains("pdf") || fileName.endsWith(".pdf")) {
                        val pdfResult = resolver.openInputStream(uri)?.use { stream ->
                            PdfTextExtractor.extractTextFromPdfStream(stream)
                        }
                        if (pdfResult == null || pdfResult.extractedText.isBlank()) {
                            RecoveryImportResult(
                                success = false,
                                lawId = targetLawId,
                                backupFilePath = null,
                                comparison = null,
                                verificationReport = null,
                                message = pdfResult?.warningMessage ?: "PDF ফাইল পড়া সম্ভব হয়নি।"
                            )
                        } else {
                            // Convert extracted text into structured sections if detectable
                            val htmlWrapped = "<html><body><div class='act-details'><p>${pdfResult.extractedText.replace("\n", "</p><p>")}</p></div></body></html>"
                            val parsed = BdlawsHtmlParser.parseHtml(htmlWrapped, "https://bdlaws.minlaw.gov.bd")
                            if (parsed.sections.isEmpty()) {
                                RecoveryImportResult(
                                    success = false,
                                    lawId = targetLawId,
                                    backupFilePath = null,
                                    comparison = null,
                                    verificationReport = null,
                                    message = pdfResult.warningMessage ?: "PDF থেকে ধারা বিভাজন করা যায়নি।"
                                )
                            } else {
                                recoveryManager.applyParsedPackageTransactionally(
                                    parsed = parsed,
                                    targetLawId = targetLawId,
                                    overrideVerificationStatus = pdfResult.verificationStatus
                                )
                            }
                        }
                    } else {
                        val text = resolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                        val trimmed = text.trimStart()
                        if (trimmed.startsWith("{")) {
                            recoveryManager.importOfficialJsonPackage(text, targetLawId)
                        } else {
                            recoveryManager.importOfficialHtml(text, targetLawId)
                        }
                    }
                } catch (e: Exception) {
                    RecoveryImportResult(
                        success = false,
                        lawId = targetLawId,
                        backupFilePath = null,
                        comparison = null,
                        verificationReport = null,
                        message = "ফাইল ইম্পোর্ট ত্রুটি: ${e.localizedMessage}"
                    )
                }
            }
            _importResult.value = result
            _isImporting.value = false
        }
    }

    // Dark Mode preference
    private val _isDarkTheme = MutableStateFlow<Boolean?>(null)
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    fun setDarkTheme(enabled: Boolean?) {
        _isDarkTheme.value = enabled
    }
}
