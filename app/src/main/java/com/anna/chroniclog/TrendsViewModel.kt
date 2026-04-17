package com.anna.chroniclog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow

import com.anna.chroniclog.data.HealthRepository
import com.anna.chroniclog.model.LogEntry
import com.anna.chroniclog.model.Medication
import com.anna.chroniclog.model.Symptom
import com.anna.chroniclog.model.Remediation
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class TrendsViewModel : ViewModel() {
    private val healthRepository = HealthRepository()
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val userId get() = auth.currentUser?.uid

    private val _symptomFrequency = MutableStateFlow<Map<String, Int>>(emptyMap())
    val symptomFrequency: StateFlow<Map<String, Int>> = _symptomFrequency


}