package com.anna.chroniclog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import com.anna.chroniclog.data.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class TrendsViewModel : ViewModel() {
    private val healthRepository = HealthRepository()

    // use StateFlow here because it's standard for analysis/logic
    private val _symptomFrequency = MutableStateFlow<Map<String, Int>>(emptyMap())
    val symptomFrequency: StateFlow<Map<String, Int>> = _symptomFrequency

    // this function asks the Repository for symptom_stats document
    fun loadSymptomFrequencies() {
        viewModelScope.launch {
            healthRepository.getSymptomSummary { map ->
                _symptomFrequency.value = map
            }
        }
    }
}