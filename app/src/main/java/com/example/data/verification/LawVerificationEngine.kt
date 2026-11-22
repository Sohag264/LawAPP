package com.example.data.verification

import com.example.data.dao.LawDao
import com.example.data.dao.SectionDao
import com.example.data.dao.VerificationDao
import com.example.data.entity.DatabaseMetadataEntity
import com.example.data.entity.ExtractionStatus
import com.example.data.entity.LawEntity
import com.example.data.entity.LawVerificationReportEntity
import com.example.data.entity.SectionEntity
import com.example.data.entity.VerificationStatus
import com.example.data.importer.LegalTextParser
import com.example.data.importer.ParsedLawPackage
import com.example.utils.BanglaNumberUtils
import com.example.utils.HashUtils

data class OfficialComparisonResult(
    val lawTitle: String,
    val officialTotalSections: Int,
    val dbTotalSections: Int,
    val matchedByHashCount: Int,
    val updatedTextCount: Int,
    val newlyRecoveredCount: Int,
    val missingInOfficialCount: Int,
    val parsingErrors: List<String>,
    val summaryMessage: String
)

data class GlobalDataQualitySummary(
    val totalLaws: Int,
    val totalChapters: Int,
    val totalSections: Int,
    val totalSubsections: Int,
    val completeLawsCount: Int,
    val incompleteLawsCount: Int,
    val totalMissingSectionsCount: Int,
    val totalIncompleteSubsectionsCount: Int,
    val unverifiedLawsCount: Int,
    val lastVerifiedAt: Long?
)

