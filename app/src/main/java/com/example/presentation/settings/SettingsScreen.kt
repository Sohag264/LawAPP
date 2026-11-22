package com.example.presentation.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.example.data.entity.LawEntity
import com.example.data.entity.LawVerificationReportEntity
import com.example.presentation.LawViewModel
import com.example.presentation.components.LegalBadge
import com.example.utils.BanglaNumberUtils
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LawViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val dataQuality by viewModel.dataQualitySummary.collectAsStateWithLifecycle()
    val verificationReports by viewModel.allVerificationReports.collectAsStateWithLifecycle()
    val allLaws by viewModel.allLaws.collectAsStateWithLifecycle()
    val integrityReport by viewModel.integrityReport.collectAsStateWithLifecycle()
    val importResult by viewModel.importResult.collectAsStateWithLifecycle()
    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
    val textScale by viewModel.readerTextScale.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val reportsByLawId = remember(verificationReports) {
        verificationReports.associateBy { it.lawId }
    }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var selectedLawForReport by remember { mutableStateOf<Pair<LawEntity, LawVerificationReportEntity?>?>(null) }
    var pendingImportLawId by remember { mutableStateOf<Long?>(null) }

    val importDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importOfficialDocumentUri(uri, targetLawId = pendingImportLawId)
        }
        pendingImportLawId = null
    }

    val lastVerifiedLabel = remember(dataQuality.lastVerifiedAt) {
        val ts = dataQuality.lastVerifiedAt
        if (ts == null || ts <= 0L) {
            "এখনো যাচাই করা হয়নি"
        } else {
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(ts))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "সেটিংস ও ডেটা মান ড্যাশবোর্ড",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        modifier = modifier.testTag("settings_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme & Display
            item {
                SettingsSectionHeader(title = "ডিসপ্লে ও থিম")
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showThemeDialog = true }
                                .padding(vertical = 8.dp)
                                .testTag("theme_setting_item"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Brightness4,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "থিম মোড (Theme)",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    val themeLabel = when (isDarkTheme) {
                                        true -> "ডার্ক মোড (Dark Theme)"
                                        false -> "লাইট মোড (Light Theme)"
                                        null -> "সিস্টেম ডিফল্ট (System Default)"
                                    }
                                    Text(
                                        text = themeLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FormatSize,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "আইন পড়ার লেখার আকার (Font Size)",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "বর্তমান সাইজ: ${((textScale * 100).toInt())}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row {
                                OutlinedButton(
                                    onClick = { viewModel.decreaseFontSize() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(40.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = { viewModel.increaseFontSize() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(40.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Data Quality Dashboard (10 Required Metrics + Import + Verification)
            item {
                SettingsSectionHeader(title = "Data Quality Dashboard (ডেটা মান ও যাচাইকরণ)")
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.testTag("data_quality_dashboard_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "অফলাইন ডেটাবেজ অডিট",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Room SQLite v2",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Grid of 10 Required Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QualityMetricTile(
                                label = "মোট আইন",
                                value = BanglaNumberUtils.toBanglaDigits(dataQuality.totalLaws),
                                accent = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            QualityMetricTile(
                                label = "মোট অধ্যায়",
                                value = BanglaNumberUtils.toBanglaDigits(dataQuality.totalChapters),
                                accent = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QualityMetricTile(
                                label = "মোট ধারা",
                                value = BanglaNumberUtils.toBanglaDigits(dataQuality.totalSections),
                                accent = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            QualityMetricTile(
                                label = "মোট উপধারা",
                                value = BanglaNumberUtils.toBanglaDigits(dataQuality.totalSubsections),
                                accent = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QualityMetricTile(
                                label = "সম্পূর্ণ আইন",
                                value = BanglaNumberUtils.toBanglaDigits(dataQuality.completeLawsCount),
                                accent = Color(0xFF2E7D32),
                                modifier = Modifier.weight(1f)
                            )
                            QualityMetricTile(
                                label = "অসম্পূর্ণ আইন",
                                value = BanglaNumberUtils.toBanglaDigits(dataQuality.incompleteLawsCount),
                                accent = Color(0xFFE65100),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QualityMetricTile(
                                label = "অনুপস্থিত ধারা",
                                value = BanglaNumberUtils.toBanglaDigits(dataQuality.totalMissingSectionsCount),
                                accent = Color(0xFFC62828),
                                modifier = Modifier.weight(1f)
                            )
                            QualityMetricTile(
                                label = "অসম্পূর্ণ উপধারা",
                                value = BanglaNumberUtils.toBanglaDigits(dataQuality.totalIncompleteSubsectionsCount),
                                accent = Color(0xFFEF6C00),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QualityMetricTile(
                                label = "যাচাই না করা আইন",
                                value = BanglaNumberUtils.toBanglaDigits(dataQuality.unverifiedLawsCount),
                                accent = Color(0xFF6A1B9A),
                                modifier = Modifier.weight(1f)
                            )
                            QualityMetricTile(
                                label = "সর্বশেষ যাচাই",
                                value = lastVerifiedLabel,
                                accent = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "• ক্যাটাগরি: ${BanglaNumberUtils.toBanglaDigits(stats.totalCategories)}টি  •  সরকারি উৎস: bdlaws.minlaw.gov.bd",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isImporting) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Import Official HTML / JSON / PDF Recovery Package
                        OutlinedButton(
                            onClick = {
                                pendingImportLawId = null
                                importDocumentLauncher.launch(
                                    arrayOf("text/html", "application/json", "application/pdf", "*/*")
                                )
                            },
                            enabled = !isImporting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("import_official_doc_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("অফিসিয়াল আইন ইম্পোর্ট / পুনরুদ্ধার (HTML / JSON / PDF)")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Verify Database Button
                        Button(
                            onClick = { viewModel.verifyDatabaseIntegrity() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verify_db_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ডেটাবেজ ও ধারা সম্পূর্ণতা যাচাই করুন")
                        }

                        // Verification Alert Box
                        if (integrityReport != null) {
                            val report = integrityReport!!
                            val fullyComplete = report.incompleteLawCount == 0 && report.missingSectionsCount == 0
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = if (fullyComplete) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (fullyComplete) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (fullyComplete) Color(0xFF2E7D32) else Color(0xFFE65100)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (fullyComplete) "সম্পূর্ণতা যাচাইকরণ সফল" else "আংশিক ডেটা ও অনুপস্থিত ধারা শনাক্ত",
                                            fontWeight = FontWeight.Bold,
                                            color = if (fullyComplete) Color(0xFF2E7D32) else Color(0xFFBF360C)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = report.statusMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF212121)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Per-Law Verification & Missing Section Reports
            item {
                SettingsSectionHeader(title = "আইনভিত্তিক যাচাই ও অনুপস্থিত ধারা রিপোর্ট")
            }

            items(allLaws, key = { it.id }) { law ->
                val rep = reportsByLawId[law.id]
                val actual = rep?.actualSectionsCount ?: 0
                val expected = rep?.expectedSectionsCount ?: law.totalSections
                val isComplete = rep?.isComplete == true

                val badgeText = when {
                    actual == 0 -> "ধারা নেই (০/${BanglaNumberUtils.toBanglaDigits(expected)})"
                    isComplete -> "সম্পূর্ণ (${BanglaNumberUtils.toBanglaDigits(actual)}/${BanglaNumberUtils.toBanglaDigits(expected)})"
                    else -> "আংশিক (${BanglaNumberUtils.toBanglaDigits(actual)}/${BanglaNumberUtils.toBanglaDigits(expected)})"
                }
                val badgeColor = when {
                    actual == 0 -> Color(0xFFC62828)
                    isComplete -> Color(0xFF2E7D32)
                    else -> Color(0xFFE65100)
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedLawForReport = Pair(law, rep) }
                        .testTag("verification_law_row_${law.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = law.titleBn,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (rep != null && rep.missingSectionsCount > 0) {
                                Text(
                                    text = "অনুপস্থিত ${BanglaNumberUtils.toBanglaDigits(rep.missingSectionsCount)}টি ধারা • বিস্তারিত দেখতে ট্যাপ করুন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else if (rep != null && rep.incompleteSubsectionsCount > 0) {
                                Text(
                                    text = "অসম্পূর্ণ উপধারা: ${BanglaNumberUtils.toBanglaDigits(rep.incompleteSubsectionsCount)}টি",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFE65100)
                                )
                            } else {
                                Text(
                                    text = "যাচাইকৃত উপধারা: ${BanglaNumberUtils.toBanglaDigits(rep?.verifiedSubsectionsCount ?: 0)}টি",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        LegalBadge(text = badgeText, color = badgeColor)
                    }
                }
            }

            // Backup & Restore
            item {
                SettingsSectionHeader(title = "ইউজার ডেটা ব্যাকআপ ও রিস্টোর")
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "আপনার ব্যক্তিগত বুকমার্ক, প্রিয় তালিকা ও এডিটকৃত নোটগুলো নিরাপদে JSON ব্যাকআপ হিসেবে সংরক্ষণ করতে পারবেন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val backupJson = viewModel.repository.exportUserBackupJson()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("LawAppBackup", backupJson))

                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Bangladesh Laws Backup JSON")
                                            putExtra(Intent.EXTRA_TEXT, backupJson)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "ব্যাকআপ শেয়ার করুন"))
                                        Toast.makeText(context, "ব্যাকআপ ক্লিপবোর্ডে কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_backup_btn")
                            ) {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ব্যাকআপ নিন")
                            }

                            OutlinedButton(
                                onClick = { showRestoreDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("restore_backup_btn")
                            ) {
                                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("রিস্টোর করুন")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                try {
                                    val pdfDir = File(context.cacheDir, "laws_pdf")
                                    if (pdfDir.exists()) {
                                        pdfDir.deleteRecursively()
                                    }
                                    Toast.makeText(context, "অস্থায়ী পিডিএফ ক্যাশ পরিষ্কার করা হয়েছে", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("clean_cache_btn")
                        ) {
                            Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("অস্থায়ী পিডিএফ ক্যাশ পরিষ্কার করুন")
                        }
                    }
                }
            }

            // About & Legal Disclaimer
            item {
                SettingsSectionHeader(title = "আইনি তথ্যের পরিচিতি ও দাবিত্যাগ")
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "বাংলাদেশ আইন (Bangladesh Laws)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "v2.0.0",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "একটি স্বয়ংসম্পূর্ণ, দ্রুতগতির ও সম্পূর্ণ অফলাইন নেটিভ অ্যান্ড্রয়েড আইনি রেফারেন্স লাইব্রেরি। Dom-ভিত্তিক অফিসিয়াল HTML/JSON/PDF ইম্পোর্ট, অনুপস্থিত ধারা নিরীক্ষণ ও সম্পূর্ণ ট্রানজেকশন-সুরক্ষিত ডেটা রিকভারি সুবিধাযুক্ত।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showDisclaimerDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("disclaimer_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("আইনি দাবিত্যাগ ও তথ্যসূত্র নীতি পড়ুন")
                        }
                    }
                }
            }
        }
    }

    // Per-Law Missing Section & Verification Report Dialog
    if (selectedLawForReport != null) {
        val (law, rep) = selectedLawForReport!!
        AlertDialog(
            onDismissRequest = { selectedLawForReport = null },
            title = {
                Text(
                    text = law.titleBn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (rep == null) {
                        Text("এই আইনের জন্য এখনও কোনো যাচাইকরণ প্রতিবেদন তৈরি হয়নি।")
                    } else {
                        Text(
                            text = "প্রত্যাশিত ধারা: ${BanglaNumberUtils.toBanglaDigits(rep.expectedSectionsCount)}টি\n" +
                                "সংরক্ষিত ধারা: ${BanglaNumberUtils.toBanglaDigits(rep.actualSectionsCount)}টি\n" +
                                "সম্পূর্ণ ধারা: ${BanglaNumberUtils.toBanglaDigits(rep.completeSectionsCount)}টি\n" +
                                "অসম্পূর্ণ ধারা: ${BanglaNumberUtils.toBanglaDigits(rep.incompleteSectionsCount)}টি\n" +
                                "বিলুপ্ত/রহিত ধারা: ${BanglaNumberUtils.toBanglaDigits(rep.repealedSectionsCount)}টি\n" +
                                "যাচাইকৃত উপধারা: ${BanglaNumberUtils.toBanglaDigits(rep.verifiedSubsectionsCount)}টি\n" +
                                "অসম্পূর্ণ উপধারা: ${BanglaNumberUtils.toBanglaDigits(rep.incompleteSubsectionsCount)}টি",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                        HorizontalDivider()
                        Text(
                            text = "অনুপস্থিত ধারা (${BanglaNumberUtils.toBanglaDigits(rep.missingSectionsCount)}টি):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = rep.missingSectionsList.ifBlank { "কোনো ধারা অনুপস্থিত নেই" },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (rep.missingSectionsCount > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
                        )
                        if (rep.duplicateSectionsList.isNotBlank()) {
                            Text(
                                text = "ডুপ্লিকেট ধারা: ${rep.duplicateSectionsList}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE65100)
                            )
                        }
                        if (rep.parsingErrorsList.isNotBlank()) {
                            HorizontalDivider()
                            Text(
                                text = "শনাক্তকৃত ত্রুটি/বিচ্যুতি:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = rep.parsingErrorsList,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingImportLawId = law.id
                        selectedLawForReport = null
                        importDocumentLauncher.launch(
                            arrayOf("text/html", "application/json", "application/pdf", "*/*")
                        )
                    },
                    modifier = Modifier.testTag("dialog_import_for_law_btn")
                ) {
                    Text("অফিসিয়াল ফাইল ইম্পোর্ট")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLawForReport = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // Import Result Dialog
    if (importResult != null) {
        val res = importResult!!
        AlertDialog(
            onDismissRequest = { viewModel.clearImportResult() },
            title = {
                Text(
                    text = if (res.success) "ডেটা পুনরুদ্ধার ও ইম্পোর্ট সম্পন্ন" else "ইম্পোর্ট ব্যর্থ",
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
                            text = "হালনাগাদ অবস্থা: ${rep.actualSectionsCount}/${rep.expectedSectionsCount} ধারা (অনুপস্থিত: ${rep.missingSectionsCount}টি)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (!res.backupFilePath.isNullOrBlank()) {
                        Text(
                            text = "ব্যাকআপ সংরক্ষিত: ${File(res.backupFilePath).name}",
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

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("থিম নির্বাচন করুন") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setDarkTheme(null)
                                showThemeDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isDarkTheme == null, onClick = {
                            viewModel.setDarkTheme(null)
                            showThemeDialog = false
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("সিস্টেম ডিফল্ট (System Default)")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setDarkTheme(false)
                                showThemeDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isDarkTheme == false, onClick = {
                            viewModel.setDarkTheme(false)
                            showThemeDialog = false
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("লাইট মোড (Light Theme)")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setDarkTheme(true)
                                showThemeDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isDarkTheme == true, onClick = {
                            viewModel.setDarkTheme(true)
                            showThemeDialog = false
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ডার্ক মোড (Dark Theme)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // Restore Backup Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("ব্যাকআপ থেকে রিস্টোর") },
            text = {
                Column {
                    Text(
                        text = "পূর্বে কপি করা ব্যাকআপ JSON কোডটি নিচে পেস্ট করুন:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = { Text("{\"version\":2, ...}") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = viewModel.repository.restoreUserBackupJson(restoreJsonInput)
                            showRestoreDialog = false
                            if (success) {
                                Toast.makeText(context, "সফলভাবে রিস্টোর হয়েছে", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "রিস্টোর ব্যর্থ: ভুল JSON ফরম্যাট", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("রিস্টোর নিশ্চিত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Disclaimer Dialog
    if (showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showDisclaimerDialog = false },
            title = { Text("সরকারি উৎস ও দাবিত্যাগ") },
            text = {
                Text(
                    text = "১. তথ্যের মূল সরকারি উৎস: বাংলাদেশ সরকারের আইন, বিচার ও সংসদ বিষয়ক মন্ত্রণালয়ের লেজিসলেটিভ ও সংসদ বিষয়ক বিভাগের পোর্টাল (https://bdlaws.minlaw.gov.bd)।\n\n" +
                        "২. কপিরাইট ও সত্ত্ব: বাংলাদেশের সরকারি আইনসমূহ সর্বসাধারণের জ্ঞাতার্থে উন্মুক্ত পাবলিক ডোমেইন দলিল। এই অ্যাপ্লিকেশনে আইনের মূল বক্তব্য কোনো পরিবর্তন ব্যতীত উপস্থাপন করা হয়েছে এবং অসম্পূর্ণ ধারাগুলো স্বচ্ছভাবে চিহ্নিত করা হয়েছে।\n\n" +
                        "৩. আইনগত পরামর্শ: এই অ্যাপ্লিকেশনটি কেবল সাধারণ তথ্য, শিক্ষা ও গবেষণামূলক উদ্দেশ্যে প্রস্তুতকৃত। যেকোনো আইনি পদক্ষেপ গ্রহণের পূর্বে আদালতের সর্বশেষ গেজেট ও সনদপ্রাপ্ত আইনজীবীর পরামর্শ গ্রহণ কাম্য।",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(onClick = { showDisclaimerDialog = false }) {
                    Text("বুঝেছি")
                }
            }
        )
    }
}

@Composable
private fun QualityMetricTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
