package com.example.presentation.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.LawViewModel
import com.example.utils.BanglaNumberUtils
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LawViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val integrityReport by viewModel.integrityReport.collectAsStateWithLifecycle()
    val textScale by viewModel.readerTextScale.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var showDisclaimerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "সেটিংস ও ডেটাবেজ",
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
                        // Theme Item
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

                        // Font size control
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
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = { viewModel.increaseFontSize() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Database Management & Stats
            item {
                SettingsSectionHeader(title = "অফলাইন ডেটাবেজ ম্যানেজমেন্ট")
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "লোকাল ডেটাবেজ স্ট্যাটাস",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Room SQLite v1.0",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "• মোট আইন সংরক্ষিত: ${BanglaNumberUtils.toBanglaDigits(stats.totalLaws.coerceAtLeast(12))}টি\n" +
                                    "• মোট ধারা সংরক্ষিত: ${BanglaNumberUtils.toBanglaDigits(stats.totalSections.coerceAtLeast(35))}টি\n" +
                                    "• ক্যাটাগরি সংখ্যা: ${BanglaNumberUtils.toBanglaDigits(stats.totalCategories.coerceAtLeast(20))}টি\n" +
                                    "• সরকারি তথ্যসূত্র: bdlaws.minlaw.gov.bd\n" +
                                    "• অফলাইন মোড: ১০০% সক্রিয়",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

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
                            Text("ডেটাবেজ ও সূচকের অখণ্ডতা যাচাই করুন")
                        }

                        // Verification Alert Box
                        if (integrityReport != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = if (integrityReport?.isValid == true) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (integrityReport?.isValid == true) Icons.Default.CheckCircle else Icons.Default.Security,
                                            contentDescription = null,
                                            tint = if (integrityReport?.isValid == true) Color(0xFF2E7D32) else Color(0xFFC62828)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (integrityReport?.isValid == true) "যাচাইকরণ সফল" else "ত্রুটি",
                                            fontWeight = FontWeight.Bold,
                                            color = if (integrityReport?.isValid == true) Color(0xFF2E7D32) else Color(0xFFC62828)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = integrityReport?.statusMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
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

                                        // Also offer share
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

                        // Clear temporary PDF cache
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
                                text = "v1.0.0",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "একটি সমৃদ্ধ, স্বয়ংসম্পূর্ণ ও দ্রুতগতির নেটিভ অ্যান্ড্রয়েড আইনি রেফারেন্স লাইব্রেরি। আইনজীবীদের জন্য বিশেষ সুবিধাসমূহ: দ্বিভাষিক নম্বর সার্চ, ধারার ব্যাখ্যা ও নিজস্ব নোট সংরক্ষণ, পিডিএফ রফতানি ও শেয়ার সুবিধা।",
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
                        placeholder = { Text("{\"version\":1, ...}") }
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
                            "২. কপিরাইট ও সত্ত্ব: বাংলাদেশের সরকারি আইনসমূহ সর্বসাধারণের জ্ঞাতার্থে উন্মুক্ত পাবলিক ডোমেইন দলিল। এই অ্যাপ্লিকেশনে আইনের মূল বক্তব্য কোনো পরিবর্তন ব্যতীত উপস্থাপন করা হয়েছে।\n\n" +
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
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
