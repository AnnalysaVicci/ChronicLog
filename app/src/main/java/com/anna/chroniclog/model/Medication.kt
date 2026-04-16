package com.anna.chroniclog.model
import java.util.UUID

data class Medication(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val adherence: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val currentlyTaking: Boolean = false,
    var isExpanded: Boolean = false
)