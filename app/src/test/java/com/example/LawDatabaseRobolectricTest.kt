package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.LawPreloader
import com.example.data.entity.ExtractionStatus
import com.example.data.entity.LawEntity
import com.example.data.entity.SectionEntity
import com.example.data.entity.SubsectionNodeType
import com.example.data.entity.VerificationStatus
import com.example.data.importer.BdlawsHtmlParser
import com.example.data.importer.LawRecoveryManager
import com.example.data.importer.LegalTextParser
import com.example.data.repository.LawRepository
import com.example.data.verification.LawVerificationEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LawDatabaseRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: LawRepository

    @Before
    fun createDb() {
        runBlocking {
            context = ApplicationProvider.getApplicationContext()
            db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()

            val lawDao = db.lawDao()
            val sectionDao = db.sectionDao()
            val userDataDao = db.userDataDao()
            val verificationDao = db.verificationDao()

            lawDao.insertCategories(LawPreloader.getInitialCategories())
            lawDao.insertLaws(LawPreloader.getInitialLaws())
            lawDao.insertCategoryCrossRefs(LawPreloader.getInitialLawCategoryCrossRefs())
            lawDao.insertChapters(LawPreloader.getInitialChapters())
            val initialSections = LawPreloader.getInitialSections()
            sectionDao.insertSections(initialSections)
            sectionDao.insertSubsections(LawPreloader.extractSubsectionsForSections(initialSections))

            repository = LawRepository(lawDao, sectionDao, userDataDao, verificationDao)
        }
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testDatabaseSeedingAndVerificationHonesty() {
        runBlocking {
            val laws = repository.allLaws.first()
            assertTrue("Laws should be populated", laws.isNotEmpty())
            assertEquals(12, laws.size)

            val categories = repository.allCategories.first()
            assertEquals(20, categories.size)

            val penalCode = repository.getLawByIdDirect(2)
            assertNotNull(penalCode)
            assertEquals("The Penal Code, 1860", penalCode?.titleEn)

            // Verify chapter foreign keys and all 25 Penal Code chapters exist (54 total across 12 laws)
            val chaptersCount = db.lawDao().getChapterCountDirect()
            assertTrue("All 54 chapters including 25 Penal Code chapters should exist", chaptersCount >= 54)
            val penalChapters = db.lawDao().getChaptersForLawDirect(2)
            assertEquals("Penal Code 1860 should have all 25 official chapters", 25, penalChapters.size)

            // Run real verification engine
            val report = repository.verifyDatabaseIntegrity()
            assertEquals(12, report.lawCount)
            assertTrue("Preloaded laws are partial so incompleteLawCount must be > 0", report.incompleteLawCount > 0)
            assertTrue("Preloaded laws have missing sections so missingSectionsCount must be > 0", report.missingSectionsCount > 0)

            val penalReport = repository.getVerificationReportForLaw(2).first()
            assertNotNull(penalReport)
            assertEquals(511, penalReport?.expectedSectionsCount)
            assertEquals(13, penalReport?.actualSectionsCount)
            assertTrue("Abbreviated sections 300 & 499 should be flagged incomplete", (penalReport?.incompleteSectionsCount ?: 0) >= 2)
            assertTrue("Verification pending count must be > 0 for unverified preloaded data", (penalReport?.verificationPendingCount ?: 0) > 0)
            assertFalse("Penal code with 13/511 sections must report isComplete=false", penalReport?.isComplete ?: true)
        }
    }

    @Test
    fun testSectionQueryAndBilingualSearchIncludingSubsections() {
        runBlocking {
            val sec302 = repository.getSectionByIdDirect(205)
            assertNotNull("Section 302 should exist", sec302)
            assertEquals("৩০২", sec302?.sectionNumberBn)
            assertEquals("302", sec302?.sectionNumberEn)
            assertEquals("খুনের শাস্তি (Punishment for murder)", sec302?.titleBn)

            val searchResultEn = repository.search("302").first()
            assertTrue("Searching '302' should find Section 302", searchResultEn.sections.any { it.sectionNumberEn == "302" })

            val searchResultBn = repository.search("৩০২").first()
            assertTrue("Searching '৩০২' should find Section 302", searchResultBn.sections.any { it.sectionNumberBn == "৩০২" })

            val searchResultTheft = repository.search("চুরি").first()
            assertTrue("Searching 'চুরি' should return sections", searchResultTheft.sections.isNotEmpty())
        }
    }

    @Test
    fun testBookmarkAndNoteOperations() {
        runBlocking {
            repository.toggleBookmark(lawId = 2, sectionId = 205, currentlyBookmarked = false)
            val isBookmarked = repository.isSectionBookmarked(205).first()
            assertTrue("Section 205 should be bookmarked", isBookmarked)

            val testNote = "ধারা ৩০২ এর মামলায় মোটিভ এবং ময়নাতদন্ত রিপোর্ট অত্যন্ত গুরুত্বপূর্ণ উপাদান।"
            repository.savePersonalNote(sectionId = 205, lawId = 2, noteText = testNote)

            val savedNote = repository.getPersonalNote(205).first()
            assertNotNull("Personal note should be saved", savedNote)
            assertEquals(testNote, savedNote?.noteText)

            val jsonBackup = repository.exportUserBackupJson()
            assertTrue("Backup JSON should contain bookmarks", jsonBackup.contains("bookmarks"))
            assertTrue("Backup JSON should contain notes", jsonBackup.contains("notes"))

            val restoreSuccess = repository.restoreUserBackupJson(jsonBackup)
            assertTrue("Backup restoration should succeed", restoreSuccess)
        }
    }

    @Test
    fun testLegalTextParserHierarchyAndGapDetection() {
        val sampleText = """
            (১) কোনো ব্যক্তি যদি প্রতারণা করে, তবে তিনি দণ্ডনীয় হইবেন।
            (ক) নগদ অর্থ আত্মসাৎ;
            (অ) ব্যাংক হিসাবের মাধ্যমে;
            তবে শর্ত থাকে যে, আদালতের অনুমতি সাপেক্ষে আপসযোগ্য হইবে।
            ব্যাখ্যা: এখানে অর্থ বলিতে স্থাবর ও অস্থাবর উভয় সম্পত্তি বুঝাইবে।
            ব্যতিক্রম: সরল বিশ্বাসে কৃত কার্য অপরাধ হইবে না।
            (৩) এই উপধারায় দুই নম্বর উপধারা বাদ পড়ায় ক্রম-বিচ্যুতি ধরা পড়িবে।
        """.trimIndent()

        val nodes = LegalTextParser.parseHierarchy(sampleText)
        assertTrue("Should parse subsections, clauses, subclauses, proviso, explanation, exception", nodes.size >= 7)
        assertTrue(nodes.any { it.nodeType == SubsectionNodeType.SUBSECTION && it.subsectionNumber == "(১)" })
        assertTrue(nodes.any { it.nodeType == SubsectionNodeType.CLAUSE && it.subsectionNumber == "(ক)" })
        assertTrue(nodes.any { it.nodeType == SubsectionNodeType.SUBCLAUSE && it.subsectionNumber == "(অ)" })
        assertTrue(nodes.any { it.nodeType == SubsectionNodeType.PROVISO })
        assertTrue(nodes.any { it.nodeType == SubsectionNodeType.EXPLANATION })
        assertTrue(nodes.any { it.nodeType == SubsectionNodeType.EXCEPTION })

        val missingSubs = LegalTextParser.findMissingSequentialSubsections(sampleText, nodes)
        assertEquals(listOf("(২)"), missingSubs)

        assertTrue(LegalTextParser.isTextLikelyTruncated("যে ব্যক্তি প্রতারণা করে...", isRepealed = false))
        assertFalse(LegalTextParser.isTextLikelyTruncated("[Omitted]", isRepealed = true))
    }

    @Test
    fun testBdlawsHtmlParserAndMultiPageSupport() {
        val page1Html = """
            <html><body>
              <div class="boxed-layout">
                <h1 class="act-title">নমুনা দণ্ড ও পদ্ধতি আইন, ২০২৪</h1>
                <div class="act-no">২০২৪ সনের ১ নং আইন</div>
                <div class="act-preamble">যেহেতু ন্যায়বিচার প্রতিষ্ঠা সমীচীন; সেহেতু আইন প্রণীত হইল।</div>
                <div class="act-chapter-group">
                  <span class="act-chapter-no">অধ্যায় ১</span>
                  <h3 class="act-chapter-name">প্রারম্ভিক</h3>
                  <div class="lineremoves">
                    <div class="txt-head">ধারা ১। সংক্ষিপ্ত শিরোনাম ও প্রবর্তন</div>
                    <div class="txt-details">
                      (১) এই আইন নমুনা আইন, ২০২৪ নামে অভিহিত হইবে।<sup>[1]</sup>
                      <div class="collapse">(২) ইহা সমগ্র বাংলাদেশে প্রযোজ্য হইবে &amp; কার্যকর থাকিবে।</div>
                    </div>
                  </div>
                  <div class="lineremoves">
                    <div class="txt-head">ধারা ১ক। সংজ্ঞা (সংশোধিত)</div>
                    <div class="txt-details">
                      <ul><li>অপরাধ অর্থ আইনত দণ্ডনীয় কার্য।</li></ul>
                      <table><tr><th>শব্দ</th><th>অর্থ</th></tr><tr><td>আদালত</td><td>দায়রা আদালত</td></tr></table>
                    </div>
                  </div>
                </div>
                <div class="foot-notes">
                  <p>[1] গেজেটে প্রকাশিত তারিখ ১ জানুয়ারি ২০২৪।</p>
                </div>
              </div>
            </body></html>
        """.trimIndent()

        val page2Html = """
            <html><body>
              <div class="boxed-layout">
                <div class="act-chapter-group">
                  <span class="act-chapter-no">অধ্যায় ২</span>
                  <h3 class="act-chapter-name">শাস্তি ও তফসিল</h3>
                  <div class="lineremoves">
                    <div class="txt-head">ধারা ২। বিলুপ্ত [Omitted]</div>
                    <div class="txt-details">[Omitted by Act II of 2025].</div>
                  </div>
                </div>
                <div class="schedule">
                  <h4>তফসিল ১: অপরাধের তালিকা</h4>
                  <p>নমুনা তফসিল কন্টেন্ট।</p>
                </div>
              </div>
            </body></html>
        """.trimIndent()

        val combined = BdlawsHtmlParser.parseMultiplePages(listOf(page1Html, page2Html), "https://bdlaws.minlaw.gov.bd/act-details-999.html")
        assertEquals(2, combined.chapters.size)
        assertEquals(3, combined.sections.size)
        assertEquals(1, combined.schedules.size)

        val sec1 = combined.sections[0]
        assertEquals("১", sec1.sectionNumberBn)
        assertEquals("1", sec1.sectionNumberEn)
        assertTrue("Hidden .collapse div text must be extracted", sec1.completeSectionText.contains("(২) ইহা সমগ্র বাংলাদেশে প্রযোজ্য হইবে & কার্যকর থাকিবে।"))
        assertEquals("1", sec1.footnotes.firstOrNull()?.marker)

        val sec1a = combined.sections[1]
        assertTrue("Section 1A amended flag should be true", sec1a.isAmended)
        assertTrue("Table content must be preserved cleanly", sec1a.completeSectionText.contains("আদালত | দায়রা আদালত"))

        val sec2 = combined.sections[2]
        assertTrue("Omitted/repealed section must be flagged", sec2.isRepealed)
        assertEquals(ExtractionStatus.COMPLETE, sec2.extractionStatus)
    }

    @Test
    fun testRecoveryImportAndVerificationEngineCompleteFlow() {
        runBlocking {
            val targetLawId = 200L
            db.lawDao().insertLaw(
                LawEntity(
                    id = targetLawId,
                    actNumber = "২০২৪ সনের ১০ নং আইন",
                    actYear = 2024,
                    titleBn = "নমুনা রিকভারি আইন, ২০২৪",
                    titleEn = "Sample Recovery Act, 2024",
                    shortDescriptionBn = "পরীক্ষামূলক আইন",
                    fullIntroductionBn = "প্রস্তাবনা",
                    sourceUrl = "https://bdlaws.minlaw.gov.bd/act-200.html",
                    totalSections = 2,
                    extractionStatus = ExtractionStatus.PARTIAL,
                    verificationStatus = VerificationStatus.INCOMPLETE
                )
            )
            // Seed an incomplete section 1 with a precious user note that MUST survive recovery
            db.sectionDao().insertSection(
                SectionEntity(
                    id = 200001L,
                    lawId = targetLawId,
                    chapterId = 200001L,
                    sectionNumberBn = "১",
                    sectionNumberEn = "1",
                    titleBn = "অসম্পূর্ণ শিরোনাম",
                    titleEn = "Incomplete Title",
                    fullContentBn = "কাটা অংশ...",
                    simplifiedExplanationBn = "আমার নিজের সহজ ব্যাখ্যা",
                    userNotes = "গুরুত্বপূর্ণ ব্যক্তিগত নোট যা হারানো যাবে না",
                    orderIndex = 1
                )
            )
            repository.toggleBookmark(targetLawId, 200001L, currentlyBookmarked = false)

            val officialHtml = """
                <html><body>
                  <div class="act-details">
                    <h1 class="act-title">নমুনা রিকভারি আইন, ২০২৪</h1>
                    <div class="act-chapter-group">
                      <span class="act-chapter-no">অধ্যায় ১</span>
                      <h3 class="act-chapter-name">প্রাথমিক</h3>
                      <div class="lineremoves">
                        <div class="txt-head">ধারা ১। সংক্ষিপ্ত শিরোনাম</div>
                        <div class="txt-details">
                          (১) এই আইন নমুনা রিকভারি আইন নামে পরিচিত হইবে।
                          (২) ইহা অবিলম্বে কার্যকর হইবে।
                        </div>
                      </div>
                      <div class="lineremoves">
                        <div class="txt-head">ধারা ২। দায় ও শাস্তি</div>
                        <div class="txt-details">কোনো ব্যক্তি আইন লঙ্ঘন করিলে দণ্ডিত হইবেন।</div>
                      </div>
                    </div>
                  </div>
                </body></html>
            """.trimIndent()

            val recoveryManager = LawRecoveryManager(context, db, repository)
            val result = recoveryManager.importOfficialHtml(officialHtml, targetLawId = targetLawId)

            assertTrue("Transactional recovery import should succeed", result.success)
            assertNotNull("Safety backup file path should be returned", result.backupFilePath)
            assertTrue("Safety backup file should exist on disk", File(result.backupFilePath!!).exists())

            val recoveredSections = db.sectionDao().getSectionsForLawDirect(targetLawId)
            assertEquals(2, recoveredSections.size)

            val sec1 = recoveredSections.first { it.sectionNumberEn == "1" }
            assertEquals("গুরুত্বপূর্ণ ব্যক্তিগত নোট যা হারানো যাবে না", sec1.userNotes)
            assertEquals("আমার নিজের সহজ ব্যাখ্যা", sec1.simplifiedExplanationBn)
            assertTrue("Bookmark must be preserved", repository.isSectionBookmarked(sec1.id).first())

            val subs = db.sectionDao().getSubsectionsForSectionDirect(sec1.id)
            assertEquals(2, subs.size)

            val report = result.verificationReport
            assertNotNull(report)
            assertTrue("Fully recovered 2/2 law should now be complete", report!!.isComplete)
            val updatedLaw = db.lawDao().getLawByIdDirect(targetLawId)
            assertEquals(VerificationStatus.VERIFIED, updatedLaw?.verificationStatus)
            assertEquals(ExtractionStatus.COMPLETE, updatedLaw?.extractionStatus)
        }
    }

    @Test
    fun testNonDestructiveMigration1To2PreservesUserData() {
        runBlocking {
            val dbName = "migration_test_${System.currentTimeMillis()}.db"
            val dbFile = context.getDatabasePath(dbName)
            dbFile.parentFile?.mkdirs()

            // Create authentic v1 SQLite database manually
            val sqlite = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            sqlite.execSQL("CREATE TABLE laws (id INTEGER PRIMARY KEY NOT NULL, actNumber TEXT NOT NULL, actYear INTEGER NOT NULL, titleBn TEXT NOT NULL, titleEn TEXT NOT NULL, shortDescriptionBn TEXT NOT NULL, fullIntroductionBn TEXT NOT NULL, sourceUrl TEXT NOT NULL, status TEXT NOT NULL, passedDate TEXT NOT NULL, effectiveDate TEXT NOT NULL, totalSections INTEGER NOT NULL, isPopular INTEGER NOT NULL, isConstitution INTEGER NOT NULL)")
            sqlite.execSQL("CREATE TABLE categories (id INTEGER PRIMARY KEY NOT NULL, nameBn TEXT NOT NULL, nameEn TEXT NOT NULL, iconName TEXT NOT NULL, displayOrder INTEGER NOT NULL)")
            sqlite.execSQL("CREATE TABLE law_category_cross_ref (lawId INTEGER NOT NULL, categoryId INTEGER NOT NULL, PRIMARY KEY(lawId, categoryId))")
            sqlite.execSQL("CREATE TABLE chapters (id INTEGER PRIMARY KEY NOT NULL, lawId INTEGER NOT NULL, chapterNumberBn TEXT NOT NULL, chapterNumberEn TEXT NOT NULL, titleBn TEXT NOT NULL, titleEn TEXT NOT NULL, orderIndex INTEGER NOT NULL)")
            sqlite.execSQL("CREATE TABLE sections (id INTEGER PRIMARY KEY NOT NULL, lawId INTEGER NOT NULL, chapterId INTEGER NOT NULL, sectionNumberBn TEXT NOT NULL, sectionNumberEn TEXT NOT NULL, titleBn TEXT NOT NULL, titleEn TEXT NOT NULL, fullContentBn TEXT NOT NULL, fullContentEn TEXT NOT NULL, simplifiedExplanationBn TEXT NOT NULL, userNotes TEXT, punishmentSummaryBn TEXT NOT NULL, bailableStatus TEXT NOT NULL, cognizableStatus TEXT NOT NULL, trialCourt TEXT NOT NULL, orderIndex INTEGER NOT NULL)")
            sqlite.execSQL("CREATE TABLE bookmarks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, lawId INTEGER NOT NULL, sectionId INTEGER NOT NULL, createdAt INTEGER NOT NULL, note TEXT NOT NULL)")
            sqlite.execSQL("CREATE TABLE favorites (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, lawId INTEGER NOT NULL, sectionId INTEGER, createdAt INTEGER NOT NULL)")
            sqlite.execSQL("CREATE TABLE reading_history (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, lawId INTEGER NOT NULL, sectionId INTEGER NOT NULL, viewedAt INTEGER NOT NULL)")
            sqlite.execSQL("CREATE TABLE personal_notes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, sectionId INTEGER NOT NULL, lawId INTEGER NOT NULL, noteText TEXT NOT NULL, updatedAt INTEGER NOT NULL)")

            sqlite.execSQL("CREATE INDEX index_laws_actYear ON laws(actYear)")
            sqlite.execSQL("CREATE INDEX index_laws_titleBn ON laws(titleBn)")
            sqlite.execSQL("CREATE INDEX index_laws_titleEn ON laws(titleEn)")
            sqlite.execSQL("CREATE INDEX index_law_category_cross_ref_categoryId ON law_category_cross_ref(categoryId)")
            sqlite.execSQL("CREATE INDEX index_law_category_cross_ref_lawId ON law_category_cross_ref(lawId)")
            sqlite.execSQL("CREATE INDEX index_chapters_lawId ON chapters(lawId)")
            sqlite.execSQL("CREATE INDEX index_sections_lawId ON sections(lawId)")
            sqlite.execSQL("CREATE INDEX index_sections_chapterId ON sections(chapterId)")
            sqlite.execSQL("CREATE INDEX index_sections_sectionNumberBn ON sections(sectionNumberBn)")
            sqlite.execSQL("CREATE INDEX index_sections_sectionNumberEn ON sections(sectionNumberEn)")
            sqlite.execSQL("CREATE INDEX index_sections_titleBn ON sections(titleBn)")
            sqlite.execSQL("CREATE INDEX index_sections_titleEn ON sections(titleEn)")
            sqlite.execSQL("CREATE UNIQUE INDEX index_bookmarks_sectionId ON bookmarks(sectionId)")
            sqlite.execSQL("CREATE UNIQUE INDEX index_favorites_lawId_sectionId ON favorites(lawId, sectionId)")
            sqlite.execSQL("CREATE INDEX index_reading_history_sectionId ON reading_history(sectionId)")
            sqlite.execSQL("CREATE UNIQUE INDEX index_personal_notes_sectionId ON personal_notes(sectionId)")

            sqlite.execSQL("INSERT INTO laws VALUES (1, '১৯৭২', 1972, 'সংবিধান', 'Constitution', 'desc', 'intro', 'url', 'কার্যকর', '', '', 153, 1, 1)")
            sqlite.execSQL("INSERT INTO sections VALUES (101, 1, 1, '১', '1', 'প্রজাতন্ত্র', 'The Republic', 'বাংলাদেশ একটি একক, স্বাধীন ও সার্বভৌম প্রজাতন্ত্র।', '', '', 'মাইগ্রেশন-পূর্ব নোট', '', '', '', '', 1)")
            sqlite.execSQL("INSERT INTO bookmarks VALUES (1, 1, 101, 1000, 'টেস্ট বুকমার্ক')")
            sqlite.execSQL("INSERT INTO personal_notes VALUES (1, 101, 1, 'মাইগ্রেশন-পূর্ব নোট', 1000)")
            sqlite.version = 1
            sqlite.close()

            val migratedDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
                .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
                .allowMainThreadQueries()
                .build()

            val sec = migratedDb.sectionDao().getSectionByIdDirect(101)
            assertNotNull(sec)
            assertEquals("মাইগ্রেশন-পূর্ব নোট", sec?.userNotes)
            assertTrue(migratedDb.userDataDao().isSectionBookmarked(101).first())
            val note = migratedDb.userDataDao().getNoteForSection(101).first()
            assertEquals("মাইগ্রেশন-পূর্ব নোট", note?.noteText)

            // Verify v3 tables & 8-point report columns work cleanly after migration
            val engine = LawVerificationEngine(migratedDb.lawDao(), migratedDb.sectionDao(), migratedDb.verificationDao())
            val reports = engine.verifyAllLaws()
            assertEquals(1, reports.size)
            assertFalse(reports[0].isComplete)
            assertTrue(reports[0].checkedSectionNumbersCount >= 153)

            migratedDb.close()
            dbFile.delete()
        }
    }

    @Test
    fun testPenalCodeAct11DomAlphaSectionsIncrementalMergeAndNoDuplicates() {
        runBlocking {
            // Preserve a user note on Penal Code section 302 (id = 205) before importing
            repository.savePersonalNote(205L, 2L, "খুনের মামলায় ৩০২ ধারার ব্যক্তিগত নোট")
            repository.toggleBookmark(2L, 205L, currentlyBookmarked = false)

            // Authentic bdlaws.minlaw.gov.bd/act-11.html DOM: .txt-head has ONLY English title, .txt-details has "1. ...", "1[108A. ...", "Explanation.-", etc.
            val chapterPrint1Html = """
                <html><body>
                  <div class="boxed-layout">
                    <div class="act-chapter-group">
                      <span class="act-chapter-no">CHAPTER I</span>
                      <h3 class="act-chapter-name">INTRODUCTION</h3>
                      <div class="row lineremoves">
                        <div class="col-sm-3 txt-head"><a href="/act-section-details-11-1.html">Title and extent of operation of the Code</a></div>
                        <div class="col-sm-9 txt-details">1. This Act shall be called the Penal Code, and shall take effect throughout Bangladesh.</div>
                      </div>
                      <div class="row lineremoves">
                        <div class="col-sm-3 txt-head">Punishment of offences committed within Bangladesh</div>
                        <div class="col-sm-9 txt-details">2. Every person shall be liable to punishment under this Code and not otherwise for every act or omission contrary to the provisions thereof.</div>
                      </div>
                    </div>
                  </div>
                </body></html>
            """.trimIndent()

            val chapterPrint5And22Html = """
                <html><body>
                  <div class="boxed-layout">
                    <div class="act-chapter-group">
                      <span class="act-chapter-no">CHAPTER V</span>
                      <h3 class="act-chapter-name">OF ABETMENT</h3>
                      <div class="row lineremoves">
                        <div class="col-sm-3 txt-head">Abetment in Bangladesh of offences outside it</div>
                        <div class="col-sm-9 txt-details">1[108A. A person abets an offence within the meaning of this Code who, in Bangladesh, abets the commission of any act without and beyond Bangladesh which would constitute an offence if committed in Bangladesh.
                          <p>Illustration.- A, in Bangladesh, instigates B, a foreigner in Goa, to commit a murder in Goa. A is guilty of abetting murder.</p>
                        </div>
                      </div>
                    </div>
                    <div class="act-chapter-group">
                      <span class="act-chapter-no">CHAPTER XVI</span>
                      <h3 class="act-chapter-name">OF OFFENCES AFFECTING THE HUMAN BODY</h3>
                      <div class="row lineremoves">
                        <div class="col-sm-3 txt-head">Punishment for murder</div>
                        <div class="col-sm-9 txt-details">302. Whoever commits murder shall be punished with death, or imprisonment for life, and shall also be liable to fine.</div>
                      </div>
                    </div>
                    <div class="act-chapter-group">
                      <span class="act-chapter-no">CHAPTER XXII</span>
                      <h3 class="act-chapter-name">CRIMINAL INTIMIDATION, INSULT AND ANNOYANCE</h3>
                      <div class="row lineremoves">
                        <div class="col-sm-3 txt-head">Prejudicial act by words, etc.</div>
                        <div class="col-sm-9 txt-details">2[505A. Whoever by words, either spoken or written, or by signs commits any prejudicial act shall be punished.</div>
                      </div>
                    </div>
                  </div>
                </body></html>
            """.trimIndent()

            val recoveryManager = LawRecoveryManager(context, db, repository)
            val result1 = recoveryManager.importOfficialHtmlPages(
                listOf(chapterPrint1Html, chapterPrint5And22Html),
                targetLawId = 2L,
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-11.html"
            )
            assertTrue("Import should succeed", result1.success)

            // Re-import chapterPrint1Html alone to ensure zero duplicates AND existing Chapter II..XXIII remain intact!
            val result2 = recoveryManager.importOfficialHtml(
                chapterPrint1Html,
                targetLawId = 2L,
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-11/act-chapter-print-1.html"
            )
            assertTrue("Incremental re-import should succeed", result2.success)

            val penalChapters = db.lawDao().getChaptersForLawDirect(2L)
            assertEquals("All 25 Penal Code chapters must remain intact", 25, penalChapters.size)

            val penalSections = db.sectionDao().getSectionsForLawDirect(2L)
            // Initial 13 (which includes 302) + 4 new (1, 2, 108A, 505A) = 17 unique sections
            assertEquals(17, penalSections.size)

            val sec1 = penalSections.first { it.sectionNumberEn == "1" }
            assertEquals("Title and extent of operation of the Code", sec1.titleEn)
            assertEquals("https://bdlaws.minlaw.gov.bd/act-section-details-11-1.html", sec1.sourceUrl)
            assertTrue(sec1.fullContentEn.contains("throughout Bangladesh"))

            val sec108A = penalSections.first { it.sectionNumberEn == "108A" }
            assertEquals("Abetment in Bangladesh of offences outside it", sec108A.titleEn)
            val subs108A = db.sectionDao().getSubsectionsForSectionDirect(sec108A.id)
            assertTrue("Illustration inside 108A must be parsed into subsections", subs108A.any { it.nodeType == SubsectionNodeType.ILLUSTRATION })

            // Verify section 302 retained its personal note, Bengali content, and bookmark, and no duplicate 302 was created
            val all302 = penalSections.filter { it.sectionNumberEn == "302" }
            assertEquals(1, all302.size)
            assertEquals("খুনের মামলায় ৩০২ ধারার ব্যক্তিগত নোট", all302.first().userNotes)
            assertTrue(repository.isSectionBookmarked(all302.first().id).first())

            // Verify bilingual English search finds 108A by English text
            val enSearch = repository.searchSectionsInLaw(2L, "foreigner in Goa").first()
            assertTrue(enSearch.any { it.sectionNumberEn == "108A" })

            // Verify 8-point completeness report counts checked numbers including lettered sections (511 + 3 lettered: 108A, 304A, 505A = 514)
            val rep = result2.verificationReport!!
            assertTrue(rep.checkedSectionNumbersCount >= 514)
            assertEquals("", rep.duplicateSectionsList)
        }
    }
}
