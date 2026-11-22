package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.LawDao
import com.example.data.dao.SectionDao
import com.example.data.dao.UserDataDao
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.FavoriteEntity
import com.example.data.entity.LawCategoryCrossRef
import com.example.data.entity.LawEntity
import com.example.data.entity.PersonalNoteEntity
import com.example.data.entity.ReadingHistoryEntity
import com.example.data.entity.SectionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        LawEntity::class,
        CategoryEntity::class,
        LawCategoryCrossRef::class,
        ChapterEntity::class,
        SectionEntity::class,
        BookmarkEntity::class,
        FavoriteEntity::class,
        ReadingHistoryEntity::class,
        PersonalNoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lawDao(): LawDao
    abstract fun sectionDao(): SectionDao
    abstract fun userDataDao(): UserDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bangladesh_laws.db"
                )
                    .addCallback(DatabaseCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getInstance(context)
                    populateDatabase(database)
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Ensure data is seeded even if database was created empty
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getInstance(context)
                    val lawDao = database.lawDao()
                    if (lawDao.getLawByIdDirect(1) == null) {
                        populateDatabase(database)
                    }
                }
            }

            private suspend fun populateDatabase(database: AppDatabase) {
                val lawDao = database.lawDao()
                val sectionDao = database.sectionDao()

                lawDao.insertCategories(LawPreloader.getInitialCategories())
                lawDao.insertLaws(LawPreloader.getInitialLaws())
                lawDao.insertCategoryCrossRefs(LawPreloader.getInitialLawCategoryCrossRefs())
                lawDao.insertChapters(LawPreloader.getInitialChapters())
                sectionDao.insertSections(LawPreloader.getInitialSections())
            }
        }
    }
}
