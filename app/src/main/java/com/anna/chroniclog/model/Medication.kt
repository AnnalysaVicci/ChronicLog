package com.anna.chroniclog.model

data class Medication(
    val id: String = "",
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val adherence: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val currentlyTaking: Boolean = false,
    var isExpanded: Boolean = false
)