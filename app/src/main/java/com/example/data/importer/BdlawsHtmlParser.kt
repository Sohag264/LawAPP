package com.example.data.importer

import com.example.data.entity.ExtractionStatus
import com.example.utils.BanglaNumberUtils
import com.example.utils.HashUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

data class ParsedChapter(
    val chapterNumberBn: String,
    val chapterNumberEn: String,
    val titleBn: String,
    val titleEn: String,
    val orderIndex: Int
)

data class ParsedSection(
    val sectionNumberBn: String,
    val sectionNumberEn: String,
    val titleBn: String,
    val titleEn: String,
    val completeSectionText: String,
    val originalHtml: String,
    val chapterOrderIndex: Int,
    val sequenceOrder: Int,
    val contentHash: String,
    val subsections: List<ParsedSubsectionNode>,
    val footnotes: List<ParsedFootnote>,
    val isRepealed: Boolean,
    val isAmended: Boolean,
    val extractionStatus: String
)

data class ParsedSchedule(
    val scheduleNumber: String,
    val title: String,
    val contentText: String,
    val originalHtml: String,
    val sequenceOrder: Int
)

data class ParsedLawPackage(
    val titleBn: String,
    val titleEn: String,
    val actNumber: String,
    val actYear: Int,
    val preambleText: String,
    val sourceUrl: String,
    val chapters: List<ParsedChapter>,
    val sections: List<ParsedSection>,
    val schedules: List<ParsedSchedule>,
    val parsingErrors: List<String>,
    val sourceContentHash: String
)

object BdlawsHtmlParser {

    private val SECTION_NUM_PREFIX_REGEX = Regex(
        "^(?:ধারা|অনুচ্ছেদ|Section|Article|Sec\\.)?\\s*([০-৯]+(?:[ক-হA-Za-z])?|\\d+(?:[A-Za-zক-হ])?)\\s*[।.\\-:–—)]?\\s*(.*)$",
        RegexOption.IGNORE_CASE
    )
    private val YEAR_REGEX = Regex("(1[7-9]\\d{2}|20\\d{2}|[১৭৮৯০-৯]{4})")
    private val BENGALI_CHAR_REGEX = Regex("[\\u0980-\\u09FF]")

    /**
     * Parses one or more HTML pages from bdlaws.minlaw.gov.bd (handles paginated acts seamlessly).
     */
    fun parseMultiplePages(htmlPages: List<String>, sourceUrl: String): ParsedLawPackage {
        if (htmlPages.isEmpty()) {
            return ParsedLawPackage(
                titleBn = "",
                titleEn = "",
                actNumber = "",
                actYear = 0,
                preambleText = "",
                sourceUrl = sourceUrl,
                chapters = emptyList(),
                sections = emptyList(),
                schedules = emptyList(),
                parsingErrors = listOf("কোনো HTML কন্টেন্ট পাওয়া যায়নি।"),
                sourceContentHash = ""
            )
        }

        val parsedPages = htmlPages.map { parseHtml(it, sourceUrl) }
        val first = parsedPages.first()
        val combinedChapters = mutableListOf<ParsedChapter>()
        val combinedSections = mutableListOf<ParsedSection>()
        val combinedSchedules = mutableListOf<ParsedSchedule>()
        val combinedErrors = mutableListOf<String>()

        var chapterSeq = 0
        var sectionSeq = 0
        var scheduleSeq = 0

        for (page in parsedPages) {
            val chapterMap = mutableMapOf<Int, Int>()
            for (ch in page.chapters) {
                val existing = combinedChapters.find {
                    it.chapterNumberBn == ch.chapterNumberBn && it.titleBn == ch.titleBn
                }
                if (existing != null) {
                    chapterMap[ch.orderIndex] = existing.orderIndex
                } else {
                    chapterSeq++
                    combinedChapters.add(ch.copy(orderIndex = chapterSeq))
                    chapterMap[ch.orderIndex] = chapterSeq
                }
            }
            for (sec in page.sections) {
                sectionSeq++
                val mappedChapterOrder = chapterMap[sec.chapterOrderIndex] ?: 1
                combinedSections.add(
                    sec.copy(
                        chapterOrderIndex = mappedChapterOrder,
                        sequenceOrder = sectionSeq
                    )
                )
            }
            for (sch in page.schedules) {
                scheduleSeq++
                combinedSchedules.add(sch.copy(sequenceOrder = scheduleSeq))
            }
            combinedErrors.addAll(page.parsingErrors)
        }

        val combinedHash = HashUtils.sha256(
            combinedSections.joinToString("\n") { "${it.sectionNumberEn}:${it.contentHash}" }
        )

        return first.copy(
            chapters = combinedChapters,
            sections = combinedSections,
            schedules = combinedSchedules,
            parsingErrors = combinedErrors,
            sourceContentHash = combinedHash
        )
    }

