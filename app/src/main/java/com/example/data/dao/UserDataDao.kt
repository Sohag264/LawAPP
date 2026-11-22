package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.FavoriteEntity
import com.example.data.entity.PersonalNoteEntity
import com.example.data.entity.ReadingHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {
    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE sectionId = :sectionId LIMIT 1)")
    fun isSectionBookmarked(sectionId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE sectionId = :sectionId LIMIT 1)")
    suspend fun isSectionBookmarkedDirect(sectionId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE sectionId = :sectionId")
    suspend fun removeBookmark(sectionId: Long)

    @Query("SELECT COUNT(*) FROM bookmarks")
    fun getBookmarkCount(): Flow<Int>

    // Favorites
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE lawId = :lawId AND sectionId IS NULL LIMIT 1)")
    fun isLawFavorite(lawId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE lawId = :lawId AND sectionId = :sectionId LIMIT 1)")
    fun isSectionFavorite(lawId: Long, sectionId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE lawId = :lawId AND sectionId IS NULL")
    suspend fun removeLawFavorite(lawId: Long)

    @Query("DELETE FROM favorites WHERE lawId = :lawId AND sectionId = :sectionId")
    suspend fun removeSectionFavorite(lawId: Long, sectionId: Long)

    @Query("SELECT COUNT(*) FROM favorites")
    fun getFavoriteCount(): Flow<Int>

    // Reading History
    @Query("SELECT * FROM reading_history ORDER BY viewedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 30): Flow<List<ReadingHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHistory(history: ReadingHistoryEntity)

    @Query("DELETE FROM reading_history")
    suspend fun clearHistory()

    // Personal Notes
    @Query("SELECT * FROM personal_notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<PersonalNoteEntity>>

    @Query("SELECT * FROM personal_notes WHERE sectionId = :sectionId LIMIT 1")
    fun getNoteForSection(sectionId: Long): Flow<PersonalNoteEntity?>

    @Query("SELECT * FROM personal_notes WHERE sectionId = :sectionId LIMIT 1")
    suspend fun getNoteForSectionDirect(sectionId: Long): PersonalNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNote(note: PersonalNoteEntity)

    @Query("DELETE FROM personal_notes WHERE sectionId = :sectionId")
    suspend fun deleteNote(sectionId: Long)

    @Query("SELECT COUNT(*) FROM personal_notes")
    fun getNoteCount(): Flow<Int>

    // User Data Backup and Restore
    @Query("SELECT * FROM personal_notes")
    suspend fun getAllNotesDirect(): List<PersonalNoteEntity>

    @Query("SELECT * FROM bookmarks")
    suspend fun getAllBookmarksDirect(): List<BookmarkEntity>

    @Query("SELECT * FROM favorites")
    suspend fun getAllFavoritesDirect(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNotes(notes: List<PersonalNoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBookmarks(bookmarks: List<BookmarkEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFavorites(favorites: List<FavoriteEntity>)
}
