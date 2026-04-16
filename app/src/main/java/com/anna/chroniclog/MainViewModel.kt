package com.anna.chroniclog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anna.chroniclog.api.FdaApi
import com.anna.chroniclog.api.FdaDrug
import com.anna.chroniclog.api.FdaRepository
import com.anna.chroniclog.model.LogEntry
import com.anna.chroniclog.model.Medication
import com.anna.chroniclog.model.Symptom
import com.anna.chroniclog.model.Remediation
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // private mutable data (so only the ViewModel can change it)
    private val _logs = MutableLiveData<List<LogEntry>>()
    val logs: LiveData<List<LogEntry>> get() = _logs
    private val _medications = MutableLiveData<List<Medication>>()
    val medications: LiveData<List<Medication>> get() = _medications
    private val _symptoms = MutableLiveData<List<Symptom>>(emptyList())
    val symptoms: LiveData<List<Symptom>> get() = _symptoms
    private val _remediations = MutableLiveData<List<Remediation>>(emptyList())
    val remediations: LiveData<List<Remediation>> get() = _remediations

    // temp storage for a log in progress
    private val _tempSymptoms = MutableLiveData<List<Symptom>>(emptyList())
    val tempSymptoms: LiveData<List<Symptom>> get() = _tempSymptoms

    private val _tempRemediations = MutableLiveData<List<Remediation>>(emptyList())
    val tempRemediations: LiveData<List<Remediation>> get() = _tempRemediations

    private val fdaApi = FdaApi.create()
    private val fdaRepository = FdaRepository(fdaApi)
    private val _drugSuggestions = MutableLiveData<List<FdaDrug>>()
    val drugSuggestions: LiveData<List<FdaDrug>> get() = _drugSuggestions


    init {
        // load initial data immediately
        loadDefaultLogs()
        loadDefaultMeds()
        loadDefaultSymptoms()
        loadDefaultRemediations()
    }

    private fun loadDefaultLogs() {
        val testLogs = listOf(
            LogEntry(id = "1", date = "April 12, 2026", sentiment = "😊"),
            LogEntry(id = "2", date = "April 11, 2026", sentiment = "😐"),
            LogEntry(id = "3", date = "April 11, 2026", sentiment = "😐")
        )
        _logs.value = testLogs
    }

    private fun loadDefaultMeds() {
        val testMeds = listOf(
            Medication(id = "1", name="Lamictal", dosage = "20mg", frequency = "1/day", adherence = "I never miss a dose", startDate = "10/10/2010", endDate = "10/10/2011", currentlyTaking = false),
            Medication(id = "2", name="Prozac", dosage = "5mg", frequency = "2/day", adherence = "I never miss a dose", startDate = "10/10/2012", endDate = "10/20/2012", currentlyTaking = false),
            Medication(id = "3", name="Birth Control", dosage = "3mg", frequency = "1/day", adherence = "I never miss a dose", startDate = "10/10/2010", currentlyTaking = true)
        )
        _medications.value = testMeds
    }

    private fun loadDefaultSymptoms() {
        val testSymptoms = listOf(
            Symptom(id = "1", name = "Headache", severity=6, logId = "2"),
            Symptom(id = "2", name = "Nausea", severity=4, logId = "2"),
            Symptom(id = "3", name = "Insomnia", severity=9, logId = "2")
        )
        _symptoms.value = testSymptoms
    }

    private fun loadDefaultRemediations() {
        val testRemediations = listOf(
            Remediation(id = "1", name = "Medication", outcome = "Helped", logId = "2"),
            Remediation(id = "2", name = "Physical Therapy", outcome = "Hurt", logId = "2"),
            Remediation(id = "3", name = "Dietary", outcome = "Hurt", logId = "3")
        )
        _remediations.value = testRemediations
    }

    fun addLog(newLog: LogEntry) {
        // save log
        val currentList = _logs.value ?: emptyList()
        _logs.value = currentList + newLog

        // save symptoms from log to main symptoms list
        val currentSymptoms = _symptoms.value ?: emptyList()
        _symptoms.value = currentSymptoms + newLog.symptoms
    }
    fun deleteLog(logId: String) {
        _logs.value = _logs.value?.filter { it.id != logId}
    }
    fun updateLog(updatedLog: LogEntry) {
        _logs.value = _logs.value?.map {
            if (it.id == updatedLog.id) updatedLog else it
        }
    }

    // handle medication data
    fun addMedication(newMed: Medication) {
        val currentList = _medications.value ?: emptyList()
        _medications.value = currentList + newMed
    }
    fun deleteMedication(medId: String) {
        _medications.value = _medications.value?.filter { it.id != medId}
    }
    fun updateMedication(updatedMed: Medication) {
        _medications.value = _medications.value?.map {
            if (it.id == updatedMed.id) updatedMed else it
        }
    }

    fun addSymptom(newSymptom: Symptom) {
        val currentList = _symptoms.value ?: emptyList()
        _symptoms.value = currentList + newSymptom
    }
    fun deleteSymptom(symptomId: String) {
        _symptoms.value = _symptoms.value?.filter { it.id != symptomId}
    }
    fun updateSymptom(updatedSymptom: Symptom) {
        _symptoms.value = _symptoms.value?.map {
            if (it.id == updatedSymptom.id) updatedSymptom else it
        }
    }

    fun addRemediation(newRemediation: Remediation) {
        val currentList = _remediations.value ?: emptyList()
        _remediations.value = currentList + newRemediation
    }
    fun deleteRemediation(remediationId: String) {
        _remediations.value = _remediations.value?.filter { it.id != remediationId}
    }
    fun updateRemediation(updatedRemediation: Remediation) {
        _remediations.value = _remediations.value?.map {
            if (it.id == updatedRemediation.id) updatedRemediation else it
        }
    }

    // handle symptom data
    fun addTempSymptom(symptom: Symptom) {
        val current = _tempSymptoms.value ?: emptyList()
        _tempSymptoms.value = current + symptom
    }
    fun addTempRemediation(remediation: Remediation) {
        val current = _tempRemediations.value ?: emptyList()
        _tempRemediations.value = current + remediation
    }
    // Call this when the Log is finally saved or cancelled to clear the "cart"
    fun clearTempData() {
        _tempSymptoms.value = emptyList()
        _tempRemediations.value = emptyList()
    }

    // drug autosuggestion
    fun searchDrugs(name: String) {
        viewModelScope.launch {
            val results = fdaRepository.searchDrugs(name)
            _drugSuggestions.postValue(results)
        }
    }
}