    /**
     * Parses an official bdlaws.minlaw.gov.bd Act HTML DOM into structured chapters,
     * sections, hierarchical subsections, footnotes, and schedules.
     */
    fun parseHtml(html: String, sourceUrl: String = "https://bdlaws.minlaw.gov.bd"): ParsedLawPackage {
        val doc: Document = Jsoup.parse(html)
        val errors = mutableListOf<String>()

        // Remove non-content elements like scripts, styles, navbars, print buttons
        doc.select("script, style, nav, header, footer, .no-print, .btn-print, .breadcrumb").remove()

        // 1. Identify main content container
        val mainContainer = doc.selectFirst(".boxed-layout, #act-details, .act-details, main, article, .container")
            ?: doc.body()

        // 2. Extract Title & Act Metadata
        val rawTitle = mainContainer.selectFirst("h1, h2, h3, .act-title")?.text()?.trim()
            ?: doc.title().trim()
        val isBengaliPrimary = BENGALI_CHAR_REGEX.containsMatchIn(rawTitle)
        val titleBn = if (isBengaliPrimary) rawTitle else ""
        val titleEn = if (!isBengaliPrimary) rawTitle else (mainContainer.selectFirst(".act-title-en, h4.subtitle")?.text()?.trim() ?: "")

        val actNumber = mainContainer.selectFirst(".act-number, .act-no, h4")?.text()?.trim() ?: ""
        val yearMatch = YEAR_REGEX.find("$rawTitle $actNumber")
        val actYear = yearMatch?.value?.let { BanglaNumberUtils.toEnglishDigits(it).toIntOrNull() } ?: 0

        val preambleText = mainContainer.select(".act-preamble, .preamble, .act-long-title, .long-title")
            .joinToString("\n") { extractStructuredText(it) }
            .trim()

        // 3. Extract document-level footnotes map (if footnotes are placed at bottom of page)
        val footnoteMap = extractFootnoteDefinitions(mainContainer)

        val chapters = mutableListOf<ParsedChapter>()
        val sections = mutableListOf<ParsedSection>()
        val schedules = mutableListOf<ParsedSchedule>()

        var currentChapterOrder = 0
        var sectionSequence = 0
        var scheduleSequence = 0

        fun ensureDefaultChapter(): Int {
            if (currentChapterOrder == 0) {
                currentChapterOrder = 1
                chapters.add(
                    ParsedChapter(
                        chapterNumberBn = "অধ্যায় ১",
                        chapterNumberEn = "Chapter I",
                        titleBn = "মূল বিধানাবলি",
                        titleEn = "Main Provisions",
                        orderIndex = 1
                    )
                )
            }
            return currentChapterOrder
        }

        // 4. Locate section / chapter / schedule DOM blocks
        val bdlawsRows = mainContainer.select(".act-chapter-group, .act-part-group, .lineremoves, .section-row, .act-section, .schedule, section[data-section]")

        if (bdlawsRows.isNotEmpty()) {
            for (node in bdlawsRows) {
                // Skip nested matches if parent already processed
                if (node.parents().any { it in bdlawsRows }) continue

                when {
                    node.hasClass("act-chapter-group") || node.hasClass("act-part-group") -> {
                        val chNum = node.selectFirst(".act-chapter-no, .chapter-no, .part-no")?.text()?.trim() ?: ""
                        val chTitle = node.selectFirst(".act-chapter-name, .chapter-title, .part-name, h3, h4")?.text()?.trim() ?: node.text().trim()
                        currentChapterOrder++
                        chapters.add(
                            ParsedChapter(
                                chapterNumberBn = chNum.ifEmpty { "অধ্যায় ${BanglaNumberUtils.toBanglaDigits(currentChapterOrder)}" },
                                chapterNumberEn = BanglaNumberUtils.toEnglishDigits(chNum).ifEmpty { "Chapter $currentChapterOrder" },
                                titleBn = chTitle,
                                titleEn = chTitle,
                                orderIndex = currentChapterOrder
                            )
                        )
                        // Check if this chapter group directly contains section rows inside
                        val innerSections = node.select(".lineremoves, .section-row, .act-section")
                        for (secEl in innerSections) {
                            sectionSequence++
                            val parsedSec = parseSectionElement(
                                secEl = secEl,
                                chapterOrder = currentChapterOrder,
                                sequenceOrder = sectionSequence,
                                footnoteMap = footnoteMap,
                                errors = errors
                            )
                            if (parsedSec != null) sections.add(parsedSec)
                        }
                    }

                    node.hasClass("schedule") -> {
                        scheduleSequence++
                        val schTitle = node.selectFirst("h3, h4, .schedule-title")?.text()?.trim()
                            ?: "তফসিল ${BanglaNumberUtils.toBanglaDigits(scheduleSequence)}"
                        val schText = extractStructuredText(node)
                        schedules.add(
                            ParsedSchedule(
                                scheduleNumber = BanglaNumberUtils.toBanglaDigits(scheduleSequence),
                                title = schTitle,
                                contentText = schText,
                                originalHtml = node.outerHtml(),
                                sequenceOrder = scheduleSequence
                            )
                        )
                    }

                    else -> {
                        val chOrder = ensureDefaultChapter()
                        sectionSequence++
                        val parsedSec = parseSectionElement(
                            secEl = node,
                            chapterOrder = chOrder,
                            sequenceOrder = sectionSequence,
                            footnoteMap = footnoteMap,
                            errors = errors
                        )
                        if (parsedSec != null) sections.add(parsedSec)
                    }
                }
            }
        } else {
            // Fallback DOM block traversal when CSS classes differ (e.g. generic official HTML)
            val headingAndBlocks = mainContainer.select("h2, h3, h4, p, div.section, table, ol, ul")
            var activeSecNumBn = ""
            var activeSecNumEn = ""
            var activeSecTitle = ""
            val activeBodyHtml = StringBuilder()
            val activeBodyText = StringBuilder()

            fun flushActiveSection() {
                if (activeSecNumEn.isNotEmpty() || activeBodyText.isNotEmpty()) {
                    val chOrder = ensureDefaultChapter()
                    sectionSequence++
                    val fullText = activeBodyText.toString().trim()
                    val numBn = activeSecNumBn.ifEmpty { BanglaNumberUtils.toBanglaDigits(sectionSequence) }
                    val numEn = activeSecNumEn.ifEmpty { BanglaNumberUtils.toEnglishDigits(numBn) }
                    val isRep = LegalTextParser.isRepealedOrOmitted(activeSecTitle, fullText)
                    val isAm = LegalTextParser.isAmended(activeSecTitle, fullText)
                    val subsections = LegalTextParser.parseHierarchy(fullText)
                    val status = if (LegalTextParser.isTextLikelyTruncated(fullText, isRep)) {
                        ExtractionStatus.PARTIAL
                    } else {
                        ExtractionStatus.COMPLETE
                    }
                    sections.add(
                        ParsedSection(
                            sectionNumberBn = numBn,
                            sectionNumberEn = numEn,
                            titleBn = activeSecTitle.ifEmpty { "ধারা $numBn" },
                            titleEn = activeSecTitle,
                            completeSectionText = fullText,
                            originalHtml = activeBodyHtml.toString(),
                            chapterOrderIndex = chOrder,
                            sequenceOrder = sectionSequence,
                            contentHash = HashUtils.sha256(fullText),
                            subsections = subsections,
                            footnotes = emptyList(),
                            isRepealed = isRep,
                            isAmended = isAm,
                            extractionStatus = status
                        )
                    )
                    activeSecNumBn = ""
                    activeSecNumEn = ""
                    activeSecTitle = ""
                    activeBodyHtml.clear()
                    activeBodyText.clear()
                }
            }

            for (el in headingAndBlocks) {
                if (el.parents().any { it in headingAndBlocks }) continue
                val txt = extractStructuredText(el).trim()
                if (txt.isEmpty()) continue

                if (el.tagName() in listOf("h2", "h3", "h4") &&
                    (txt.contains("অধ্যায়") || txt.contains("অধ্যায়") || txt.contains("Chapter", ignoreCase = true) || txt.contains("Part", ignoreCase = true))
                ) {
                    flushActiveSection()
                    currentChapterOrder++
                    chapters.add(
                        ParsedChapter(
                            chapterNumberBn = "অধ্যায় ${BanglaNumberUtils.toBanglaDigits(currentChapterOrder)}",
                            chapterNumberEn = "Chapter $currentChapterOrder",
                            titleBn = txt,
                            titleEn = txt,
                            orderIndex = currentChapterOrder
                        )
                    )
                } else {
                    val secMatch = SECTION_NUM_PREFIX_REGEX.matchEntire(txt)
                    val looksLikeSectionHeader = el.tagName() in listOf("h3", "h4") ||
                        txt.startsWith("ধারা ") ||
                        txt.startsWith("অনুচ্ছেদ ") ||
                        txt.startsWith("Section ", ignoreCase = true)

                    if (looksLikeSectionHeader && secMatch != null) {
                        flushActiveSection()
                        val rawNum = secMatch.groupValues[1]
                        activeSecNumBn = BanglaNumberUtils.toBanglaDigits(rawNum)
                        activeSecNumEn = BanglaNumberUtils.toEnglishDigits(rawNum)
                        activeSecTitle = secMatch.groupValues[2].trim()
                        activeBodyHtml.append(el.outerHtml())
                    } else {
                        if (activeBodyText.isNotEmpty()) activeBodyText.append("\n")
                        activeBodyText.append(txt)
                        activeBodyHtml.append(el.outerHtml())
                    }
                }
            }
            flushActiveSection()
        }

        if (sections.isEmpty()) {
            errors.add("HTML ডকুমেন্টে কোনো সুনির্দিষ্ট ধারা শনাক্ত করা যায়নি।")
        }

        val fullHash = HashUtils.sha256(sections.joinToString("\n") { "${it.sectionNumberEn}:${it.contentHash}" })

        return ParsedLawPackage(
            titleBn = titleBn,
            titleEn = titleEn,
            actNumber = actNumber,
            actYear = actYear,
            preambleText = preambleText,
            sourceUrl = sourceUrl,
            chapters = chapters,
            sections = sections,
            schedules = schedules,
            parsingErrors = errors,
            sourceContentHash = fullHash
        )
    }

