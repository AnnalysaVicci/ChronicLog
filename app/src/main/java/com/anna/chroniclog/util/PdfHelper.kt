package com.anna.chroniclog.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.anna.chroniclog.model.*
import java.io.File

object PdfHelper {
    fun createMedicalReport(
        context: Context,
        age: Int,
        sex: String,
        illnesses: List<String>,
        meds: List<Medication>,
        symptoms: Map<String, Int>?,
        remedies: List<Remediation>
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        var y = 60f

        // Helper to draw a horizontal line
        fun drawDivider(currentY: Float) {
            paint.color = android.graphics.Color.LTGRAY
            paint.strokeWidth = 1f
            canvas.drawLine(40f, currentY, 555f, currentY, paint)
            paint.color = android.graphics.Color.BLACK // Reset to black
        }

        fun checkNewLine(increment: Float) {
            y += increment
            if (y > 780) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }
        }

        // --- TITLE SECTION ---
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.rgb(103, 58, 183) // Purple branding
        canvas.drawText("ChronicLog Medical Report", 40f, y, paint)

        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.GRAY
        checkNewLine(20f)
        canvas.drawText("Generated on: ${java.text.SimpleDateFormat("MMM dd, yyyy").format(java.util.Date())}", 40f, y, paint)

        // --- PATIENT INFO ---
        checkNewLine(40f)
        paint.textSize = 14f
        paint.color = android.graphics.Color.BLACK
        paint.isFakeBoldText = true
        canvas.drawText("Patient Demographics", 40f, y, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        checkNewLine(25f)
        canvas.drawText("Age: $age", 40f, y, paint)
        canvas.drawText("Sex: $sex", 150f, y, paint)

        checkNewLine(15f)
        drawDivider(y)

        // --- CHRONIC CONDITIONS ---
        checkNewLine(35f)
        paint.isFakeBoldText = true
        canvas.drawText("Chronic Issues", 40f, y, paint)
        paint.isFakeBoldText = false
        checkNewLine(25f)
        val illnessText = if (illnesses.isEmpty()) "No conditions reported." else illnesses.joinToString(", ")
        // Simple wrap check (basic)
        canvas.drawText(illnessText, 50f, y, paint)

        // --- MEDICATIONS ---
        checkNewLine(40f)
        paint.isFakeBoldText = true
        canvas.drawText("Active Medications", 40f, y, paint)
        paint.isFakeBoldText = false

        val activeMeds = meds.filter { it.currentlyTaking }
        if (activeMeds.isEmpty()) {
            checkNewLine(20f)
            canvas.drawText("No active medications listed.", 50f, y, paint)
        } else {
            activeMeds.forEach {
                checkNewLine(22f)
                canvas.drawText("• ${it.name}", 50f, y, paint)
                paint.textSize = 10f
                paint.color = android.graphics.Color.DKGRAY
                canvas.drawText("  ${it.dosage} - ${it.frequency}", 200f, y, paint)
                paint.textSize = 12f
                paint.color = android.graphics.Color.BLACK
            }
        }

        // --- SYMPTOM TRENDS ---
        checkNewLine(45f)
        paint.isFakeBoldText = true
        canvas.drawText("Symptom Frequency (Trends)", 40f, y, paint)
        paint.isFakeBoldText = false

        if (symptoms.isNullOrEmpty()) {
            checkNewLine(20f)
            canvas.drawText("Insufficient data for trends.", 50f, y, paint)
        } else {
            symptoms.forEach { (name, count) ->
                checkNewLine(22f)
                canvas.drawText("• $name", 50f, y, paint)
                canvas.drawText("$count times", 450f, y, paint)
            }
        }

        // --- FOOTER ---
        paint.textSize = 9f
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("Confidential Medical Document - ChronicLog App", 40f, 820f, paint)

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "ChronicLog_Report.pdf")
        pdfDocument.writeTo(file.outputStream())
        pdfDocument.close()
        return file
    }
}