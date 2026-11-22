package com.example.data.importer

import com.example.data.entity.VerificationStatus
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.InflaterInputStream

data class PdfExtractionResult(
    val extractedText: String,
    val isScannedOrRequiresOcr: Boolean,
    val verificationStatus: String,
    val pageCountEstimate: Int,
    val warningMessage: String?
)

/**
 * Memory-safe PDF text extraction helper.
 * Reads PDF streams in chunks to detect embedded text operators vs scanned image-only PDFs.
 * Per data integrity rules, scanned PDFs requiring OCR are never auto-marked as VERIFIED.
 */
object PdfTextExtractor {

    private const val MAX_STREAM_BUFFER = 512 * 1024 // 512KB per stream chunk

    fun extractTextFromPdfStream(inputStream: InputStream): PdfExtractionResult {
        val buffered = BufferedInputStream(inputStream, 16 * 1024)
        val rawBytes = ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        var totalRead = 0
        val maxRead = 8 * 1024 * 1024 // Cap single-pass stream read at 8MB to prevent OOM

        while (true) {
            val read = buffered.read(chunk)
            if (read == -1) break
            if (totalRead + read > maxRead) {
                return PdfExtractionResult(
                    extractedText = "",
                    isScannedOrRequiresOcr = true,
                    verificationStatus = VerificationStatus.OCR_UNVERIFIED,
                    pageCountEstimate = 0,
                    warningMessage = "PDF ফাইলটি অত্যধিক বড় (৮ MB-এর বেশি)। স্ট্রিমিং বা খণ্ডিত HTML/JSON প্যাকেজ ব্যবহার করুন।"
                )
            }
            rawBytes.write(chunk, 0, read)
            totalRead += read
        }

        val pdfString = rawBytes.toString(Charsets.ISO_8859_1.name())
        if (!pdfString.startsWith("%PDF")) {
            return PdfExtractionResult(
                extractedText = "",
                isScannedOrRequiresOcr = false,
                verificationStatus = VerificationStatus.UNVERIFIED,
                pageCountEstimate = 0,
                warningMessage = "বৈধ PDF হেডার পাওয়া যায়নি।"
            )
        }

        val pageCount = Regex("/Type\\s*/Page\\b").findAll(pdfString).count()
        val hasImageStreams = pdfString.contains("/Subtype /Image") || pdfString.contains("/Subtype/Image")

        val textBuilder = StringBuilder()
        // Extract literal text inside PDF text objects BT ... ET or FlateDecode streams
        val streamRegex = Regex("stream\\r?\\n([\\s\\S]*?)\\r?\\nendstream")
        for (match in streamRegex.findAll(pdfString)) {
            val rawStream = match.groupValues[1].toByteArray(Charsets.ISO_8859_1)
            val decoded = tryInflate(rawStream) ?: rawStream
            if (decoded.size <= MAX_STREAM_BUFFER) {
                val content = String(decoded, Charsets.UTF_8)
                extractPdfTextOperators(content, textBuilder)
            }
        }

        val finalText = textBuilder.toString().trim()
        val requiresOcr = finalText.length < 30 && (hasImageStreams || pageCount > 0)

        return if (requiresOcr) {
            PdfExtractionResult(
                extractedText = finalText,
                isScannedOrRequiresOcr = true,
                verificationStatus = VerificationStatus.OCR_UNVERIFIED,
                pageCountEstimate = pageCount,
                warningMessage = "এটি একটি স্ক্যান করা (Image-based) PDF। OCR যাচাইকরণ ব্যতীত এই তথ্য 'Verified' হিসেবে সংরক্ষণ করা হবে না।"
            )
        } else {
            PdfExtractionResult(
                extractedText = finalText,
                isScannedOrRequiresOcr = false,
                verificationStatus = VerificationStatus.UNVERIFIED,
                pageCountEstimate = pageCount,
                warningMessage = if (finalText.isEmpty()) "PDF থেকে পাঠযোগ্য টেক্সট পাওয়া যায়নি।" else null
            )
        }
    }

    private fun tryInflate(bytes: ByteArray): ByteArray? {
        return try {
            InflaterInputStream(bytes.inputStream()).use { it.readBytes() }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractPdfTextOperators(streamContent: String, out: StringBuilder) {
        val tjRegex = Regex("\\(([^()\\\\]*(?:\\\\.[^()\\\\]*)*)\\)\\s*(?:Tj|')")
        var foundAny = false
        for (m in tjRegex.findAll(streamContent)) {
            val unescaped = m.groupValues[1]
                .replace("\\n", "\n")
                .replace("\\r", "")
                .replace("\\(", "(")
                .replace("\\)", ")")
                .replace("\\\\", "\\")
            if (unescaped.isNotBlank()) {
                out.append(unescaped).append(" ")
                foundAny = true
            }
        }
        if (foundAny) out.append("\n")
    }
}