    private fun parseSectionElement(
        secEl: Element,
        chapterOrder: Int,
        sequenceOrder: Int,
        footnoteMap: Map<String, String>,
        errors: MutableList<String>
    ): ParsedSection? {
        // Extract section heading & body separately to avoid bleed-over
        val headEl = secEl.selectFirst(".txt-head, .sec-head, .col-sm-3, .section-head, h4, h5")
        val bodyEl = secEl.selectFirst(".txt-details, .sec-details, .col-sm-9, .section-body, .collapse")

        val headText = headEl?.let { extractStructuredText(it).trim() } ?: ""
        val rawBodyText = if (bodyEl != null) {
            extractStructuredText(bodyEl).trim()
        } else {
            // Clone and remove headEl if present
            val clone = secEl.clone()
            clone.select(".txt-head, .sec-head, .col-sm-3, .section-head, .footnote, .foot-notes").remove()
            extractStructuredText(clone).trim()
        }

        if (headText.isEmpty() && rawBodyText.isEmpty()) return null

        // Determine section number & title
        var numBn = ""
        var numEn = ""
        var titleText = headText

        val numEl = secEl.selectFirst(".sec-no, .section-number, [data-section-number]")
        if (numEl != null) {
            val rawNum = numEl.attr("data-section-number").ifEmpty { numEl.text().trim() }
            numBn = BanglaNumberUtils.toBanglaDigits(rawNum)
            numEn = BanglaNumberUtils.toEnglishDigits(rawNum)
        } else {
            val match = SECTION_NUM_PREFIX_REGEX.matchEntire(headText)
                ?: SECTION_NUM_PREFIX_REGEX.matchEntire(rawBodyText.lineSequence().firstOrNull() ?: "")
            if (match != null) {
                val rawNum = match.groupValues[1]
                numBn = BanglaNumberUtils.toBanglaDigits(rawNum)
                numEn = BanglaNumberUtils.toEnglishDigits(rawNum)
                val extractedTitle = match.groupValues[2].trim()
                if (extractedTitle.isNotEmpty() && headText.isNotEmpty()) {
                    titleText = extractedTitle
                }
            }
        }

        if (numEn.isEmpty()) {
            numBn = BanglaNumberUtils.toBanglaDigits(sequenceOrder)
            numEn = sequenceOrder.toString()
            errors.add("ধারা নম্বর স্বয়ংক্রিয়ভাবে শনাক্ত করা যায়নি (ক্রম $sequenceOrder); ক্রম অনুযায়ী '$numBn' নির্ধারিত।")
        }

        // Extract inline footnotes attached inside this section element + matched global footnote references
        val sectionFootnotes = mutableListOf<ParsedFootnote>()
        var fnSeq = 0
        secEl.select(".footnote, .foot-note, .fn-text").forEach { fnEl ->
            fnSeq++
            val fnTxt = extractStructuredText(fnEl).trim()
            if (fnTxt.isNotEmpty()) {
                sectionFootnotes.add(ParsedFootnote(marker = fnSeq.toString(), text = fnTxt, sequenceOrder = fnSeq))
            }
        }
        secEl.select("sup").forEach { sup ->
            val key = sup.text().trim().replace("[", "").replace("]", "")
            val mapped = footnoteMap[key] ?: footnoteMap[BanglaNumberUtils.toEnglishDigits(key)]
            if (mapped != null && sectionFootnotes.none { it.text == mapped }) {
                fnSeq++
                sectionFootnotes.add(ParsedFootnote(marker = key, text = mapped, sequenceOrder = fnSeq))
            }
        }

        val isRepealed = LegalTextParser.isRepealedOrOmitted(titleText, rawBodyText)
        val isAmended = LegalTextParser.isAmended(titleText, rawBodyText)
        val subsections = LegalTextParser.parseHierarchy(rawBodyText)

        val status = if (LegalTextParser.isTextLikelyTruncated(rawBodyText, isRepealed)) {
            errors.add("ধারা $numBn ($numEn) এর মূল বক্তব্য অসম্পূর্ণ বা মাঝখানে কাটা পড়েছে।")
            ExtractionStatus.PARTIAL
        } else {
            ExtractionStatus.COMPLETE
        }

        return ParsedSection(
            sectionNumberBn = numBn,
            sectionNumberEn = numEn,
            titleBn = titleText.ifEmpty { "ধারা $numBn" },
            titleEn = titleText,
            completeSectionText = rawBodyText,
            originalHtml = secEl.outerHtml(),
            chapterOrderIndex = chapterOrder,
            sequenceOrder = sequenceOrder,
            contentHash = HashUtils.sha256(rawBodyText),
            subsections = subsections,
            footnotes = sectionFootnotes,
            isRepealed = isRepealed,
            isAmended = isAmended,
            extractionStatus = status
        )
    }

