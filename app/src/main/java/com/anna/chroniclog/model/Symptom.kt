package com.anna.chroniclog.model

import java.util.UUID

data class Symptom(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val severity: Int = 0,
    //val imgUri: String = "",
    val logId: String = ""
)