class LawVerificationEngine(
    private val lawDao: LawDao,
    private val sectionDao: SectionDao,
    private val verificationDao: VerificationDao
) {

    suspend fun verifyAllLaws(): List<LawVerificationReportEntity> {
        val laws = lawDao.getAllLawsDirect()
        val reports = mutableListOf<LawVerificationReportEntity>()
        val now = System.currentTimeMillis()

        for (law in laws) {
            val report = verifySingleLaw(law, now)
            reports.add(report)
        }

        verificationDao.insertVerificationReports(reports)
        verificationDao.setMetadata(
            DatabaseMetadataEntity(
                key = "last_verification_at",
                value = now.toString(),
                updatedAt = now
            )
        )
        return reports
    }

    suspend fun verifySingleLaw(law: LawEntity, timestamp: Long = System.currentTimeMillis()): LawVerificationReportEntity {
        val sections = sectionDao.getSectionsForLawDirect(law.id)
        val subsections = sectionDao.getSubsectionsForLawDirect(law.id)
        val subsectionsBySection = subsections.groupBy { it.sectionId }

        var completeSections = 0
        var incompleteSections = 0
        var repealedSections = 0
        var verificationPendingSections = 0
        var verifiedSubsections = 0
        var incompleteSubsections = 0
        val incompleteSectionNumbers = mutableListOf<String>()
        val parsingErrors = mutableListOf<String>()

        // 1. Check duplicates by normalized English section number
        val duplicateNums = sections
            .groupBy { BanglaNumberUtils.toEnglishDigits(it.sectionNumberEn).trim().uppercase() }
            .filter { it.key.isNotEmpty() && it.value.size > 1 }
            .keys
            .toList()

        // 2. Inspect each section for completeness, truncation, hash integrity, official comparison, and subsections
        val presentBaseNumbers = mutableSetOf<Int>()
        val letteredSections = mutableSetOf<String>()
        for (sec in sections) {
            val normNum = BanglaNumberUtils.toEnglishDigits(sec.sectionNumberEn).trim().uppercase()
            val baseNum = extractBaseNumber(normNum)
            if (baseNum != null) {
                presentBaseNumbers.add(baseNum)
                if (normNum != baseNum.toString()) {
                    letteredSections.add(normNum)
                }
            }

            if (sec.isRepealed || LegalTextParser.isRepealedOrOmitted(sec.titleBn, sec.fullContentBn)) {
                repealedSections++
            }

            val primaryText = sec.fullContentBn.ifBlank { sec.fullContentEn }
            val expectedHash = HashUtils.sha256(primaryText)
            val hashValid = sec.contentHash.isEmpty() ||
                sec.contentHash == expectedHash ||
                sec.contentHash == HashUtils.sha256(sec.fullContentEn)
            val truncated = sec.extractionStatus == ExtractionStatus.PARTIAL ||
                LegalTextParser.isTextLikelyTruncated(primaryText, sec.isRepealed)

            val secSubs = subsectionsBySection[sec.id] ?: emptyList()
            val parsedNodes = LegalTextParser.parseHierarchy(primaryText)
            val missingSubs = LegalTextParser.findMissingSequentialSubsections(primaryText, parsedNodes)

            var sectionSubIncomplete = false
            if (secSubs.isEmpty() && parsedNodes.isNotEmpty()) {
                incompleteSubsections += parsedNodes.size
                sectionSubIncomplete = true
                parsingErrors.add("ধারা ${sec.sectionNumberBn}: উপধারা ডেটাবেজে আলাদা টেবিলে ম্যাপ করা নেই।")
            } else if (missingSubs.isNotEmpty()) {
                incompleteSubsections += missingSubs.size
                sectionSubIncomplete = true
                parsingErrors.add("ধারা ${sec.sectionNumberBn}: অনুপস্থিত উপধারা ${missingSubs.joinToString(", ")}")
            } else {
                verifiedSubsections += secSubs.size
            }

            if (truncated || !hashValid || sectionSubIncomplete) {
                incompleteSections++
                incompleteSectionNumbers.add(sec.sectionNumberEn.ifEmpty { sec.sectionNumberBn })
                if (truncated) {
                    parsingErrors.add("ধারা ${sec.sectionNumberBn}: মূল বক্তব্য অসম্পূর্ণ, সংক্ষিপ্ত বা মাঝখানে কাটা পড়েছে।")
                }
                if (!hashValid) {
                    parsingErrors.add("ধারা ${sec.sectionNumberBn}: কন্টেন্ট হ্যাশ অসামঞ্জস্যপূর্ণ।")
                }
            } else {
                completeSections++
            }

            // Do NOT count a section as verified unless it was actually compared/imported from official source
            val isVerifiedAgainstOfficial = sec.verificationStatus == VerificationStatus.VERIFIED &&
                sec.originalHtml.isNotBlank()
            if (!isVerifiedAgainstOfficial) {
                verificationPendingSections++
            }
        }

        // 3. Check missing section numbers from 1..law.totalSections (plus any lettered sections like 108A, 153A, 505A)
        val expectedCount = law.totalSections.coerceAtLeast(sections.size)
        val missingNumbers = mutableListOf<Int>()
        for (n in 1..expectedCount) {
            if (n !in presentBaseNumbers) {
                missingNumbers.add(n)
            }
        }
        val checkedSectionNumbersCount = law.totalSections + letteredSections.size

        // Check trailing section presence
        if (law.totalSections > 0 && law.totalSections !in presentBaseNumbers) {
            parsingErrors.add("আইনের শেষের ধারা (${BanglaNumberUtils.toBanglaDigits(law.totalSections)}) অনুপস্থিত।")
        }

        val missingSummary = formatMissingRanges(missingNumbers)
        val isComplete = sections.isNotEmpty() &&
            missingNumbers.isEmpty() &&
            incompleteSections == 0 &&
            duplicateNums.isEmpty() &&
            incompleteSubsections == 0 &&
            verificationPendingSections == 0 &&
            sections.size >= law.totalSections

        val extractionStatus = when {
            sections.isEmpty() -> ExtractionStatus.NOT_EXTRACTED
            missingNumbers.isEmpty() && incompleteSections == 0 -> ExtractionStatus.COMPLETE
            else -> ExtractionStatus.PARTIAL
        }

        val verificationStatus = when {
            sections.isEmpty() -> VerificationStatus.VERIFICATION_PENDING
            isComplete -> VerificationStatus.VERIFIED
            missingNumbers.isEmpty() && incompleteSections == 0 && verificationPendingSections > 0 ->
                VerificationStatus.VERIFICATION_PENDING
            else -> VerificationStatus.INCOMPLETE
        }

        lawDao.updateLawVerification(
            lawId = law.id,
            extractionStatus = extractionStatus,
            verificationStatus = verificationStatus,
            verifiedAt = timestamp,
            sourceHash = HashUtils.sha256(sections.joinToString("|") { it.contentHash })
        )

        val report = LawVerificationReportEntity(
            lawId = law.id,
            expectedSectionsCount = expectedCount,
            checkedSectionNumbersCount = checkedSectionNumbersCount.coerceAtLeast(expectedCount),
            actualSectionsCount = sections.size,
            completeSectionsCount = completeSections,
            incompleteSectionsCount = incompleteSections,
            repealedSectionsCount = repealedSections,
            verificationPendingCount = verificationPendingSections + missingNumbers.size,
            missingSectionsCount = missingNumbers.size,
            missingSectionsList = missingSummary,
            incompleteSectionsList = incompleteSectionNumbers.distinct().joinToString(", "),
            verifiedSubsectionsCount = verifiedSubsections,
            incompleteSubsectionsCount = incompleteSubsections,
            duplicateSectionsList = duplicateNums.joinToString(", "),
            parsingErrorsList = parsingErrors.take(15).joinToString("\n"),
            isComplete = isComplete,
            verifiedAt = timestamp
        )
        verificationDao.insertVerificationReport(report)
        return report
    }

    /**
     * Compares an incoming official ParsedLawPackage (e.g. from bdlaws.minlaw.gov.bd HTML)
     * against existing database sections using both Content Hash and normalized text/structure.
     */
    suspend fun compareWithOfficialPackage(lawId: Long?, parsed: ParsedLawPackage): OfficialComparisonResult {
        val existingSections = if (lawId != null) sectionDao.getSectionsForLawDirect(lawId) else emptyList()
        val existingByNum = existingSections.associateBy {
            BanglaNumberUtils.toEnglishDigits(it.sectionNumberEn).trim().uppercase()
        }

        var matchedHash = 0
        var updatedText = 0
        var newlyRecovered = 0

        val officialNums = mutableSetOf<String>()
        for (pSec in parsed.sections) {
            val key = BanglaNumberUtils.toEnglishDigits(pSec.sectionNumberEn).trim().uppercase()
            officialNums.add(key)
            val existing = existingByNum[key]
            if (existing == null) {
                newlyRecovered++
            } else {
                val existingNormalized = existing.fullContentBn.trim().replace(Regex("\\s+"), " ")
                val newNormalized = pSec.completeSectionText.trim().replace(Regex("\\s+"), " ")
                if (existing.contentHash == pSec.contentHash && existingNormalized == newNormalized) {
                    matchedHash++
                } else {
                    updatedText++
                }
            }
        }

        val missingInOfficial = existingByNum.keys.count { it !in officialNums }
        val msg = "সরকারি উৎস (${parsed.sections.size}টি ধারা) বনাম ডেটাবেজ (${existingSections.size}টি ধারা): " +
            "$newlyRecovered টি নতুন ধারা পুনরুদ্ধারযোগ্য, $updatedText টি ধারা আপডেট, $matchedHash টি হুবহু মিলেছে।"

        return OfficialComparisonResult(
            lawTitle = parsed.titleBn.ifEmpty { parsed.titleEn },
            officialTotalSections = parsed.sections.size,
            dbTotalSections = existingSections.size,
            matchedByHashCount = matchedHash,
            updatedTextCount = updatedText,
            newlyRecoveredCount = newlyRecovered,
            missingInOfficialCount = missingInOfficial,
            parsingErrors = parsed.parsingErrors,
            summaryMessage = msg
        )
    }

    private fun extractBaseNumber(sectionNumEn: String): Int? {
        val clean = BanglaNumberUtils.toEnglishDigits(sectionNumEn).trim()
        val match = Regex("^(\\d+)").find(clean) ?: return null
        return match.groupValues[1].toIntOrNull()
    }

    private fun formatMissingRanges(numbers: List<Int>): String {
        if (numbers.isEmpty()) return "কোনো ধারা অনুপস্থিত নেই"
        val ranges = mutableListOf<String>()
        var start = numbers[0]
        var prev = numbers[0]

        for (i in 1 until numbers.size) {
            val curr = numbers[i]
            if (curr == prev + 1) {
                prev = curr
            } else {
                ranges.add(formatRange(start, prev))
                start = curr
                prev = curr
            }
        }
        ranges.add(formatRange(start, prev))

        return if (ranges.size > 12) {
            ranges.take(12).joinToString(", ") + " … (মোট ${BanglaNumberUtils.toBanglaDigits(numbers.size)}টি অনুপস্থিত)"
        } else {
            ranges.joinToString(", ")
        }
    }

    private fun formatRange(start: Int, end: Int): String {
        val sBn = BanglaNumberUtils.toBanglaDigits(start)
        val eBn = BanglaNumberUtils.toBanglaDigits(end)
        return if (start == end) sBn else "$sBn–$eBn"
    }
}
