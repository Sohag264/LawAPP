package com.example.utils

import java.security.MessageDigest

object HashUtils {
    fun sha256(input: String): String {
        val normalized = input.trim().replace(Regex("\\s+"), " ")
        val bytes = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
