package com.anna.chroniclog.util

import android.content.Context
import android.graphics.Canvas
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
        symptoms: Map<String, Int>,
        remedies: List<Remediation>
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        var y = 50f

        fun checkNewLine(increment: Float) {
            y += increment
            if (y > 800) { // Start new page if out of space
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
        }

        // Header
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Patient Health Summary", 40f, y, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        checkNewLine(30f)
        canvas.drawText("Age: $age | Sex: $sex", 40f, y, paint)

        // Chronic Illnesses
        checkNewLine(25f)
        paint.isFakeBoldText = true
        canvas.drawText("Diagnosed Conditions:", 40f, y, paint)
        paint.isFakeBoldText = false
        val illnessText = if (illnesses.isEmpty()) "None reported" else illnesses.joinToString(", ")
        canvas.drawText(illnessText, 200f, y, paint)

        // Medications
        checkNewLine(40f)
        paint.isFakeBoldText = true
        canvas.drawText("Active Medications:", 40f, y, paint)
        paint.isFakeBoldText = false
        meds.filter { it.currentlyTaking }.forEach {
            checkNewLine(20f)
            canvas.drawText("- ${it.name}: ${it.dosage} (${it.frequency})", 60f, y, paint)
        }

        // Trends (Symptom Frequencies)
        checkNewLine(40f)
        paint.isFakeBoldText = true
        canvas.drawText("Symptom Trends (Frequency):", 40f, y, paint)
        paint.isFakeBoldText = false
        symptoms.forEach { (name, count) ->
            checkNewLine(20f)
            canvas.drawText("- $name: reported $count times", 60f, y, paint)
        }

        // Remedies
        checkNewLine(40f)
        paint.isFakeBoldText = true
        canvas.drawText("Reported Remedies:", 40f, y, paint)
        paint.isFakeBoldText = false
        remedies.takeLast(10).forEach { // Last 10 remedies
            checkNewLine(20f)
            canvas.drawText("- ${it.name}: ${it.outcome}", 60f, y, paint)
        }

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "ChronicLog_Report.pdf")
        pdfDocument.writeTo(file.outputStream())
        pdfDocument.close()
        return file
    }
}