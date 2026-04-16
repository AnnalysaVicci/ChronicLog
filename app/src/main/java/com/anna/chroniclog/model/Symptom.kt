package com.anna.chroniclog.model

import java.util.UUID

data class Symptom(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val severity: Int = 0,    // 1-5, useful for trend charts later
    val logId: String = ""
)