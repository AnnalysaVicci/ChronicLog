package com.anna.chroniclog.model

import java.util.UUID

data class Remediation(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    //val description: String = "",
    val outcome: String = "",
    val logId: String = ""
)