package com.anna.chroniclog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anna.chroniclog.api.FdaApi
import com.anna.chroniclog.api.FdaDrug
import com.anna.chroniclog.api.FdaReaction
import com.anna.chroniclog.api.FdaRepository
import com.anna.chroniclog.data.HealthRepository
import com.anna.chroniclog.model.LogEntry
import com.anna.chroniclog.model.Medication
import com.anna.chroniclog.model.Symptom
import com.anna.chroniclog.model.Remediation
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val healthRepository = HealthRepository()

    private val _userAge = MutableLiveData<Int>()
    val userAge: LiveData<Int> get() = _userAge

    private val _userSex = MutableLiveData<String>()
    val userSex: LiveData<String> get() = _userSex

    private val _logs = MutableLiveData<List<LogEntry>>()
    val logs: LiveData<List<LogEntry>> get() = _logs

    private val _medications = MutableLiveData<List<Medication>>()
    val medications: LiveData<List<Medication>> get() = _medications

    private val _symptoms = MutableLiveData<List<Symptom>>(emptyList())
    val symptoms: LiveData<List<Symptom>> get() = _symptoms

    private val _remediations = MutableLiveData<List<Remediation>>(emptyList())
    val remediations: LiveData<List<Remediation>> get() = _remediations


    private val _tempSymptoms = MutableLiveData<List<Symptom>>(emptyList())
    val tempSymptoms: LiveData<List<Symptom>> get() = _tempSymptoms


    private val _tempRemediations = MutableLiveData<List<Remediation>>(emptyList())
    val tempRemediations: LiveData<List<Remediation>> get() = _tempRemediations


    private val fdaApi = FdaApi.create()
    private val fdaRepository = FdaRepository(fdaApi)
    private val _drugSuggestions = MutableLiveData<List<FdaDrug>>()
    val drugSuggestions: LiveData<List<FdaDrug>> get() = _drugSuggestions
    private val _reactionSuggestions = MutableLiveData<List<FdaReaction>>()
    val reactionSuggestions: LiveData<List<FdaReaction>> get() = _reactionSuggestions


    init {
        // load initial data
        loadUserProfile()
        loadLogs()
        loadMedications()
        loadSymptoms()
        loadRemediations()
        //loadDefaultLogs()
        //loadDefaultMeds()
        //loadDefaultSymptoms()
        //loadDefaultRemediations()
    }

    fun saveUserProfile(age: Int, sex: String) {
        _userAge.value = age
        _userSex.value = sex
        healthRepository.saveUserData(age, sex)
    }

    fun loadUserProfile() {
        healthRepository.loadUserData { age, sex ->
            _userAge.postValue(age)
            _userSex.postValue(sex)
        }
    }

    fun addLog(newLog: LogEntry) {
        // save log
        val currentList = _logs.value ?: emptyList()
        _logs.value = currentList + newLog

        // save symptoms from log to main symptoms list
        val currentSymptoms = _symptoms.value ?: emptyList()
        _symptoms.value = currentSymptoms + newLog.symptoms

        // firestore
        healthRepository.saveLog(newLog)
    }
    fun deleteLog(logId: String) {
        _logs.value = _logs.value?.filter { it.id != logId}
        // firestore
        healthRepository.deleteLog(logId)
    }
    fun updateLog(updatedLog: LogEntry) {
        _logs.value = _logs.value?.map {
            if (it.id == updatedLog.id) updatedLog else it
        }
    }
    private fun loadLogs() {
        healthRepository.loadLogs { logs ->
            _logs.postValue(logs)
        }
    }

    // handle medication data
    fun addMedication(newMed: Medication) {
        val currentList = _medications.value ?: emptyList()
        _medications.value = currentList + newMed
        // firestore
        healthRepository.saveMedication(newMed)
    }
    fun deleteMedication(medId: String) {
        _medications.value = _medications.value?.filter { it.id != medId}
        // firestore
        //healthRepository.deleteMedication(medId)
    }
    fun updateMedication(updatedMed: Medication) {
        _medications.value = _medications.value?.map {
            if (it.id == updatedMed.id) updatedMed else it
        }
    }
    // FireStore data
    private fun loadMedications() {
        healthRepository.loadMedications { meds ->
            _medications.postValue(meds)
        }
    }

    // SYMPTOMS
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
    private fun loadSymptoms() {
        healthRepository.loadSymptoms { symptoms ->
            _symptoms.postValue(symptoms)
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
    private fun loadRemediations() {
        healthRepository.loadRemediations { remediations ->
            _remediations.postValue(remediations)
        }
    }

    // handle temp symptom data
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
    fun searchReactions(name: String) {
        viewModelScope.launch {
            val results = fdaRepository.searchReaction(name)
            _reactionSuggestions.postValue(results)
        }
    }
}