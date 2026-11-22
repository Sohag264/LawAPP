package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.LawCategoryCrossRef
import com.example.data.entity.LawEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LawDao {
    @Query("SELECT * FROM laws ORDER BY isConstitution DESC, actYear ASC, titleBn ASC")
    fun getAllLaws(): Flow<List<LawEntity>>

    @Query("SELECT * FROM laws ORDER BY isConstitution DESC, actYear ASC, titleBn ASC")
    suspend fun getAllLawsDirect(): List<LawEntity>

    @Query("SELECT * FROM laws WHERE isPopular = 1 OR isConstitution = 1 ORDER BY isConstitution DESC, actYear ASC")
    fun getPopularLaws(): Flow<List<LawEntity>>

    @Query("SELECT * FROM laws WHERE id = :lawId LIMIT 1")
    fun getLawById(lawId: Long): Flow<LawEntity?>

    @Query("SELECT * FROM laws WHERE id = :lawId LIMIT 1")
    suspend fun getLawByIdDirect(lawId: Long): LawEntity?

    @Query("""
        SELECT laws.* FROM laws
        INNER JOIN law_category_cross_ref ON laws.id = law_category_cross_ref.lawId
        WHERE law_category_cross_ref.categoryId = :categoryId
        ORDER BY laws.isConstitution DESC, laws.actYear ASC
    """)
    fun getLawsByCategory(categoryId: Long): Flow<List<LawEntity>>

    @Query("SELECT * FROM categories ORDER BY displayOrder ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    fun getCategoryById(categoryId: Long): Flow<CategoryEntity?>

    @Query("SELECT * FROM chapters WHERE lawId = :lawId ORDER BY orderIndex ASC")
    fun getChaptersForLaw(lawId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE lawId = :lawId ORDER BY orderIndex ASC")
    suspend fun getChaptersForLawDirect(lawId: Long): List<ChapterEntity>

    @Query("SELECT COUNT(*) FROM laws")
    fun getLawCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM laws")
    suspend fun getLawCountDirect(): Int

    @Query("SELECT COUNT(*) FROM chapters")
    fun getChapterCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM chapters")
    suspend fun getChapterCountDirect(): Int

    @Query("SELECT COUNT(*) FROM categories")
    fun getCategoryCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaw(law: LawEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaws(laws: List<LawEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategoryCrossRefs(refs: List<LawCategoryCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("DELETE FROM chapters WHERE lawId = :lawId")
    suspend fun deleteChaptersForLaw(lawId: Long)

    @Query("""
        UPDATE laws SET 
            extractionStatus = :extractionStatus, 
            verificationStatus = :verificationStatus, 
            lastVerifiedAt = :verifiedAt,
            sourceContentHash = :sourceHash
        WHERE id = :lawId
    """)
    suspend fun updateLawVerification(
        lawId: Long,
        extractionStatus: String,
        verificationStatus: String,
        verifiedAt: Long,
        sourceHash: String?
    )

    @Query("""
        SELECT * FROM laws
        WHERE titleBn LIKE '%' || :query || '%'
           OR titleEn LIKE '%' || :query || '%'
           OR shortDescriptionBn LIKE '%' || :query || '%'
           OR actNumber LIKE '%' || :query || '%'
           OR CAST(actYear AS TEXT) LIKE '%' || :queryBn || '%'
           OR CAST(actYear AS TEXT) LIKE '%' || :queryEn || '%'
        ORDER BY isConstitution DESC, actYear ASC
    """)
    fun searchLaws(query: String, queryBn: String, queryEn: String): Flow<List<LawEntity>>
}
