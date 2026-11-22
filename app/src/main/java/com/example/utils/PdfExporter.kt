package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    private const val PAGE_WIDTH = 595 // Standard A4 in points (72 dpi)
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    fun exportLawToPdf(
        context: Context,
        lawTitleBn: String,
        lawTitleEn: String,
        actDetails: String,
        sections: List<Pair<String, String>>, // Pair of (Section Title, Section Content)
        officialSourceUrl: String
    ): File? {
        return try {
            val pdfDir = File(context.cacheDir, "laws_pdf").apply { mkdirs() }
            val cleanTitle = lawTitleEn.replace(Regex("[^a-zA-Z0-9]"), "_").take(25)
            val file = File(pdfDir, "Law_${cleanTitle}_${System.currentTimeMillis()}.pdf")

            val document = PdfDocument()

            val customTypeface = try {
                ResourcesCompat.getFont(context, R.font.hind_siliguri) ?: Typeface.DEFAULT
            } catch (e: Exception) {
                Typeface.DEFAULT
            }

            val titlePaint = Paint().apply {
                typeface = Typeface.create(customTypeface, Typeface.BOLD)
                textSize = 18f
                color = Color.rgb(0, 77, 64) // Emerald Legal Green
                isAntiAlias = true
            }

            val subTitlePaint = Paint().apply {
                typeface = Typeface.create(customTypeface, Typeface.NORMAL)
                textSize = 12f
                color = Color.rgb(90, 90, 90)
                isAntiAlias = true
            }

            val sectionTitlePaint = Paint().apply {
                typeface = Typeface.create(customTypeface, Typeface.BOLD)
                textSize = 13f
                color = Color.rgb(13, 71, 161) // Deep Blue Accent
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                typeface = Typeface.create(customTypeface, Typeface.NORMAL)
                textSize = 10.5f
                color = Color.rgb(33, 33, 33)
                isAntiAlias = true
            }

            val footerPaint = Paint().apply {
                typeface = Typeface.create(customTypeface, Typeface.ITALIC)
                textSize = 8.5f
                color = Color.rgb(120, 120, 120)
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.rgb(200, 200, 200)
                strokeWidth = 1f
            }

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var yPos = MARGIN + 20f

            fun drawHeaderAndFooter(c: Canvas, pNum: Int) {
                // Header accent bar
                val headerPaint = Paint().apply { color = Color.rgb(0, 77, 64) }
                c.drawRect(MARGIN, MARGIN - 15f, PAGE_WIDTH - MARGIN, MARGIN - 12f, headerPaint)

                // Footer
                val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                val footerText = "সরকারি তথ্যসূত্র: $officialSourceUrl | প্রস্তুত: $dateStr"
                c.drawText(footerText, MARGIN, PAGE_HEIGHT - MARGIN + 10f, footerPaint)
                val pText = "পৃষ্ঠা $pNum"
                val pWidth = footerPaint.measureText(pText)
                c.drawText(pText, PAGE_WIDTH - MARGIN - pWidth, PAGE_HEIGHT - MARGIN + 10f, footerPaint)
            }

            drawHeaderAndFooter(canvas, pageNumber)

            // Draw Document Title on Page 1
            canvas.drawText(lawTitleBn, MARGIN, yPos, titlePaint)
            yPos += 22f
            if (lawTitleEn.isNotEmpty()) {
                canvas.drawText(lawTitleEn, MARGIN, yPos, subTitlePaint)
                yPos += 18f
            }
            if (actDetails.isNotEmpty()) {
                canvas.drawText(actDetails, MARGIN, yPos, subTitlePaint)
                yPos += 18f
            }
            canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, linePaint)
            yPos += 20f

            fun splitIntoLines(text: String, paint: Paint, maxWidth: Float): List<String> {
                val lines = mutableListOf<String>()
                val paragraphs = text.split("\n")
                for (para in paragraphs) {
                    if (para.isEmpty()) {
                        lines.add("")
                        continue
                    }
                    val words = para.split(" ")
                    var currentLine = StringBuilder()
                    for (word in words) {
                        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                        if (paint.measureText(testLine) <= maxWidth) {
                            currentLine = StringBuilder(testLine)
                        } else {
                            if (currentLine.isNotEmpty()) {
                                lines.add(currentLine.toString())
                            }
                            currentLine = StringBuilder(word)
                        }
                    }
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine.toString())
                    }
                }
                return lines
            }

            for ((sTitle, sContent) in sections) {
                // Check space for section header
                if (yPos > PAGE_HEIGHT - MARGIN - 60f) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    drawHeaderAndFooter(canvas, pageNumber)
                    yPos = MARGIN + 20f
                }

                // Draw section heading
                canvas.drawText(sTitle, MARGIN, yPos, sectionTitlePaint)
                yPos += 18f

                // Draw content lines
                val lines = splitIntoLines(sContent, bodyPaint, CONTENT_WIDTH)
                for (line in lines) {
                    if (yPos > PAGE_HEIGHT - MARGIN - 25f) {
                        document.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        drawHeaderAndFooter(canvas, pageNumber)
                        yPos = MARGIN + 20f
                    }
                    if (line.isNotEmpty()) {
                        canvas.drawText(line, MARGIN, yPos, bodyPaint)
                    }
                    yPos += 15f
                }
                yPos += 12f // Extra gap between sections
            }

            document.finishPage(page)

            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun sharePdf(context: Context, file: File, subject: String = "আইন নথি - Bangladesh Laws") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, "বাংলাদেশের আইন অ্যাপ থেকে রফতানিকৃত PDF নথি।")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "পিডিএফ শেয়ার করুন"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
