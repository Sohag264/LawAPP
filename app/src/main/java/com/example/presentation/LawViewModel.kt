package com.example.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.FavoriteEntity
import com.example.data.entity.LawEntity
import com.example.data.entity.PersonalNoteEntity
import com.example.data.entity.ReadingHistoryEntity
import com.example.data.entity.SectionEntity
import com.example.data.repository.DatabaseStats
import com.example.data.repository.IntegrityReport
import com.example.data.repository.LawRepository
import com.example.data.repository.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LawViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = LawRepository(
        database.lawDao(),
        database.sectionDao(),
        database.userDataDao()
    )

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
    private val _readerTextScale = MutableStateFlow(1.0f) // 0.85f to 1.35f
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

    // Category Filter in AllLaws screen
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

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

    // Save customized section explanation or personal notes
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

    // Database integrity report state
    private val _integrityReport = MutableStateFlow<IntegrityReport?>(null)
    val integrityReport: StateFlow<IntegrityReport?> = _integrityReport.asStateFlow()

    fun verifyDatabaseIntegrity() {
        viewModelScope.launch {
            _integrityReport.value = repository.verifyDatabaseIntegrity()
        }
    }

    fun clearIntegrityReport() {
        _integrityReport.value = null
    }

    // Dark Mode preference
    private val _isDarkTheme = MutableStateFlow<Boolean?>(null) // null means follow system
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    fun setDarkTheme(enabled: Boolean?) {
        _isDarkTheme.value = enabled
    }
}
