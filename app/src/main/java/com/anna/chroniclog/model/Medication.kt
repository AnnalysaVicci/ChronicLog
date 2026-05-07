package com.anna.chroniclog.model
import java.util.UUID

data class Medication(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val dosage: String = "",
    //val dosageValue: Double,
    //val dosageUnit: String,
    //val frequencyInterval: Int, // e.g., 8
    //val frequencyUnit: String,   // e.g., "Hours"
    val frequency: String = "",
    val adherence: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val currentlyTaking: Boolean = false,
    var isExpanded: Boolean = false
)