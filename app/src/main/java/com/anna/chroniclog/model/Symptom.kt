package com.anna.chroniclog.model

data class Symptom(
    val id: String = "",
    val name: String = "",
    val severity: Int = 0,    // 1-5, useful for trend charts later
    val logId: String = ""
)