package com.anna.chroniclog.model
import java.util.UUID

data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val symptoms: List<Symptom> = emptyList(),
    val remediations: List<Remediation> = emptyList(),
    val sentiment: String = "",
    val notes: String = ""
)