    private fun extractFootnoteDefinitions(container: Element): Map<String, String> {
        val map = mutableMapOf<String, String>()
        container.select(".foot-notes p, .footnotes li, div[id^=fn], .footnote-list li").forEach { el ->
            val txt = extractStructuredText(el).trim()
            val match = Regex("^\\[?([০-৯\\d]+)\\]?[.:\\s]+(.+)$").matchEntire(txt)
            if (match != null) {
                val key = match.groupValues[1]
                val value = match.groupValues[2].trim()
                map[key] = value
                map[BanglaNumberUtils.toEnglishDigits(key)] = value
            }
        }
        return map
    }

    /**
     * Converts a DOM Element into clean Unicode text while preserving:
     * - Paragraph breaks (<p>, <div>, <br>)
     * - Ordered / nested lists (<ol>, <ul>, <li>)
     * - Table rows and columns (<table>, <tr>, <td>, <th>)
     * - HTML entity decoding via Jsoup TextNode
     */
    fun extractStructuredText(element: Element): String {
        val sb = StringBuilder()
        appendNodeText(element, sb, listDepth = 0)
        return sb.toString()
            .replace(Regex("[ \\t\\xA0]+"), " ")
            .replace(Regex(" *\\n *"), "\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun appendNodeText(node: Node, sb: StringBuilder, listDepth: Int) {
        when (node) {
            is TextNode -> {
                sb.append(node.text())
            }
            is Element -> {
                val tag = node.tagName().lowercase()
                when (tag) {
                    "br" -> sb.append("\n")
                    "table" -> {
                        if (sb.isNotEmpty() && !sb.endsWith("\n")) sb.append("\n")
                        node.select("tr").forEach { tr ->
                            val cells = tr.select("th, td").map { cell ->
                                extractStructuredText(cell).replace("\n", " ")
                            }
                            if (cells.isNotEmpty()) {
                                sb.append(cells.joinToString(" | ")).append("\n")
                            }
                        }
                        sb.append("\n")
                    }
                    "ol", "ul" -> {
                        if (sb.isNotEmpty() && !sb.endsWith("\n")) sb.append("\n")
                        var itemIdx = 0
                        for (child in node.children()) {
                            if (child.tagName().lowercase() == "li") {
                                itemIdx++
                                val indent = "  ".repeat(listDepth)
                                val prefix = if (tag == "ol") {
                                    "${BanglaNumberUtils.toBanglaDigits(itemIdx)}। "
                                } else {
                                    "• "
                                }
                                sb.append(indent).append(prefix)
                                for (liChild in child.childNodes()) {
                                    appendNodeText(liChild, sb, listDepth + 1)
                                }
                                if (!sb.endsWith("\n")) sb.append("\n")
                            } else {
                                appendNodeText(child, sb, listDepth)
                            }
                        }
                    }
                    "p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote" -> {
                        if (sb.isNotEmpty() && !sb.endsWith("\n")) sb.append("\n")
                        for (child in node.childNodes()) {
                            appendNodeText(child, sb, listDepth)
                        }
                        if (!sb.endsWith("\n")) sb.append("\n")
                    }
                    else -> {
                        for (child in node.childNodes()) {
                            appendNodeText(child, sb, listDepth)
                        }
                    }
                }
            }
        }
    }
}
