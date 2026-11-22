package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.FootnoteEntity
import com.example.data.entity.ScheduleEntity
import com.example.data.entity.SectionEntity
import com.example.data.entity.SubsectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {
    @Query("SELECT * FROM sections WHERE lawId = :lawId ORDER BY orderIndex ASC, id ASC")
    fun getSectionsForLaw(lawId: Long): Flow<List<SectionEntity>>

    @Query("SELECT * FROM sections WHERE lawId = :lawId ORDER BY orderIndex ASC, id ASC")
    suspend fun getSectionsForLawDirect(lawId: Long): List<SectionEntity>

    @Query("SELECT * FROM sections WHERE chapterId = :chapterId ORDER BY orderIndex ASC, id ASC")
    fun getSectionsForChapter(chapterId: Long): Flow<List<SectionEntity>>

    @Query("SELECT * FROM sections WHERE id = :sectionId LIMIT 1")
    fun getSectionById(sectionId: Long): Flow<SectionEntity?>

    @Query("SELECT * FROM sections WHERE id = :sectionId LIMIT 1")
    suspend fun getSectionByIdDirect(sectionId: Long): SectionEntity?

    @Query("SELECT * FROM sections WHERE id IN (:sectionIds)")
    fun getSectionsByIds(sectionIds: List<Long>): Flow<List<SectionEntity>>

    @Query("SELECT COUNT(*) FROM sections")
    fun getSectionCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sections")
    suspend fun getSectionCountDirect(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SectionEntity>)

    @Query("DELETE FROM sections WHERE lawId = :lawId")
    suspend fun deleteSectionsForLaw(lawId: Long)

    @Query("UPDATE sections SET simplifiedExplanationBn = :explanation WHERE id = :sectionId")
    suspend fun updateSectionExplanation(sectionId: Long, explanation: String)

    @Query("UPDATE sections SET userNotes = :notes WHERE id = :sectionId")
    suspend fun updateUserNotes(sectionId: Long, notes: String?)

    // Subsections
    @Query("SELECT * FROM subsections WHERE sectionId = :sectionId ORDER BY sequenceOrder ASC, id ASC")
    fun getSubsectionsForSection(sectionId: Long): Flow<List<SubsectionEntity>>

    @Query("SELECT * FROM subsections WHERE sectionId = :sectionId ORDER BY sequenceOrder ASC, id ASC")
    suspend fun getSubsectionsForSectionDirect(sectionId: Long): List<SubsectionEntity>

    @Query("SELECT * FROM subsections WHERE sectionId IN (SELECT id FROM sections WHERE lawId = :lawId) ORDER BY sequenceOrder ASC, id ASC")
    fun getSubsectionsForLaw(lawId: Long): Flow<List<SubsectionEntity>>

    @Query("SELECT * FROM subsections WHERE sectionId IN (SELECT id FROM sections WHERE lawId = :lawId) ORDER BY sequenceOrder ASC, id ASC")
    suspend fun getSubsectionsForLawDirect(lawId: Long): List<SubsectionEntity>

    @Query("SELECT COUNT(*) FROM subsections")
    fun getSubsectionCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM subsections")
    suspend fun getSubsectionCountDirect(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubsection(subsection: SubsectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubsections(subsections: List<SubsectionEntity>)

    @Query("DELETE FROM subsections WHERE sectionId = :sectionId")
    suspend fun deleteSubsectionsForSection(sectionId: Long)

    // Footnotes
    @Query("SELECT * FROM footnotes WHERE sectionId = :sectionId ORDER BY sequenceOrder ASC, id ASC")
    fun getFootnotesForSection(sectionId: Long): Flow<List<FootnoteEntity>>

    @Query("SELECT * FROM footnotes WHERE lawId = :lawId ORDER BY sequenceOrder ASC, id ASC")
    fun getFootnotesForLaw(lawId: Long): Flow<List<FootnoteEntity>>

    @Query("SELECT * FROM footnotes WHERE sectionId = :sectionId ORDER BY sequenceOrder ASC, id ASC")
    suspend fun getFootnotesForSectionDirect(sectionId: Long): List<FootnoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFootnotes(footnotes: List<FootnoteEntity>)

    @Query("DELETE FROM footnotes WHERE sectionId = :sectionId")
    suspend fun deleteFootnotesForSection(sectionId: Long)

    // Schedules
    @Query("SELECT * FROM schedules WHERE lawId = :lawId ORDER BY sequenceOrder ASC")
    fun getSchedulesForLaw(lawId: Long): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Query("DELETE FROM schedules WHERE lawId = :lawId")
    suspend fun deleteSchedulesForLaw(lawId: Long)

    // Full-text indexed search across sections AND subsections
    @Query("""
        SELECT DISTINCT s.* FROM sections s
        LEFT JOIN subsections sub ON sub.sectionId = s.id
        WHERE s.sectionNumberBn = :queryBn
           OR s.sectionNumberEn = :queryEn
           OR s.sectionNumberBn LIKE '%' || :queryBn || '%'
           OR s.sectionNumberEn LIKE '%' || :queryEn || '%'
           OR s.titleBn LIKE '%' || :query || '%'
           OR s.titleEn LIKE '%' || :query || '%'
           OR s.fullContentBn LIKE '%' || :query || '%'
           OR s.simplifiedExplanationBn LIKE '%' || :query || '%'
           OR sub.subsectionText LIKE '%' || :query || '%'
           OR sub.subsectionNumber LIKE '%' || :queryBn || '%'
           OR sub.subsectionNumber LIKE '%' || :queryEn || '%'
        ORDER BY s.orderIndex ASC
        LIMIT 120
    """)
    fun searchSections(query: String, queryBn: String, queryEn: String): Flow<List<SectionEntity>>

    @Query("""
        SELECT DISTINCT s.* FROM sections s
        LEFT JOIN subsections sub ON sub.sectionId = s.id
        WHERE s.lawId = :lawId AND (
           s.sectionNumberBn = :queryBn
           OR s.sectionNumberEn = :queryEn
           OR s.sectionNumberBn LIKE '%' || :queryBn || '%'
           OR s.sectionNumberEn LIKE '%' || :queryEn || '%'
           OR s.titleBn LIKE '%' || :query || '%'
           OR s.titleEn LIKE '%' || :query || '%'
           OR s.fullContentBn LIKE '%' || :query || '%'
           OR s.simplifiedExplanationBn LIKE '%' || :query || '%'
           OR sub.subsectionText LIKE '%' || :query || '%'
        )
        ORDER BY s.orderIndex ASC
    """)
    fun searchSectionsInLaw(lawId: Long, query: String, queryBn: String, queryEn: String): Flow<List<SectionEntity>>
}
