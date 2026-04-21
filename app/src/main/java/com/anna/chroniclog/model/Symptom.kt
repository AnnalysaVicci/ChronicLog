package com.anna.chroniclog.model

import java.util.UUID

data class Symptom(
    val id: String = UUID.randomUUID().toString(),
    //val id: String = "",
    val name: String = "",
    val severity: Int = 0,
    val imageUri: String = "",
    val logId: String = ""
)