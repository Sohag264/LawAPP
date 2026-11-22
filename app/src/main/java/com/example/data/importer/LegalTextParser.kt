package com.example.data.importer

import com.example.data.entity.SubsectionNodeType
import com.example.utils.BanglaNumberUtils

data class ParsedSubsectionNode(
    val tempId: Int,
    val parentTempId: Int?,
    val subsectionNumber: String,
    val nodeType: String,
    val subsectionText: String,
    val sequenceOrder: Int
)

data class ParsedFootnote(
    val marker: String,
    val text: String,
    val sequenceOrder: Int
)

object LegalTextParser {

    // Patterns for Bengali & English legal hierarchy
    private val SUBSECTION_START = Regex("^\\s*\\(([০-৯]+|\\d+)\\)\\s*(.*)$")
    private val NUMBERED_ITEM_START = Regex("^\\s*([০-৯]+|\\d+)\\.\\s+(.+)$")
    private val CLAUSE_START = Regex("^\\s*\\(([ক-হ]|[a-z])\\)\\s*(.*)$")
    private val SUBCLAUSE_START = Regex("^\\s*\\(([অ-ঔ]|i{1,3}|iv|v|vi{0,3}|ix|x)\\)\\s*(.*)$", RegexOption.IGNORE_CASE)
    private val PROVISO_START = Regex("^\\s*(তবে শর্ত থাকে যে|আরও শর্ত থাকে যে|শর্ত থাকে যে|Provided that|Provided further that)[:\\s,]*(.*)$", RegexOption.IGNORE_CASE)
    private val EXPLANATION_START = Regex("^\\s*(ব্যাখ্যা(?:\\s*[০-৯\\d]*)?|Explanation(?:\\s*\\d*)?)\\s*[:\\-–—.]\\s*(.*)$", RegexOption.IGNORE_CASE)
    private val EXCEPTION_START = Regex("^\\s*(ব্যতিক্রম(?:সমূহ)?|Exception(?:s)?)\\s*[:\\-–—.]\\s*(.*)$", RegexOption.IGNORE_CASE)
    private val ILLUSTRATION_START = Regex("^\\s*(দৃষ্টান্ত(?:সমূহ)?|Illustration(?:s)?)\\s*[:\\-–—.]\\s*(.*)$", RegexOption.IGNORE_CASE)

    fun isRepealedOrOmitted(title: String, content: String): Boolean {
        val combined = "$title $content"
        return combined.contains("বিলুপ্ত") ||
            combined.contains("রহিত") ||
            combined.contains("[Omitted", ignoreCase = true) ||
            combined.contains("[Repealed", ignoreCase = true) ||
            combined.trim().equals("Omitted", ignoreCase = true) ||
            combined.trim().equals("Repealed", ignoreCase = true)
    }

    fun isAmended(title: String, content: String): Boolean {
        val combined = "$title $content"
        return combined.contains("সংশোধিত") ||
            combined.contains("প্রতিস্থাপিত") ||
            combined.contains("Substituted", ignoreCase = true) ||
            combined.contains("Inserted", ignoreCase = true) ||
            combined.contains("Amended", ignoreCase = true)
    }

