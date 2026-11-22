package com.example.presentation.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExtractionStatus
import com.example.data.entity.FootnoteEntity
import com.example.data.entity.LawEntity
import com.example.data.entity.LawVerificationReportEntity
import com.example.data.entity.SectionEntity
import com.example.data.entity.SubsectionEntity
import com.example.data.entity.SubsectionNodeType
import com.example.data.entity.VerificationStatus
import com.example.ui.theme.BadgeBailable
import com.example.ui.theme.BadgeCognizable
import com.example.ui.theme.BadgeNonBailable
import com.example.ui.theme.BadgeOfficial
import com.example.utils.BanglaNumberUtils
import com.example.utils.PdfExporter

@Composable
fun StatCounterCard(
    count: String,
    label: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = count,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LawCard(
    law: LawEntity,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    verificationReport: LawVerificationReportEntity? = null
) {
    val completenessLabel: String
    val completenessColor: Color
    when {
        law.extractionStatus == ExtractionStatus.NOT_EXTRACTED ||
            (verificationReport != null && verificationReport.actualSectionsCount == 0) -> {
            completenessLabel = "ধারা অনুপস্থিত"
            completenessColor = Color(0xFFC62828)
        }
        law.extractionStatus == ExtractionStatus.COMPLETE &&
            law.verificationStatus == VerificationStatus.VERIFIED -> {
            completenessLabel = "সম্পূর্ণ যাচাইকৃত"
            completenessColor = Color(0xFF2E7D32)
        }
        law.verificationStatus == VerificationStatus.OCR_UNVERIFIED -> {
            completenessLabel = "OCR অযাচাইকৃত"
            completenessColor = Color(0xFFEF6C00)
        }
        else -> {
            val actual = verificationReport?.actualSectionsCount
            completenessLabel = if (actual != null && law.totalSections > 0) {
                "আংশিক (${BanglaNumberUtils.toBanglaDigits(actual)}/${BanglaNumberUtils.toBanglaDigits(law.totalSections)})"
            } else {
                "আংশিক সংগৃহীত"
            }
            completenessColor = Color(0xFFE65100)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("law_card_${law.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = law.actNumber,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "${BanglaNumberUtils.toBanglaDigits(law.actYear)} সাল",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("favorite_law_${law.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "পছন্দের আইন",
                        tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = law.titleBn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (law.titleEn.isNotEmpty()) {
                Text(
                    text = law.titleEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = law.shortDescriptionBn,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    LegalBadge(
                        text = law.status,
                        color = BadgeOfficial
                    )
                    LegalBadge(
                        text = completenessLabel,
                        color = completenessColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "আইন পড়ুন",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LegalBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun buildCompleteSectionTextForExport(
    section: SectionEntity,
    subsections: List<SubsectionEntity>,
    footnotes: List<FootnoteEntity>
): String {
    val sb = StringBuilder()
    sb.append(section.fullContentBn)
    if (section.fullContentEn.isNotBlank() && section.fullContentEn.trim() != section.fullContentBn.trim()) {
        sb.append("\n\n[Official English Text]\n")
        sb.append(section.fullContentEn)
    }
    if (subsections.isNotEmpty() && subsections.size > 1) {
        sb.append("\n\n[উপধারা, দফা ও ব্যাখ্যা]\n")
        subsections.forEach { sub ->
            val indent = if (sub.parentSubsectionId != null) "   " else ""
            sb.append("$indent${sub.subsectionText}\n")
        }
    }
    if (footnotes.isNotEmpty()) {
        sb.append("\n[পাদটীকা / Footnotes]\n")
        footnotes.forEach { fn ->
            sb.append("[${fn.footnoteMarker}] ${fn.footnoteText}\n")
        }
    }
    if (section.amendmentNote.isNotBlank()) {
        sb.append("\n[সংশোধনী তথ্য]\n")
        sb.append(section.amendmentNote)
    }
    if (section.simplifiedExplanationBn.isNotEmpty()) {
        sb.append("\n[সহজ সরল ব্যাখ্যা ও আইনের প্রায়োগিক দিক]\n")
        sb.append(section.simplifiedExplanationBn)
    }
    return sb.toString().trim()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SectionCard(
    section: SectionEntity,
    isBookmarked: Boolean,
    isFavorite: Boolean,
    textScale: Float = 1.0f,
    lawTitleBn: String = "",
    officialSourceUrl: String = "https://bdlaws.minlaw.gov.bd",
    onBookmarkToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onSaveExplanation: (String) -> Unit,
    onSavePersonalNote: (String) -> Unit,
    modifier: Modifier = Modifier,
    subsections: List<SubsectionEntity> = emptyList(),
    footnotes: List<FootnoteEntity> = emptyList(),
    chapterTitle: String? = null
) {
    val context = LocalContext.current
    var showEditNoteDialog by remember { mutableStateOf(false) }
    var showEditExplanationDialog by remember { mutableStateOf(false) }
    var expandSubsections by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("section_card_${section.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            if (!chapterTitle.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = chapterTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Header: Section Number Badge & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "ধারা ${section.sectionNumberBn}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sec ${section.sectionNumberEn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBookmarkToggle,
                        modifier = Modifier.size(48.dp).testTag("bookmark_section_${section.id}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "বুকমার্ক",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(48.dp).testTag("fav_section_${section.id}")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "পছন্দ",
                            tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Section Title
            Text(
                text = section.titleBn,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp * textScale
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (section.titleEn.isNotEmpty() && section.titleEn != section.titleBn) {
                Text(
                    text = section.titleEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Legal Attribute & Integrity Badges
            val isVerificationPending = section.verificationStatus == VerificationStatus.VERIFICATION_PENDING ||
                section.verificationStatus == VerificationStatus.UNVERIFIED ||
                section.originalHtml.isBlank()
            val hasBadges = section.bailableStatus.isNotEmpty() ||
                section.cognizableStatus.isNotEmpty() ||
                section.trialCourt.isNotEmpty() ||
                section.isRepealed ||
                section.isAmended ||
                section.extractionStatus == ExtractionStatus.PARTIAL ||
                isVerificationPending

            if (hasBadges) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (section.isRepealed) {
                        LegalBadge(
                            text = "বিলুপ্ত / রহিত (Repealed)",
                            color = Color(0xFFC62828)
                        )
                    }
                    if (section.isAmended) {
                        LegalBadge(
                            text = "সংশোধিত (Amended)",
                            color = Color(0xFF6A1B9A)
                        )
                    }
                    if (section.extractionStatus == ExtractionStatus.PARTIAL) {
                        LegalBadge(
                            text = "আংশিক পাঠ (Partial)",
                            color = Color(0xFFE65100)
                        )
                    }
                    if (section.verificationStatus == VerificationStatus.VERIFIED && section.originalHtml.isNotBlank()) {
                        LegalBadge(
                            text = "সরকারি উৎস যাচাইকৃত",
                            color = Color(0xFF2E7D32)
                        )
                    } else if (isVerificationPending) {
                        LegalBadge(
                            text = "যাচাই অপেক্ষমাণ",
                            color = Color(0xFF5D4037)
                        )
                    }
                    if (section.bailableStatus.isNotEmpty()) {
                        val isNonBail = section.bailableStatus.contains("অ-জামিন") || section.bailableStatus.contains("Non")
                        LegalBadge(
                            text = section.bailableStatus,
                            color = if (isNonBail) BadgeNonBailable else BadgeBailable
                        )
                    }
                    if (section.cognizableStatus.isNotEmpty()) {
                        LegalBadge(
                            text = section.cognizableStatus,
                            color = BadgeCognizable
                        )
                    }
                    if (section.trialCourt.isNotEmpty()) {
                        LegalBadge(
                            text = section.trialCourt,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Punishment Summary Card
            if (section.punishmentSummaryBn.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚖️ সাজার বিধান: ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = section.punishmentSummaryBn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Verbatim Official Text (complete, never truncated)
            Text(
                text = section.fullContentBn,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp * textScale,
                    lineHeight = 24.sp * textScale
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (section.fullContentEn.isNotBlank() && section.fullContentEn.trim() != section.fullContentBn.trim()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "মূল ইংরেজি পাঠ (Official English Text)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = section.fullContentEn,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp * textScale,
                                lineHeight = 21.sp * textScale
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Expandable Hierarchical Subsections / Clauses / Provisos / Exceptions
            if (subsections.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandSubsections = !expandSubsections }
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                                .testTag("toggle_subsections_${section.id}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "উপধারা, দফা ও শর্তাবলীর বিন্যাস (${BanglaNumberUtils.toBanglaDigits(subsections.size)}টি অংশ)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (expandSubsections) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expandSubsections) "সংকুচিত করুন" else "বিস্তারিত দেখুন",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        AnimatedVisibility(visible = expandSubsections) {
                            Column(
                                modifier = Modifier.padding(top = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                subsections.forEach { node ->
                                    val isChild = node.parentSubsectionId != null ||
                                        node.nodeType == SubsectionNodeType.CLAUSE ||
                                        node.nodeType == SubsectionNodeType.SUBCLAUSE
                                    val nodeBadgeLabel = when (node.nodeType) {
                                        SubsectionNodeType.PROVISO -> "শর্তাংশ"
                                        SubsectionNodeType.EXPLANATION -> "ব্যাখ্যা"
                                        SubsectionNodeType.EXCEPTION -> "ব্যতিক্রম"
                                        SubsectionNodeType.ILLUSTRATION -> "দৃষ্টান্ত"
                                        SubsectionNodeType.CLAUSE -> "দফা ${node.subsectionNumber}"
                                        SubsectionNodeType.SUBCLAUSE -> "উপদফা ${node.subsectionNumber}"
                                        else -> "উপধারা ${node.subsectionNumber}"
                                    }
                                    val nodeBadgeColor = when (node.nodeType) {
                                        SubsectionNodeType.PROVISO -> Color(0xFF00695C)
                                        SubsectionNodeType.EXPLANATION -> Color(0xFF1565C0)
                                        SubsectionNodeType.EXCEPTION -> Color(0xFFC62828)
                                        SubsectionNodeType.ILLUSTRATION -> Color(0xFF6A1B9A)
                                        else -> MaterialTheme.colorScheme.primary
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = if (isChild) 14.dp else 2.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(10.dp)
                                    ) {
                                        LegalBadge(
                                            text = nodeBadgeLabel,
                                            color = nodeBadgeColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = node.subsectionText,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp * textScale,
                                                lineHeight = 21.sp * textScale
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Footnotes Section
            if (footnotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "পাদটীকা (Footnotes)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        footnotes.forEach { fn ->
                            Text(
                                text = "[${fn.footnoteMarker}] ${fn.footnoteText}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Simplified Explanation Box (সহজ সরল ও সাবলীল ব্যাখ্যা)
            if (section.simplifiedExplanationBn.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 সহজ সরল ব্যাখ্যা ও প্রায়োগিক দিক",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { showEditExplanationDialog = true },
                                modifier = Modifier.size(36.dp).testTag("edit_explanation_${section.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "ব্যাখ্যা সম্পাদনা",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = section.simplifiedExplanationBn,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp * textScale,
                                lineHeight = 21.sp * textScale
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // User / Lawyer Personal Notes Box
            if (!section.userNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📝 আপনার ব্যক্তিগত নোট",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            IconButton(
                                onClick = { showEditNoteDialog = true },
                                modifier = Modifier.size(36.dp).testTag("edit_note_${section.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "নোট সম্পাদনা",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = section.userNotes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action toolbar: Note, Copy, Share, PDF Export
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showEditNoteDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_note_btn_${section.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NoteAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (section.userNotes.isNullOrBlank()) "নোট যোগ করুন" else "নোট সম্পাদনা",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Row {
                    // Copy Section
                    IconButton(
                        onClick = {
                            val exportBody = buildCompleteSectionTextForExport(section, subsections, footnotes)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(
                                "Section ${section.sectionNumberBn}",
                                "${section.titleBn} (ধারা ${section.sectionNumberBn})\n\n$exportBody\n\nউৎস: $officialSourceUrl"
                            )
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "সম্পূর্ণ ধারা ও উপধারা কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(48.dp).testTag("copy_section_${section.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "কপি করুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Share Section
                    IconButton(
                        onClick = {
                            val exportBody = buildCompleteSectionTextForExport(section, subsections, footnotes)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "${section.titleBn} (ধারা ${section.sectionNumberBn})")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "আইন: $lawTitleBn\nধারা: ${section.sectionNumberBn} - ${section.titleBn}\n\n$exportBody\n\nতথ্যসূত্র: $officialSourceUrl"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "ধারা শেয়ার করুন"))
                        },
                        modifier = Modifier.size(48.dp).testTag("share_section_${section.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "শেয়ার করুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // PDF Export Section
                    IconButton(
                        onClick = {
                            val exportBody = buildCompleteSectionTextForExport(section, subsections, footnotes)
                            val file = PdfExporter.exportLawToPdf(
                                context = context,
                                lawTitleBn = lawTitleBn,
                                lawTitleEn = "",
                                actDetails = "ধারা ${section.sectionNumberBn}: ${section.titleBn}",
                                sections = listOf(
                                    Pair(
                                        "ধারা ${section.sectionNumberBn}: ${section.titleBn}",
                                        exportBody
                                    )
                                ),
                                officialSourceUrl = officialSourceUrl
                            )
                            if (file != null) {
                                PdfExporter.sharePdf(context, file, "ধারা ${section.sectionNumberBn} PDF - Bangladesh Laws")
                            } else {
                                Toast.makeText(context, "পিডিএফ তৈরি ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(48.dp).testTag("pdf_section_${section.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "পিডিএফ তৈরি",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialog for adding / editing personal note
    if (showEditNoteDialog) {
        EditContentDialog(
            title = "ব্যক্তিগত নোট বা কেস রেফারেন্স",
            initialValue = section.userNotes ?: "",
            hint = "এই ধারা সম্পর্কিত আপনার নিজস্ব ব্যাখ্যা, আদালতের রায় বা আইনি নোট লিখুন...",
            onDismiss = { showEditNoteDialog = false },
            onSave = { updatedText ->
                onSavePersonalNote(updatedText)
                showEditNoteDialog = false
                Toast.makeText(context, "ব্যক্তিগত নোট সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog for editing simplified explanation
    if (showEditExplanationDialog) {
        EditContentDialog(
            title = "ধারার সহজ সরল ব্যাখ্যা এডিট করুন",
            initialValue = section.simplifiedExplanationBn,
            hint = "ধারার সাবলীল ব্যাখ্যা লিখুন...",
            onDismiss = { showEditExplanationDialog = false },
            onSave = { updatedExplanation ->
                onSaveExplanation(updatedExplanation)
                showEditExplanationDialog = false
                Toast.makeText(context, "ধারার ব্যাখ্যা আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun EditContentDialog(
    title: String,
    initialValue: String,
    hint: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(hint) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                maxLines = 10
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(text) },
                modifier = Modifier.testTag("dialog_save_btn")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
fun HighlightedText(
    text: String,
    query: String,
    modifier: Modifier = Modifier,
    highlightColor: Color = Color(0xFFFFD54F)
) {
    if (query.isBlank() || !text.contains(query, ignoreCase = true)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier
        )
        return
    }

    val annotatedString = buildAnnotatedString {
        var startIndex = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()

        while (startIndex < text.length) {
            val index = lowerText.indexOf(lowerQuery, startIndex)
            if (index == -1) {
                append(text.substring(startIndex))
                break
            }
            append(text.substring(startIndex, index))
            withStyle(
                style = SpanStyle(
                    background = highlightColor,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            ) {
                append(text.substring(index, index + query.length))
            }
            startIndex = index + query.length
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}
