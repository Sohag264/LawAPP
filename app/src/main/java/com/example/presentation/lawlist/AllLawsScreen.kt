package com.example.presentation.lawlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.LawViewModel
import com.example.presentation.components.LawCard
import com.example.utils.BanglaNumberUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllLawsScreen(
    viewModel: LawViewModel,
    onNavigateToLaw: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allLaws by viewModel.allLaws.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val favorites by viewModel.allFavorites.collectAsStateWithLifecycle()

    // Determine laws to display: either all or filtered by category cross-ref
    val displayedLaws = if (selectedCategoryId == null) {
        allLaws
    } else {
        // Find laws belonging to selected category
        when (selectedCategoryId) {
            1L -> allLaws.filter { it.id == 1L } // Constitution
            2L -> allLaws.filter { it.id in listOf(2L, 3L, 6L, 8L) } // Criminal
            3L -> allLaws.filter { it.id == 3L } // CrPC
            4L -> allLaws.filter { it.id == 4L } // Evidence
            5L -> allLaws.filter { it.id in listOf(5L, 7L, 10L) } // Civil
            6L -> allLaws.filter { it.id == 6L } // Women & Child
            7L -> allLaws.filter { it.id == 8L } // Narcotics
            9L -> allLaws.filter { it.id == 7L } // Land & Specific Relief
            10L -> allLaws.filter { it.id == 11L } // Family
            12L -> allLaws.filter { it.id == 9L } // Anti-Corruption
            15L -> allLaws.filter { it.id == 12L } // Road Transport
            20L -> allLaws.filter { it.id == 10L } // Limitation Act
            else -> allLaws
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "বাংলাদেশের সকল আইন",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "মোট ${BanglaNumberUtils.toBanglaDigits(displayedLaws.size)}টি আইন তালিকাভুক্ত",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("all_laws_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("সকল আইন") },
                        leadingIcon = if (selectedCategoryId == null) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("chip_all_laws")
                    )
                }

                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = {
                            viewModel.selectCategory(if (selectedCategoryId == cat.id) null else cat.id)
                        },
                        label = { Text(cat.nameBn) },
                        leadingIcon = if (selectedCategoryId == cat.id) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("chip_cat_${cat.id}")
                    )
                }
            }

            // Laws List
            if (displayedLaws.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderSpecial,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.height(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "এই ক্যাটাগরিতে কোনো আইন পাওয়া যায়নি",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedLaws, key = { it.id }) { law ->
                        val isFav = favorites.any { it.lawId == law.id && it.sectionId == null }
                        LawCard(
                            law = law,
                            isFavorite = isFav,
                            onFavoriteToggle = { viewModel.toggleLawFavorite(law.id, isFav) },
                            onClick = { onNavigateToLaw(law.id) }
                        )
                    }
                }
            }
        }
    }
}
