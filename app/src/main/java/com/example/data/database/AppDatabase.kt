package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.LawDao
import com.example.data.dao.SectionDao
import com.example.data.dao.UserDataDao
import com.example.data.dao.VerificationDao
import com.example.data.entity.AmendmentEntity
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.DatabaseMetadataEntity
import com.example.data.entity.FavoriteEntity
import com.example.data.entity.FootnoteEntity
import com.example.data.entity.LawCategoryCrossRef
import com.example.data.entity.LawEntity
import com.example.data.entity.LawVerificationReportEntity
import com.example.data.entity.PersonalNoteEntity
import com.example.data.entity.ReadingHistoryEntity
import com.example.data.entity.ScheduleEntity
import com.example.data.entity.SectionEntity
import com.example.data.entity.SubsectionEntity
import com.example.data.verification.LawVerificationEngine
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
        SubsectionEntity::class,
        FootnoteEntity::class,
        ScheduleEntity::class,
        AmendmentEntity::class,
        DatabaseMetadataEntity::class,
        LawVerificationReportEntity::class,
        BookmarkEntity::class,
        FavoriteEntity::class,
        ReadingHistoryEntity::class,
        PersonalNoteEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lawDao(): LawDao
    abstract fun sectionDao(): SectionDao
    abstract fun userDataDao(): UserDataDao
    abstract fun verificationDao(): VerificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Upgrade laws table safely without dropping user data
                db.execSQL("ALTER TABLE laws ADD COLUMN extractionStatus TEXT NOT NULL DEFAULT 'PARTIAL'")
                db.execSQL("ALTER TABLE laws ADD COLUMN verificationStatus TEXT NOT NULL DEFAULT 'INCOMPLETE'")
                db.execSQL("ALTER TABLE laws ADD COLUMN lastVerifiedAt INTEGER")
                db.execSQL("ALTER TABLE laws ADD COLUMN sourceContentHash TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_laws_extractionStatus ON laws(extractionStatus)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_laws_verificationStatus ON laws(verificationStatus)")

                // Upgrade chapters indices
                db.execSQL("CREATE INDEX IF NOT EXISTS index_chapters_lawId_orderIndex ON chapters(lawId, orderIndex)")

                // Upgrade sections table safely
                db.execSQL("ALTER TABLE sections ADD COLUMN originalHtml TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sections ADD COLUMN contentHash TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sections ADD COLUMN sourceUrl TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sections ADD COLUMN extractionStatus TEXT NOT NULL DEFAULT 'COMPLETE'")
                db.execSQL("ALTER TABLE sections ADD COLUMN verificationStatus TEXT NOT NULL DEFAULT 'UNVERIFIED'")
                db.execSQL("ALTER TABLE sections ADD COLUMN isRepealed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sections ADD COLUMN isAmended INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sections ADD COLUMN amendmentNote TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sections_lawId_orderIndex ON sections(lawId, orderIndex)")

                // Create subsections table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS subsections (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sectionId INTEGER NOT NULL,
                        parentSubsectionId INTEGER,
                        subsectionNumber TEXT NOT NULL,
                        nodeType TEXT NOT NULL,
                        subsectionText TEXT NOT NULL,
                        sequenceOrder INTEGER NOT NULL,
                        FOREIGN KEY(sectionId) REFERENCES sections(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subsections_sectionId ON subsections(sectionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subsections_parentSubsectionId ON subsections(parentSubsectionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subsections_sectionId_sequenceOrder ON subsections(sectionId, sequenceOrder)")

                // Create footnotes table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS footnotes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sectionId INTEGER NOT NULL,
                        lawId INTEGER NOT NULL,
                        footnoteMarker TEXT NOT NULL,
                        footnoteText TEXT NOT NULL,
                        sequenceOrder INTEGER NOT NULL,
                        FOREIGN KEY(sectionId) REFERENCES sections(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_footnotes_sectionId ON footnotes(sectionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_footnotes_lawId ON footnotes(lawId)")

                // Create schedules table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS schedules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        lawId INTEGER NOT NULL,
                        scheduleNumber TEXT NOT NULL,
                        title TEXT NOT NULL,
                        contentText TEXT NOT NULL,
                        originalHtml TEXT NOT NULL,
                        sequenceOrder INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_schedules_lawId ON schedules(lawId)")

                // Create amendments table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS amendments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        lawId INTEGER NOT NULL,
                        sectionId INTEGER,
                        amendingActName TEXT NOT NULL,
                        amendingActNumber TEXT NOT NULL,
                        amendmentYear INTEGER NOT NULL,
                        summaryBn TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_amendments_lawId ON amendments(lawId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_amendments_sectionId ON amendments(sectionId)")

                // Create database_metadata table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS database_metadata (
                        `key` TEXT NOT NULL PRIMARY KEY,
                        value TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create verification_reports table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS verification_reports (
                        lawId INTEGER NOT NULL PRIMARY KEY,
                        expectedSectionsCount INTEGER NOT NULL,
                        actualSectionsCount INTEGER NOT NULL,
                        completeSectionsCount INTEGER NOT NULL,
                        incompleteSectionsCount INTEGER NOT NULL,
                        repealedSectionsCount INTEGER NOT NULL,
                        missingSectionsCount INTEGER NOT NULL,
                        missingSectionsList TEXT NOT NULL,
                        verifiedSubsectionsCount INTEGER NOT NULL,
                        incompleteSubsectionsCount INTEGER NOT NULL,
                        duplicateSectionsList TEXT NOT NULL,
                        parsingErrorsList TEXT NOT NULL,
                        isComplete INTEGER NOT NULL,
                        verifiedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE verification_reports ADD COLUMN checkedSectionNumbersCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE verification_reports ADD COLUMN verificationPendingCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE verification_reports ADD COLUMN incompleteSectionsList TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bangladesh_laws.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(DatabaseCallback(context))
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
                    populateAndVerifyDatabase(database)
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getInstance(context)
                    val lawDao = database.lawDao()
                    val sectionDao = database.sectionDao()
                    val verificationDao = database.verificationDao()
                    val schemaMeta = verificationDao.getMetadata("schema_version")?.value
                    if (lawDao.getLawByIdDirect(1) == null ||
                        sectionDao.getSubsectionCountDirect() == 0 ||
                        verificationDao.getAllVerificationReportsDirect().isEmpty() ||
                        schemaMeta != "3"
                    ) {
                        populateAndVerifyDatabase(database)
                    }
                }
            }

            private suspend fun populateAndVerifyDatabase(database: AppDatabase) {
                database.withTransaction {
                    val lawDao = database.lawDao()
                    val sectionDao = database.sectionDao()
                    val verificationDao = database.verificationDao()

                    if (lawDao.getLawByIdDirect(1) == null) {
                        lawDao.insertCategories(LawPreloader.getInitialCategories())
                        lawDao.insertLaws(LawPreloader.getInitialLaws())
                        lawDao.insertCategoryCrossRefs(LawPreloader.getInitialLawCategoryCrossRefs())
                        lawDao.insertChapters(LawPreloader.getInitialChapters())
                        val sections = LawPreloader.getInitialSections()
                        sectionDao.insertSections(sections)
                        sectionDao.insertSubsections(LawPreloader.extractSubsectionsForSections(sections))
                    } else {
                        // Ensure all 54 initial chapters (including all 25 Penal Code chapters) & subsections exist without overwriting user notes or imported sections
                        lawDao.insertChapters(LawPreloader.getInitialChapters())
                        val existingSections = LawPreloader.getInitialSections()
                        val existingById = existingSections.associateBy { it.id }
                        if (sectionDao.getSubsectionCountDirect() == 0) {
                            val currentDbSections = lawDao.getAllLawsDirect().flatMap {
                                sectionDao.getSectionsForLawDirect(it.id)
                            }.map { dbSec ->
                                val template = existingById[dbSec.id]
                                if (template != null) {
                                    dbSec.copy(
                                        chapterId = template.chapterId,
                                        contentHash = template.contentHash,
                                        sourceUrl = template.sourceUrl,
                                        extractionStatus = template.extractionStatus
                                    )
                                } else dbSec
                            }
                            sectionDao.insertSections(currentDbSections)
                            sectionDao.insertSubsections(LawPreloader.extractSubsectionsForSections(currentDbSections))
                        }
                    }

                    verificationDao.setMetadata(
                        DatabaseMetadataEntity(
                            key = "schema_version",
                            value = "3",
                            updatedAt = System.currentTimeMillis()
                        )
                    )

                    val engine = LawVerificationEngine(lawDao, sectionDao, verificationDao)
                    engine.verifyAllLaws()
                }
            }
        }
    }
}
