package com.anna.chroniclog.model

data class Remediation(
    val id: String = "",
    val name: String = "",
    val outcome: String = "",  // "Helped", "Hurt", "Neutral"
    val logId: String = ""
)