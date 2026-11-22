package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object ExtractionStatus {
    const val COMPLETE = "COMPLETE"
    const val PARTIAL = "PARTIAL"
    const val NOT_EXTRACTED = "NOT_EXTRACTED"
    const val FAILED = "FAILED"
}

object VerificationStatus {
    const val VERIFIED = "VERIFIED"
    const val INCOMPLETE = "INCOMPLETE"
    const val MISMATCH = "MISMATCH"
    const val UNVERIFIED = "UNVERIFIED"
    const val VERIFICATION_PENDING = "VERIFICATION_PENDING"
    const val OCR_UNVERIFIED = "OCR_UNVERIFIED"
}

object SubsectionNodeType {
    const val SUBSECTION = "SUBSECTION"
    const val CLAUSE = "CLAUSE"
    const val SUBCLAUSE = "SUBCLAUSE"
    const val PROVISO = "PROVISO"
    const val EXPLANATION = "EXPLANATION"
    const val EXCEPTION = "EXCEPTION"
    const val ILLUSTRATION = "ILLUSTRATION"
}

@Entity(
    tableName = "laws",
    indices = [
        Index(value = ["actYear"]),
        Index(value = ["titleBn"]),
        Index(value = ["titleEn"]),
        Index(value = ["extractionStatus"]),
        Index(value = ["verificationStatus"])
    ]
)
data class LawEntity(
    @PrimaryKey val id: Long,
    val actNumber: String,
    val actYear: Int,
    val titleBn: String,
    val titleEn: String,
    val shortDescriptionBn: String,
    val fullIntroductionBn: String,
    val sourceUrl: String,
    val status: String = "কার্যকর (In Force)",
    val passedDate: String = "",
    val effectiveDate: String = "",
    val totalSections: Int = 0,
    val isPopular: Boolean = false,
    val isConstitution: Boolean = false,
    val extractionStatus: String = ExtractionStatus.PARTIAL,
    val verificationStatus: String = VerificationStatus.INCOMPLETE,
    val lastVerifiedAt: Long? = null,
    val sourceContentHash: String? = null
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Long,
    val nameBn: String,
    val nameEn: String,
    val iconName: String,
    val displayOrder: Int
)

@Entity(
    tableName = "law_category_cross_ref",
    primaryKeys = ["lawId", "categoryId"],
    indices = [Index(value = ["categoryId"]), Index(value = ["lawId"])]
)
data class LawCategoryCrossRef(
    val lawId: Long,
    val categoryId: Long
)

@Entity(
    tableName = "chapters",
    indices = [Index(value = ["lawId"]), Index(value = ["lawId", "orderIndex"])]
)
data class ChapterEntity(
    @PrimaryKey val id: Long,
    val lawId: Long,
    val chapterNumberBn: String,
    val chapterNumberEn: String,
    val titleBn: String,
    val titleEn: String,
    val orderIndex: Int
)

@Entity(
    tableName = "sections",
    indices = [
        Index(value = ["lawId"]),
        Index(value = ["chapterId"]),
        Index(value = ["sectionNumberBn"]),
        Index(value = ["sectionNumberEn"]),
        Index(value = ["titleBn"]),
        Index(value = ["titleEn"]),
        Index(value = ["lawId", "orderIndex"])
    ]
)
data class SectionEntity(
    @PrimaryKey val id: Long,
    val lawId: Long,
    val chapterId: Long,
    val sectionNumberBn: String,
    val sectionNumberEn: String,
    val titleBn: String,
    val titleEn: String,
    val fullContentBn: String,
    val fullContentEn: String = "",
    val simplifiedExplanationBn: String = "",
    val userNotes: String? = null,
    val punishmentSummaryBn: String = "",
    val bailableStatus: String = "",
    val cognizableStatus: String = "",
    val trialCourt: String = "",
    val orderIndex: Int = 0,
    val originalHtml: String = "",
    val contentHash: String = "",
    val sourceUrl: String = "",
    val extractionStatus: String = ExtractionStatus.COMPLETE,
    val verificationStatus: String = VerificationStatus.UNVERIFIED,
    val isRepealed: Boolean = false,
    val isAmended: Boolean = false,
    val amendmentNote: String = ""
)

@Entity(
    tableName = "subsections",
    foreignKeys = [
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sectionId"]),
        Index(value = ["parentSubsectionId"]),
        Index(value = ["sectionId", "sequenceOrder"])
    ]
)
data class SubsectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectionId: Long,
    val parentSubsectionId: Long? = null,
    val subsectionNumber: String,
    val nodeType: String = SubsectionNodeType.SUBSECTION,
    val subsectionText: String,
    val sequenceOrder: Int = 0
)

@Entity(
    tableName = "footnotes",
    foreignKeys = [
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sectionId"]),
        Index(value = ["lawId"])
    ]
)
data class FootnoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectionId: Long,
    val lawId: Long,
    val footnoteMarker: String,
    val footnoteText: String,
    val sequenceOrder: Int = 0
)

@Entity(
    tableName = "schedules",
    indices = [Index(value = ["lawId"])]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lawId: Long,
    val scheduleNumber: String,
    val title: String,
    val contentText: String,
    val originalHtml: String = "",
    val sequenceOrder: Int = 0
)

@Entity(
    tableName = "amendments",
    indices = [Index(value = ["lawId"]), Index(value = ["sectionId"])]
)
data class AmendmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lawId: Long,
    val sectionId: Long? = null,
    val amendingActName: String,
    val amendingActNumber: String,
    val amendmentYear: Int,
    val summaryBn: String
)

@Entity(tableName = "database_metadata")
data class DatabaseMetadataEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "verification_reports")
data class LawVerificationReportEntity(
    @PrimaryKey val lawId: Long,
    val expectedSectionsCount: Int,
    val checkedSectionNumbersCount: Int = expectedSectionsCount,
    val actualSectionsCount: Int,
    val completeSectionsCount: Int,
    val incompleteSectionsCount: Int,
    val repealedSectionsCount: Int,
    val verificationPendingCount: Int = 0,
    val missingSectionsCount: Int,
    val missingSectionsList: String,
    val incompleteSectionsList: String = "",
    val verifiedSubsectionsCount: Int,
    val incompleteSubsectionsCount: Int,
    val duplicateSectionsList: String,
    val parsingErrorsList: String,
    val isComplete: Boolean,
    val verifiedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["sectionId"], unique = true)]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lawId: Long,
    val sectionId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(
    tableName = "favorites",
    indices = [Index(value = ["lawId", "sectionId"], unique = true)]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lawId: Long,
    val sectionId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reading_history",
    indices = [Index(value = ["sectionId"])]
)
data class ReadingHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lawId: Long,
    val sectionId: Long,
    val viewedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "personal_notes",
    indices = [Index(value = ["sectionId"], unique = true)]
)
data class PersonalNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectionId: Long,
    val lawId: Long,
    val noteText: String,
    val updatedAt: Long = System.currentTimeMillis()
)
