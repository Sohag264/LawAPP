package com.example.utils

object BanglaNumberUtils {
    private val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val enDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    fun toBanglaDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            val idx = enDigits.indexOf(ch)
            if (idx != -1) {
                sb.append(bnDigits[idx])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toBanglaDigits(number: Int): String = toBanglaDigits(number.toString())
    fun toBanglaDigits(number: Long): String = toBanglaDigits(number.toString())

    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            val idx = bnDigits.indexOf(ch)
            if (idx != -1) {
                sb.append(enDigits[idx])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Generates normalized query variations (both Bengali and English numerals)
     * to ensure bidirectional search.
     */
    fun getNormalizedQueries(query: String): List<String> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val bn = toBanglaDigits(trimmed)
        val en = toEnglishDigits(trimmed)

        return if (bn == en) {
            listOf(trimmed)
        } else {
            listOf(trimmed, bn, en).distinct()
        }
    }
}