    /**
     * Parses hierarchical subsections, clauses, subclauses, provisos, explanations,
     * exceptions, and illustrations from complete statutory section text without dropping any text.
     */
    fun parseHierarchy(fullSectionText: String): List<ParsedSubsectionNode> {
        if (fullSectionText.isBlank()) return emptyList()

        val normalizedText = fullSectionText
            .replace("\r\n", "\n")
            .replace(
                Regex("(?<=[।.;:])[ \\t]+(?=\\((?:[০-৯]+|\\d+|[ক-হ]|[a-z])\\)\\s)"),
                "\n"
            )
            .replace(
                Regex("(?<=[।.;:])[ \\t]+(?=(?:তবে শর্ত থাকে যে|আরও শর্ত থাকে যে|Provided that|Provided further that))", RegexOption.IGNORE_CASE),
                "\n"
            )
        val lines = normalizedText.split("\n")
        val nodes = mutableListOf<ParsedSubsectionNode>()

        var nextTempId = 1
        var currentSubsectionId: Int? = null
        var currentClauseId: Int? = null
        var activeNodeIndex: Int? = null
        var sequenceOrder = 0

        fun addNode(parentTempId: Int?, number: String, type: String, text: String): Int {
            val id = nextTempId++
            sequenceOrder++
            nodes.add(
                ParsedSubsectionNode(
                    tempId = id,
                    parentTempId = parentTempId,
                    subsectionNumber = number,
                    nodeType = type,
                    subsectionText = text.trim(),
                    sequenceOrder = sequenceOrder
                )
            )
            activeNodeIndex = nodes.lastIndex
            return id
        }

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            val subMatch = SUBSECTION_START.matchEntire(line)
            val numItemMatch = if (subMatch == null) NUMBERED_ITEM_START.matchEntire(line) else null
            val clauseMatch = if (subMatch == null && numItemMatch == null) CLAUSE_START.matchEntire(line) else null
            val subclauseMatch = if (subMatch == null && numItemMatch == null && clauseMatch == null) SUBCLAUSE_START.matchEntire(line) else null
            val provisoMatch = PROVISO_START.matchEntire(line)
            val explanationMatch = EXPLANATION_START.matchEntire(line)
            val exceptionMatch = EXCEPTION_START.matchEntire(line)
            val illustrationMatch = ILLUSTRATION_START.matchEntire(line)

            when {
                subMatch != null -> {
                    val num = "(${subMatch.groupValues[1]})"
                    val body = subMatch.groupValues[2].trim()
                    val text = if (body.isNotEmpty()) "$num $body" else num
                    currentSubsectionId = addNode(null, num, SubsectionNodeType.SUBSECTION, text)
                    currentClauseId = null
                }
                numItemMatch != null -> {
                    val num = "${numItemMatch.groupValues[1]}."
                    val body = numItemMatch.groupValues[2].trim()
                    val text = "$num $body"
                    val parent = currentSubsectionId
                    if (parent == null) {
                        currentSubsectionId = addNode(null, num, SubsectionNodeType.SUBSECTION, text)
                        currentClauseId = null
                    } else {
                        currentClauseId = addNode(parent, num, SubsectionNodeType.CLAUSE, text)
                    }
                }
                subclauseMatch != null && currentClauseId != null -> {
                    val num = "(${subclauseMatch.groupValues[1]})"
                    val body = subclauseMatch.groupValues[2].trim()
                    val text = if (body.isNotEmpty()) "$num $body" else num
                    addNode(currentClauseId, num, SubsectionNodeType.SUBCLAUSE, text)
                }
                clauseMatch != null -> {
                    val num = "(${clauseMatch.groupValues[1]})"
                    val body = clauseMatch.groupValues[2].trim()
                    val text = if (body.isNotEmpty()) "$num $body" else num
                    currentClauseId = addNode(currentSubsectionId, num, SubsectionNodeType.CLAUSE, text)
                }
                provisoMatch != null -> {
                    val label = provisoMatch.groupValues[1]
                    val rest = provisoMatch.groupValues[2].trim()
                    val text = if (rest.isNotEmpty()) "$label $rest" else line
                    addNode(currentSubsectionId, "শর্ত", SubsectionNodeType.PROVISO, text)
                }
                explanationMatch != null -> {
                    val label = explanationMatch.groupValues[1]
                    val rest = explanationMatch.groupValues[2].trim()
                    val text = if (rest.isNotEmpty()) "$label: $rest" else line
                    addNode(currentSubsectionId, label, SubsectionNodeType.EXPLANATION, text)
                }
                exceptionMatch != null -> {
                    val label = exceptionMatch.groupValues[1]
                    val rest = exceptionMatch.groupValues[2].trim()
                    val text = if (rest.isNotEmpty()) "$label: $rest" else line
                    addNode(currentSubsectionId, label, SubsectionNodeType.EXCEPTION, text)
                }
                illustrationMatch != null -> {
                    val label = illustrationMatch.groupValues[1]
                    val rest = illustrationMatch.groupValues[2].trim()
                    val text = if (rest.isNotEmpty()) "$label: $rest" else line
                    addNode(currentSubsectionId, label, SubsectionNodeType.ILLUSTRATION, text)
                }
                else -> {
                    // Continuation line of active node or standalone introductory text
                    val idx = activeNodeIndex
                    if (idx != null) {
                        val prev = nodes[idx]
                        nodes[idx] = prev.copy(
                            subsectionText = "${prev.subsectionText}\n$line"
                        )
                    } else {
                        currentSubsectionId = addNode(null, "মূল পাঠ", SubsectionNodeType.SUBSECTION, line)
                    }
                }
            }
        }

        return nodes
    }

    /**
     * Checks how many numbered subsections (১), (২), (৩)... or (1), (2), (3)... are referenced in raw text
     * and verifies sequential completeness.
     */
    fun findMissingSequentialSubsections(fullText: String, parsedNodes: List<ParsedSubsectionNode>): List<String> {
        val topSubsections = parsedNodes.filter { it.nodeType == SubsectionNodeType.SUBSECTION }
        val numbers = topSubsections.mapNotNull { node ->
            val clean = node.subsectionNumber.replace("(", "").replace(")", "").replace(".", "").trim()
            BanglaNumberUtils.toEnglishDigits(clean).toIntOrNull()
        }.distinct().sorted()

        if (numbers.size <= 1) return emptyList()

        val missing = mutableListOf<String>()
        val min = 1
        val max = numbers.last()
        for (i in min..max) {
            if (i !in numbers) {
                missing.add("(${BanglaNumberUtils.toBanglaDigits(i)})")
            }
        }
        return missing
    }

    /**
     * Detects whether section text appears truncated mid-sentence or has unbalanced delimiters.
     */
    fun isTextLikelyTruncated(text: String, isRepealed: Boolean): Boolean {
        if (isRepealed) return false
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return true
        if (trimmed.endsWith("...") || trimmed.endsWith("…")) return true

        // Check unbalanced parentheses
        val openParens = trimmed.count { it == '(' }
        val closeParens = trimmed.count { it == ')' }
        if (kotlin.math.abs(openParens - closeParens) > 1) return true

        // Valid endings in Bengali or English statutory texts
        val lastChar = trimmed.last()
        val validEndings = charArrayOf('।', '.', ';', ')', ']', '"', '’', '”', '?', '!')
        // If very short (< 18 chars) and doesn't end with punctuation, likely cut off
        if (trimmed.length < 18 && lastChar !in validEndings) return true

        return false
    }
}
