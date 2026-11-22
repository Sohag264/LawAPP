package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.SectionEntity
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SectionEntity>)

    @Query("UPDATE sections SET simplifiedExplanationBn = :explanation WHERE id = :sectionId")
    suspend fun updateSectionExplanation(sectionId: Long, explanation: String)

    @Query("UPDATE sections SET userNotes = :notes WHERE id = :sectionId")
    suspend fun updateUserNotes(sectionId: Long, notes: String?)

    @Query("""
        SELECT * FROM sections
        WHERE sectionNumberBn = :queryBn
           OR sectionNumberEn = :queryEn
           OR sectionNumberBn LIKE '%' || :queryBn || '%'
           OR sectionNumberEn LIKE '%' || :queryEn || '%'
           OR titleBn LIKE '%' || :query || '%'
           OR titleEn LIKE '%' || :query || '%'
           OR fullContentBn LIKE '%' || :query || '%'
           OR simplifiedExplanationBn LIKE '%' || :query || '%'
        ORDER BY orderIndex ASC
        LIMIT 100
    """)
    fun searchSections(query: String, queryBn: String, queryEn: String): Flow<List<SectionEntity>>

    @Query("""
        SELECT * FROM sections
        WHERE lawId = :lawId AND (
           sectionNumberBn = :queryBn
           OR sectionNumberEn = :queryEn
           OR sectionNumberBn LIKE '%' || :queryBn || '%'
           OR sectionNumberEn LIKE '%' || :queryEn || '%'
           OR titleBn LIKE '%' || :query || '%'
           OR titleEn LIKE '%' || :query || '%'
           OR fullContentBn LIKE '%' || :query || '%'
           OR simplifiedExplanationBn LIKE '%' || :query || '%'
        )
        ORDER BY orderIndex ASC
    """)
    fun searchSectionsInLaw(lawId: Long, query: String, queryBn: String, queryEn: String): Flow<List<SectionEntity>>
}
