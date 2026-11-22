package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "laws",
    indices = [
        Index(value = ["actYear"]),
        Index(value = ["titleBn"]),
        Index(value = ["titleEn"])
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
    val isConstitution: Boolean = false
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
    indices = [Index(value = ["lawId"])]
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
        Index(value = ["titleEn"])
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
    val orderIndex: Int = 0
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
    val sectionId: Long? = null, // null means whole law is favorited
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
