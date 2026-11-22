package com.example.data.importer

import android.content.Context
import androidx.room.withTransaction
import com.example.data.database.AppDatabase
import com.example.data.entity.ChapterEntity
import com.example.data.entity.ExtractionStatus
import com.example.data.entity.FootnoteEntity
import com.example.data.entity.LawEntity
import com.example.data.entity.LawVerificationReportEntity
import com.example.data.entity.ScheduleEntity
import com.example.data.entity.SectionEntity
import com.example.data.entity.SubsectionEntity
import com.example.data.entity.VerificationStatus
import com.example.data.repository.LawRepository
import com.example.data.verification.LawVerificationEngine
import com.example.data.verification.OfficialComparisonResult
import com.example.utils.BanglaNumberUtils
import com.example.utils.HashUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class RecoveryImportResult(
    val success: Boolean,
    val lawId: Long?,
    val backupFilePath: String?,
    val comparison: OfficialComparisonResult?,
    val verificationReport: LawVerificationReportEntity?,
    val message: String
)

class LawRecoveryManager(
    private val context: Context,
    private val database: AppDatabase,
    private val repository: LawRepository
) {
    private val lawDao = database.lawDao()
    private val sectionDao = database.sectionDao()
    private val verificationDao = database.verificationDao()
    private val verificationEngine = LawVerificationEngine(lawDao, sectionDao, verificationDao)

    /**
     * Step 1 of safe recovery: persist a full JSON backup of all user bookmarks, favorites,
     * personal notes, and section references before running any database update.
     */
    suspend fun createSafetyBackup(): File {
        val backupDir = File(context.filesDir, "recovery_backups").apply { mkdirs() }
        val backupFile = File(backupDir, "user_data_backup_${System.currentTimeMillis()}.json")
        val json = repository.exportUserBackupJson()
        backupFile.writeText(json, Charsets.UTF_8)
        return backupFile
    }

    /**
     * Imports an official bdlaws.minlaw.gov.bd HTML file (or multi-page HTML list) into the database
     * inside a safe atomic transaction, preserving all user notes, bookmarks, and favorites.
     */
    suspend fun importOfficialHtml(
        htmlContent: String,
        targetLawId: Long? = null,
        sourceUrl: String = "https://bdlaws.minlaw.gov.bd"
    ): RecoveryImportResult {
        if (htmlContent.isBlank()) {
            return RecoveryImportResult(
                success = false,
                lawId = targetLawId,
                backupFilePath = null,
                comparison = null,
                verificationReport = null,
                message = "আমদানিকৃত HTML ফাইলটি খালি।"
            )
        }

        val parsed = BdlawsHtmlParser.parseHtml(htmlContent, sourceUrl)
        if (parsed.sections.isEmpty()) {
            return RecoveryImportResult(
                success = false,
                lawId = targetLawId,
                backupFilePath = null,
                comparison = null,
                verificationReport = null,
                message = "HTML পার্সিংয়ে কোনো ধারা পাওয়া যায়নি। বৈধ bdlaws.minlaw.gov.bd HTML নির্বাচন করুন।"
            )
        }

        return applyParsedPackageTransactionally(
            parsed = parsed,
            targetLawId = targetLawId,
            overrideVerificationStatus = null
        )
    }

    /**
     * Imports a structured JSON Law Package (validated for schema, duplicate IDs, and content).
     */
    suspend fun importOfficialJsonPackage(
        jsonString: String,
        targetLawId: Long? = null
    ): RecoveryImportResult {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("sections")) {
                return RecoveryImportResult(
                    success = false,
                    lawId = targetLawId,
                    backupFilePath = null,
                    comparison = null,
                    verificationReport = null,
                    message = "অবৈধ JSON প্যাকেজ: 'sections' তালিকা পাওয়া যায়নি।"
                )
            }

            val titleBn = root.optString("titleBn", "")
            val titleEn = root.optString("titleEn", "")
            val actNumber = root.optString("actNumber", "")
            val actYear = root.optInt("actYear", 0)
            val sourceUrl = root.optString("sourceUrl", "https://bdlaws.minlaw.gov.bd")
            val preambleText = root.optString("preamble", "")

            val chArr = root.optJSONArray("chapters") ?: JSONArray()
            val chapters = mutableListOf<ParsedChapter>()
            for (i in 0 until chArr.length()) {
                val c = chArr.getJSONObject(i)
                chapters.add(
                    ParsedChapter(
                        chapterNumberBn = c.optString("chapterNumberBn", "অধ্যায় ${BanglaNumberUtils.toBanglaDigits(i + 1)}"),
                        chapterNumberEn = c.optString("chapterNumberEn", "Chapter ${i + 1}"),
                        titleBn = c.optString("titleBn", ""),
                        titleEn = c.optString("titleEn", ""),
                        orderIndex = c.optInt("orderIndex", i + 1)
                    )
                )
            }

            val secArr = root.getJSONArray("sections")
            val seenSectionNums = mutableSetOf<String>()
            val sections = mutableListOf<ParsedSection>()
            val errors = mutableListOf<String>()

            for (i in 0 until secArr.length()) {
                val s = secArr.getJSONObject(i)
                val secNumEn = BanglaNumberUtils.toEnglishDigits(s.optString("sectionNumberEn", s.optString("sectionNumber", "${i + 1}"))).trim()
                val secNumBn = BanglaNumberUtils.toBanglaDigits(s.optString("sectionNumberBn", secNumEn)).trim()

                if (!seenSectionNums.add(secNumEn.uppercase())) {
                    errors.add("ডুপ্লিকেট ধারা শনাক্ত হয়েছে: $secNumBn ($secNumEn)")
                    continue
                }

                val fullText = s.optString("completeSectionText", s.optString("fullContentBn", "")).trim()
                val sTitleBn = s.optString("titleBn", "ধারা $secNumBn")
                val sTitleEn = s.optString("titleEn", sTitleBn)
                val isRep = s.optBoolean("isRepealed", LegalTextParser.isRepealedOrOmitted(sTitleBn, fullText))
                val isAm = s.optBoolean("isAmended", LegalTextParser.isAmended(sTitleBn, fullText))

                val fnList = mutableListOf<ParsedFootnote>()
                val fnArr = s.optJSONArray("footnotes")
                if (fnArr != null) {
                    for (j in 0 until fnArr.length()) {
                        val fnObj = fnArr.optJSONObject(j)
                        if (fnObj != null) {
                            fnList.add(
                                ParsedFootnote(
                                    marker = fnObj.optString("marker", "${j + 1}"),
                                    text = fnObj.optString("text", ""),
                                    sequenceOrder = j + 1
                                )
                            )
                        }
                    }
                }

                sections.add(
                    ParsedSection(
                        sectionNumberBn = secNumBn,
                        sectionNumberEn = secNumEn,
                        titleBn = sTitleBn,
                        titleEn = sTitleEn,
                        completeSectionText = fullText,
                        originalHtml = s.optString("originalHtml", ""),
                        chapterOrderIndex = s.optInt("chapterOrderIndex", 1),
                        sequenceOrder = s.optInt("orderIndex", i + 1),
                        contentHash = HashUtils.sha256(fullText),
                        subsections = LegalTextParser.parseHierarchy(fullText),
                        footnotes = fnList,
                        isRepealed = isRep,
                        isAmended = isAm,
                        extractionStatus = if (LegalTextParser.isTextLikelyTruncated(fullText, isRep)) {
                            ExtractionStatus.PARTIAL
                        } else {
                            ExtractionStatus.COMPLETE
                        }
                    )
                )
            }

            if (sections.isEmpty()) {
                return RecoveryImportResult(
                    success = false,
                    lawId = targetLawId,
                    backupFilePath = null,
                    comparison = null,
                    verificationReport = null,
                    message = "JSON প্যাকেজে কোনো বৈধ ধারা পাওয়া যায়নি।"
                )
            }

            val parsed = ParsedLawPackage(
                titleBn = titleBn,
                titleEn = titleEn,
                actNumber = actNumber,
                actYear = actYear,
                preambleText = preambleText,
                sourceUrl = sourceUrl,
                chapters = chapters,
                sections = sections,
                schedules = emptyList(),
                parsingErrors = errors,
                sourceContentHash = HashUtils.sha256(sections.joinToString("\n") { it.contentHash })
            )

            applyParsedPackageTransactionally(
                parsed = parsed,
                targetLawId = targetLawId,
                overrideVerificationStatus = null
            )
        } catch (e: Exception) {
            RecoveryImportResult(
                success = false,
                lawId = targetLawId,
                backupFilePath = null,
                comparison = null,
                verificationReport = null,
                message = "JSON প্যাকেজ পার্স করতে ব্যর্থ: ${e.localizedMessage}"
            )
        }
    }

    suspend fun applyParsedPackageTransactionally(
        parsed: ParsedLawPackage,
        targetLawId: Long?,
        overrideVerificationStatus: String?
    ): RecoveryImportResult {
        val backupFile = createSafetyBackup()
        val resolvedLawId = targetLawId ?: findMatchingLawId(parsed) ?: generateNextLawId()

        val comparison = verificationEngine.compareWithOfficialPackage(resolvedLawId, parsed)

        return try {
            val report = database.withTransaction {
                val existingLaw = lawDao.getLawByIdDirect(resolvedLawId)
                val existingSections = sectionDao.getSectionsForLawDirect(resolvedLawId)
                val existingByNum = existingSections.associateBy {
                    BanglaNumberUtils.toEnglishDigits(it.sectionNumberEn).trim().uppercase()
                }

                // 1. Insert or update Law record without wiping expected totalSections if known larger
                val expectedTotal = maxOf(existingLaw?.totalSections ?: 0, parsed.sections.size)
                val lawEntity = if (existingLaw != null) {
                    existingLaw.copy(
                        titleBn = parsed.titleBn.ifEmpty { existingLaw.titleBn },
                        titleEn = parsed.titleEn.ifEmpty { existingLaw.titleEn },
                        actNumber = parsed.actNumber.ifEmpty { existingLaw.actNumber },
                        actYear = if (parsed.actYear > 0) parsed.actYear else existingLaw.actYear,
                        fullIntroductionBn = parsed.preambleText.ifEmpty { existingLaw.fullIntroductionBn },
                        sourceUrl = parsed.sourceUrl.ifEmpty { existingLaw.sourceUrl },
                        totalSections = expectedTotal,
                        sourceContentHash = parsed.sourceContentHash
                    )
                } else {
                    LawEntity(
                        id = resolvedLawId,
                        actNumber = parsed.actNumber.ifEmpty { "আইন" },
                        actYear = parsed.actYear,
                        titleBn = parsed.titleBn.ifEmpty { parsed.titleEn.ifEmpty { "নতুন আইন" } },
                        titleEn = parsed.titleEn,
                        shortDescriptionBn = parsed.preambleText.take(160),
                        fullIntroductionBn = parsed.preambleText,
                        sourceUrl = parsed.sourceUrl,
                        totalSections = expectedTotal,
                        extractionStatus = ExtractionStatus.PARTIAL,
                        verificationStatus = overrideVerificationStatus ?: VerificationStatus.UNVERIFIED,
                        sourceContentHash = parsed.sourceContentHash
                    )
                }
                lawDao.insertLaw(lawEntity)

                // 2. Replace Chapters cleanly using deterministic IDs (lawId * 1000 + orderIndex)
                val chaptersToInsert = if (parsed.chapters.isNotEmpty()) {
                    parsed.chapters.map { ch ->
                        ChapterEntity(
                            id = resolvedLawId * 1000L + ch.orderIndex,
                            lawId = resolvedLawId,
                            chapterNumberBn = ch.chapterNumberBn,
                            chapterNumberEn = ch.chapterNumberEn,
                            titleBn = ch.titleBn,
                            titleEn = ch.titleEn,
                            orderIndex = ch.orderIndex
                        )
                    }
                } else {
                    val existingChapters = lawDao.getChaptersForLawDirect(resolvedLawId)
                    if (existingChapters.isNotEmpty()) existingChapters else listOf(
                        ChapterEntity(
                            id = resolvedLawId * 1000L + 1L,
                            lawId = resolvedLawId,
                            chapterNumberBn = "অধ্যায় ১",
                            chapterNumberEn = "Chapter I",
                            titleBn = "মূল বিধানাবলি",
                            titleEn = "Main Provisions",
                            orderIndex = 1
                        )
                    )
                }
                lawDao.insertChapters(chaptersToInsert)

                val chapterIdByOrder = chaptersToInsert.associate { it.orderIndex to it.id }
                val defaultChapterId = chaptersToInsert.first().id

                // 3. Upsert Sections while preserving userNotes, simplifiedExplanationBn, and existing Section IDs!
                var nextSyntheticSectionId = (existingSections.maxOfOrNull { it.id } ?: (resolvedLawId * 1000L)) + 1L
                val sectionsToInsert = mutableListOf<SectionEntity>()
                val sectionIdForParsed = mutableMapOf<Int, Long>()

                for (pSec in parsed.sections) {
                    val key = BanglaNumberUtils.toEnglishDigits(pSec.sectionNumberEn).trim().uppercase()
                    val existingSec = existingByNum[key]
                    val targetSecId = existingSec?.id ?: (resolvedLawId * 1000L + pSec.sequenceOrder).let { candidate ->
                        if (existingSections.any { it.id == candidate } || sectionIdForParsed.values.contains(candidate)) {
                            nextSyntheticSectionId++
                        } else {
                            candidate
                        }
                    }
                    sectionIdForParsed[pSec.sequenceOrder] = targetSecId

                    val chId = chapterIdByOrder[pSec.chapterOrderIndex] ?: existingSec?.chapterId ?: defaultChapterId
                    sectionsToInsert.add(
                        SectionEntity(
                            id = targetSecId,
                            lawId = resolvedLawId,
                            chapterId = chId,
                            sectionNumberBn = pSec.sectionNumberBn,
                            sectionNumberEn = pSec.sectionNumberEn,
                            titleBn = pSec.titleBn,
                            titleEn = pSec.titleEn,
                            fullContentBn = pSec.completeSectionText,
                            fullContentEn = existingSec?.fullContentEn ?: "",
                            simplifiedExplanationBn = existingSec?.simplifiedExplanationBn ?: "",
                            userNotes = existingSec?.userNotes,
                            punishmentSummaryBn = existingSec?.punishmentSummaryBn ?: "",
                            bailableStatus = existingSec?.bailableStatus ?: "",
                            cognizableStatus = existingSec?.cognizableStatus ?: "",
                            trialCourt = existingSec?.trialCourt ?: "",
                            orderIndex = pSec.sequenceOrder,
                            originalHtml = pSec.originalHtml,
                            contentHash = pSec.contentHash,
                            sourceUrl = parsed.sourceUrl,
                            extractionStatus = pSec.extractionStatus,
                            verificationStatus = overrideVerificationStatus ?: VerificationStatus.UNVERIFIED,
                            isRepealed = pSec.isRepealed,
                            isAmended = pSec.isAmended,
                            amendmentNote = existingSec?.amendmentNote ?: ""
                        )
                    )
                }
                sectionDao.insertSections(sectionsToInsert)

                // 4. Rebuild hierarchical Subsections & Footnotes cleanly for each imported section
                for (pSec in parsed.sections) {
                    val secId = sectionIdForParsed[pSec.sequenceOrder] ?: continue
                    sectionDao.deleteSubsectionsForSection(secId)
                    sectionDao.deleteFootnotesForSection(secId)

                    val tempToRealId = mutableMapOf<Int, Long>()
                    for (node in pSec.subsections) {
                        val realParentId = node.parentTempId?.let { tempToRealId[it] }
                        val insertedId = sectionDao.insertSubsection(
                            SubsectionEntity(
                                sectionId = secId,
                                parentSubsectionId = realParentId,
                                subsectionNumber = node.subsectionNumber,
                                nodeType = node.nodeType,
                                subsectionText = node.subsectionText,
                                sequenceOrder = node.sequenceOrder
                            )
                        )
                        tempToRealId[node.tempId] = insertedId
                    }

                    if (pSec.footnotes.isNotEmpty()) {
                        sectionDao.insertFootnotes(
                            pSec.footnotes.map { fn ->
                                FootnoteEntity(
                                    sectionId = secId,
                                    lawId = resolvedLawId,
                                    footnoteMarker = fn.marker,
                                    footnoteText = fn.text,
                                    sequenceOrder = fn.sequenceOrder
                                )
                            }
                        )
                    }
                }

                // 5. Schedules
                if (parsed.schedules.isNotEmpty()) {
                    sectionDao.deleteSchedulesForLaw(resolvedLawId)
                    sectionDao.insertSchedules(
                        parsed.schedules.map { sch ->
                            ScheduleEntity(
                                lawId = resolvedLawId,
                                scheduleNumber = sch.scheduleNumber,
                                title = sch.title,
                                contentText = sch.contentText,
                                originalHtml = sch.originalHtml,
                                sequenceOrder = sch.sequenceOrder
                            )
                        }
                    )
                }

                // 6. Run automated completeness verification on the updated law
                verificationEngine.verifySingleLaw(lawEntity)
            }

            RecoveryImportResult(
                success = true,
                lawId = resolvedLawId,
                backupFilePath = backupFile.absolutePath,
                comparison = comparison,
                verificationReport = report,
                message = "সফলভাবে ${parsed.sections.size}টি ধারা ট্রানজেকশন-সুরক্ষিতভাবে ইম্পোর্ট ও যাচাই করা হয়েছে।"
            )
        } catch (e: Exception) {
            RecoveryImportResult(
                success = false,
                lawId = resolvedLawId,
                backupFilePath = backupFile.absolutePath,
                comparison = comparison,
                verificationReport = null,
                message = "ইম্পোর্ট ব্যর্থ হয়েছে (পূর্বের ডেটাবেজ অক্ষত আছে): ${e.localizedMessage}"
            )
        }
    }

    private suspend fun findMatchingLawId(parsed: ParsedLawPackage): Long? {
        val laws = lawDao.getAllLawsDirect()
        return laws.firstOrNull { law ->
            (parsed.sourceUrl.isNotBlank() && law.sourceUrl.equals(parsed.sourceUrl, ignoreCase = true)) ||
                (parsed.titleBn.isNotBlank() && law.titleBn.contains(parsed.titleBn.take(16), ignoreCase = true)) ||
                (parsed.titleEn.isNotBlank() && law.titleEn.contains(parsed.titleEn.take(16), ignoreCase = true))
        }?.id
    }

    private suspend fun generateNextLawId(): Long {
        val laws = lawDao.getAllLawsDirect()
        return (laws.maxOfOrNull { it.id } ?: 100L) + 1L
    }
}
