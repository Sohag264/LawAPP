package com.example.presentation.lawreader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.LawViewModel
import com.example.presentation.components.LegalBadge
import com.example.presentation.components.SectionCard
import com.example.presentation.components.buildCompleteSectionTextForExport
import com.example.ui.theme.BadgeOfficial
import com.example.utils.BanglaNumberUtils
import com.example.utils.PdfExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LawReaderScreen(
    lawId: Long,
    targetSectionId: Long? = null,
    viewModel: LawViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val allLaws by viewModel.allLaws.collectAsStateWithLifecycle()
    val law = allLaws.find { it.id == lawId }

    val chapters by viewModel.repository.getChaptersForLaw(lawId).collectAsStateWithLifecycle(emptyList())
    val sections by viewModel.repository.getSectionsForLaw(lawId).collectAsStateWithLifecycle(emptyList())
    val subsections by viewModel.repository.getSubsectionsForLaw(lawId).collectAsStateWithLifecycle(emptyList())
    val footnotes by viewModel.repository.getFootnotesForLaw(lawId).collectAsStateWithLifecycle(emptyList())
    val schedules by viewModel.repository.getSchedulesForLaw(lawId).collectAsStateWithLifecycle(emptyList())
    val verificationReport by viewModel.repository.getVerificationReportForLaw(lawId).collectAsStateWithLifecycle(null)

    val favorites by viewModel.allFavorites.collectAsStateWithLifecycle()
    val bookmarks by viewModel.allBookmarks.collectAsStateWithLifecycle()
    val textScale by viewModel.readerTextScale.collectAsStateWithLifecycle()
    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
    val importResult by viewModel.importResult.collectAsStateWithLifecycle()

    val subsectionsBySection = remember(subsections) { subsections.groupBy { it.sectionId } }
    val footnotesBySection = remember(footnotes) { footnotes.groupBy { it.sectionId } }
    val chaptersById = remember(chapters) { chapters.associateBy { it.id } }

    val importDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importOfficialDocumentUri(uri, targetLawId = lawId)
        }
    }

    var showInLawSearch by remember { mutableStateOf(false) }
    var inLawQuery by remember { mutableStateOf("") }
    var showTableOfContents by remember { mutableStateOf(false) }

    val hasVerificationBanner = verificationReport != null || sections.isEmpty()
    val sectionsStartIndex = 1 + if (hasVerificationBanner) 1 else 0

    // Scroll to target section if provided
    LaunchedEffect(sections, targetSectionId, sectionsStartIndex) {
        if (targetSectionId != null && sections.isNotEmpty()) {
            val index = sections.indexOfFirst { it.id == targetSectionId }
            if (index != -1) {
                listState.animateScrollToItem(index + sectionsStartIndex)
                viewModel.recordHistory(lawId, targetSectionId)
            }
        }
    }

    val filteredSections = if (inLawQuery.isBlank()) {
        sections
    } else {
        val q = inLawQuery.trim()
        val qBn = BanglaNumberUtils.toBanglaDigits(q)
        val qEn = BanglaNumberUtils.toEnglishDigits(q)
        sections.filter { s ->
            val subTextHit = (subsectionsBySection[s.id] ?: emptyList()).any {
                it.subsectionText.contains(q, ignoreCase = true) || it.subsectionNumber.contains(q, ignoreCase = true)
            }
            s.sectionNumberBn.contains(qBn) ||
                s.sectionNumberEn.contains(qEn) ||
                s.titleBn.contains(q, ignoreCase = true) ||
                s.fullContentBn.contains(q, ignoreCase = true) ||
                s.simplifiedExplanationBn.contains(q, ignoreCase = true) ||
                subTextHit
        }
    }

    val isLawFav = favorites.any { it.lawId == lawId && it.sectionId == null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = law?.titleBn ?: "আইন পাঠ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${law?.actNumber ?: ""} • ${BanglaNumberUtils.toBanglaDigits(law?.actYear ?: 0)} সাল",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("reader_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান"
                        )
                    }
                },
                actions = {
                    // Search in Law
                    IconButton(
                        onClick = { showInLawSearch = !showInLawSearch },
                        modifier = Modifier.testTag("in_law_search_toggle")
                    ) {
                        Icon(
                            imageVector = if (showInLawSearch) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "আইনে খুঁজুন"
                        )
                    }

                    // Table of Contents
                    IconButton(
                        onClick = { showTableOfContents = true },
                        modifier = Modifier.testTag("toc_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = "সূচিপত্র"
                        )
                    }

                    // Font Size Increase / Decrease
                    IconButton(
                        onClick = {
                            if (textScale > 1.25f) viewModel.decreaseFontSize() else viewModel.increaseFontSize()
                        },
                        modifier = Modifier.testTag("font_size_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "ফন্ট সাইজ"
                        )
                    }

                    // PDF Export Full Law (including subsections & footnotes)
                    IconButton(
                        onClick = {
                            if (law != null && sections.isNotEmpty()) {
                                val sectionPairs = sections.map { s ->
                                    val secSubs = subsectionsBySection[s.id] ?: emptyList()
                                    val secFns = footnotesBySection[s.id] ?: emptyList()
                                    Pair(
                                        "ধারা ${s.sectionNumberBn}: ${s.titleBn}",
                                        buildCompleteSectionTextForExport(s, secSubs, secFns)
                                    )
                                }
                                val pdfFile = PdfExporter.exportLawToPdf(
                                    context = context,
                                    lawTitleBn = law.titleBn,
                                    lawTitleEn = law.titleEn,
                                    actDetails = "${law.actNumber} (প্রণয়ন: ${law.passedDate})",
                                    sections = sectionPairs,
                                    officialSourceUrl = law.sourceUrl
                                )
                                if (pdfFile != null) {
                                    PdfExporter.sharePdf(context, pdfFile, "${law.titleBn} PDF - Bangladesh Laws")
                                } else {
                                    Toast.makeText(context, "পিডিএফ তৈরিতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "পিডিএফ তৈরির জন্য কোনো ধারা সংরক্ষিত নেই", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("export_full_law_pdf")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "সম্পূর্ণ আইন PDF"
                        )
                    }

                    // Favorite Law Toggle
                    IconButton(
                        onClick = {
                            viewModel.toggleLawFavorite(lawId, isLawFav)
                        },
                        modifier = Modifier.testTag("fav_law_btn")
                    ) {
                        Icon(
                            imageVector = if (isLawFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "পছন্দ",
                            tint = if (isLawFav) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("law_reader_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isImporting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Find in Law inline bar
            AnimatedVisibility(visible = showInLawSearch) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inLawQuery,
                            onValueChange = { inLawQuery = it },
                            placeholder = { Text("এই আইনের ধারা, উপধারা বা শব্দ খুঁজুন…") },
                            singleLine = true,
                            trailingIcon = {
                                if (inLawQuery.isNotEmpty()) {
                                    IconButton(onClick = { inLawQuery = "" }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "পরিষ্কার")
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("in_law_search_field")
                        )
                    }
                }
            }

            if (law == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("আইনের তথ্য লোড হচ্ছে...")
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Law Meta Header Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LegalBadge(text = law.status, color = BadgeOfficial)
                                    Text(
                                        text = "সংরক্ষিত: ${BanglaNumberUtils.toBanglaDigits(sections.size)} / ${BanglaNumberUtils.toBanglaDigits(law.totalSections)} ধারা",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = law.titleBn,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (law.titleEn.isNotEmpty()) {
                                    Text(
                                        text = law.titleEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = law.fullIntroductionBn,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.5.sp * textScale
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "কার্যকর: ${law.effectiveDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(
                                        modifier = Modifier.clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(law.sourceUrl))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "লিংক খুলতে ব্যর্থ", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "সরকারি উৎস (bdlaws)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.OpenInBrowser,
                                            contentDescription = "ব্রাউজারে খুলুন",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Honest Data Completeness & Verification Banner
                    if (hasVerificationBanner) {
                        item {
                            val report = verificationReport
                            val isComplete = report?.isComplete == true && sections.isNotEmpty()
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isComplete) {
                                        Color(0xFFE8F5E9)
                                    } else {
                                        Color(0xFFFFF3E0)
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("law_completeness_banner")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (isComplete) Color(0xFF2E7D32) else Color(0xFFE65100)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isComplete) {
                                                "সম্পূর্ণ ও যাচাইকৃত আইন (${BanglaNumberUtils.toBanglaDigits(sections.size)}টি ধারা)"
                                            } else if (sections.isEmpty()) {
                                                "এই আইনের ধারাগুলো এখনও ডেটাবেজে সংরক্ষিত হয়নি"
                                            } else {
                                                "আংশিক সংগৃহীত আইন (${BanglaNumberUtils.toBanglaDigits(report?.actualSectionsCount ?: sections.size)}/${BanglaNumberUtils.toBanglaDigits(report?.expectedSectionsCount ?: law.totalSections)} ধারা)"
                                            },
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isComplete) Color(0xFF1B5E20) else Color(0xFFBF360C)
                                        )
                                    }

                                    if (!isComplete && report != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        if (report.missingSectionsCount > 0) {
                                            Text(
                                                text = "অনুপস্থিত ধারা (${BanglaNumberUtils.toBanglaDigits(report.missingSectionsCount)}টি): ${report.missingSectionsList}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF4E342E)
                                            )
                                        }
                                        if (report.incompleteSubsectionsCount > 0) {
                                            Text(
                                                text = "অসম্পূর্ণ উপধারা/শর্ত শনাক্ত: ${BanglaNumberUtils.toBanglaDigits(report.incompleteSubsectionsCount)}টি",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF4E342E)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedButton(
                                            onClick = {
                                                importDocumentLauncher.launch(
                                                    arrayOf("text/html", "application/json", "application/pdf", "*/*")
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("reader_import_official_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.UploadFile,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("অফিসিয়াল HTML / JSON / PDF ইম্পোর্ট করুন")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Sections list
                    itemsIndexed(filteredSections, key = { _, s -> s.id }) { _, section ->
                        val isBookmarked = bookmarks.any { it.sectionId == section.id }
                        val isSectionFav = favorites.any { it.lawId == lawId && it.sectionId == section.id }
                        val ch = chaptersById[section.chapterId]
                        val chLabel = ch?.let { "${it.chapterNumberBn}: ${it.titleBn}" }

                        SectionCard(
                            section = section,
                            isBookmarked = isBookmarked,
                            isFavorite = isSectionFav,
                            textScale = textScale,
                            lawTitleBn = law.titleBn,
                            officialSourceUrl = section.sourceUrl.ifEmpty { law.sourceUrl },
                            onBookmarkToggle = {
                                viewModel.toggleBookmark(lawId, section.id, isBookmarked)
                            },
                            onFavoriteToggle = {
                                viewModel.toggleSectionFavorite(lawId, section.id, isSectionFav)
                            },
                            onSaveExplanation = { newExp ->
                                viewModel.saveSectionExplanation(section.id, newExp)
                            },
                            onSavePersonalNote = { note ->
                                viewModel.savePersonalNote(section.id, lawId, note)
                            },
                            subsections = subsectionsBySection[section.id] ?: emptyList(),
                            footnotes = footnotesBySection[section.id] ?: emptyList(),
                            chapterTitle = chLabel
                        )
                    }

                    // Schedules Section (তফসিলসমূহ) if available
                    if (schedules.isNotEmpty()) {
                        item {
                            Text(
                                text = "তফসিলসমূহ (Schedules)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(schedules, key = { it.id }) { sch ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "${sch.scheduleNumber}: ${sch.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = sch.contentText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp * textScale,
                                            lineHeight = 21.sp * textScale
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Import Result Feedback Dialog
    if (importResult != null) {
        val res = importResult!!
        AlertDialog(
            onDismissRequest = { viewModel.clearImportResult() },
            title = {
                Text(
                    text = if (res.success) "অফিসিয়াল ডেটা ইম্পোর্ট সম্পন্ন" else "ইম্পোর্ট ব্যর্থ",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = res.message, style = MaterialTheme.typography.bodyMedium)
                    res.comparison?.let { cmp ->
                        Text(text = cmp.summaryMessage, style = MaterialTheme.typography.bodySmall)
                    }
                    res.verificationReport?.let { rep ->
                        Text(
                            text = "যাচাইকৃত অবস্থা: ${rep.actualSectionsCount}/${rep.expectedSectionsCount} ধারা, অনুপস্থিত: ${rep.missingSectionsCount}টি",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (!res.backupFilePath.isNullOrBlank()) {
                        Text(
                            text = "সুরক্ষা ব্যাকআপ সংরক্ষিত হয়েছে।",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.clearImportResult() }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // Table of Contents Bottom Sheet
    if (showTableOfContents) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showTableOfContents = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "আইনের সূচিপত্র ও ধারা তালিকা (${BanglaNumberUtils.toBanglaDigits(sections.size)}টি)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (sections.isEmpty()) {
                    Text(
                        text = "এই আইনে এখনও কোনো ধারা সংরক্ষিত নেই। উপরের 'অফিসিয়াল HTML/JSON/PDF ইম্পোর্ট করুন' বোতাম ব্যবহার করে ধারা যুক্ত করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(sections) { sIndex, sec ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showTableOfContents = false
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(sIndex + sectionsStartIndex)
                                            viewModel.recordHistory(lawId, sec.id)
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "ধারা ${sec.sectionNumberBn}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = sec.titleBn,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Text(
                                        text = "Sec ${sec.sectionNumberEn}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
