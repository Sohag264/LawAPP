package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.LawPreloader
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.PersonalNoteEntity
import com.example.data.repository.LawRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LawDatabaseRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: LawRepository

    @Before
    fun createDb() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val lawDao = db.lawDao()
        val sectionDao = db.sectionDao()
        val userDataDao = db.userDataDao()

        lawDao.insertCategories(LawPreloader.getInitialCategories())
        lawDao.insertLaws(LawPreloader.getInitialLaws())
        lawDao.insertCategoryCrossRefs(LawPreloader.getInitialLawCategoryCrossRefs())
        lawDao.insertChapters(LawPreloader.getInitialChapters())
        sectionDao.insertSections(LawPreloader.getInitialSections())

        repository = LawRepository(lawDao, sectionDao, userDataDao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testDatabaseSeeding() = runBlocking {
        val laws = repository.allLaws.first()
        assertTrue("Laws should be populated", laws.isNotEmpty())
        assertTrue("At least 10 laws should be seeded", laws.size >= 10)

        val categories = repository.allCategories.first()
        assertEquals(20, categories.size)

        val penalCode = repository.getLawByIdDirect(2)
        assertNotNull(penalCode)
        assertEquals("The Penal Code, 1860", penalCode?.titleEn)
    }

    @Test
    fun testSectionQueryAndSearch() = runBlocking {
        val sec302 = repository.getSectionByIdDirect(205)
        assertNotNull("Section 302 should exist", sec302)
        assertEquals("৩০২", sec302?.sectionNumberBn)
        assertEquals("302", sec302?.sectionNumberEn)
        assertEquals("খুনের শাস্তি (Punishment for murder)", sec302?.titleBn)

        // Search with English number "302"
        val searchResultEn = repository.search("302").first()
        assertTrue("Searching '302' should find Section 302", searchResultEn.sections.any { it.sectionNumberEn == "302" })

        // Search with Bengali number "৩০২"
        val searchResultBn = repository.search("৩০২").first()
        assertTrue("Searching '৩০২' should find Section 302", searchResultBn.sections.any { it.sectionNumberBn == "৩০২" })

        // Search by keyword "চুরি"
        val searchResultTheft = repository.search("চুরি").first()
        assertTrue("Searching 'চুরি' should return sections", searchResultTheft.sections.isNotEmpty())
    }

    @Test
    fun testBookmarkAndNoteOperations() = runBlocking {
        // Toggle bookmark
        repository.toggleBookmark(lawId = 2, sectionId = 205, currentlyBookmarked = false)
        val isBookmarked = repository.isSectionBookmarked(205).first()
        assertTrue("Section 205 should be bookmarked", isBookmarked)

        // Save personal note
        val testNote = "ধারা ৩০২ এর মামলায় মোটিভ এবং ময়নাতদন্ত রিপোর্ট অত্যন্ত গুরুত্বপূর্ণ উপাদান।"
        repository.savePersonalNote(sectionId = 205, lawId = 2, noteText = testNote)

        val savedNote = repository.getPersonalNote(205).first()
        assertNotNull("Personal note should be saved", savedNote)
        assertEquals(testNote, savedNote?.noteText)

        // Export and restore backup
        val jsonBackup = repository.exportUserBackupJson()
        assertTrue("Backup JSON should contain bookmarks", jsonBackup.contains("bookmarks"))
        assertTrue("Backup JSON should contain notes", jsonBackup.contains("notes"))

        val restoreSuccess = repository.restoreUserBackupJson(jsonBackup)
        assertTrue("Backup restoration should succeed", restoreSuccess)
    }
}
