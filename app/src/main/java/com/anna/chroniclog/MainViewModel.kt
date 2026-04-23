package com.anna.chroniclog

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anna.chroniclog.api.FdaApi
import com.anna.chroniclog.api.FdaDrug
import com.anna.chroniclog.api.FdaReaction
import com.anna.chroniclog.api.FdaRepository
import com.anna.chroniclog.data.HealthRepository
import com.anna.chroniclog.model.ChatMessage
import com.anna.chroniclog.model.LogEntry
import com.anna.chroniclog.model.Medication
import com.anna.chroniclog.model.Symptom
import com.anna.chroniclog.model.Remediation
import com.google.android.play.integrity.internal.u
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val healthRepository = HealthRepository()

    private val _userAge = MutableLiveData<Int>()
    val userAge: LiveData<Int> get() = _userAge
    private val _userSex = MutableLiveData<String>()
    val userSex: LiveData<String> get() = _userSex
    private val _userIssues = MutableLiveData<String>()
    val userIssues: LiveData<String> get() = _userIssues

    private val _chronicIllnesses = MutableLiveData<List<String>>(emptyList())
    val chronicIllnesses: LiveData<List<String>> get() = _chronicIllnesses

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

    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> get() = _chatMessages

    init {
        // load initial data
        loadUserProfile()
        loadLogs()
        loadMedications()
        loadSymptoms()
        loadRemediations()
        // start chat
        startChatService()
    }

    fun saveUserProfile(age: Int, sex: String, illnesses: List<String>) {
        _userAge.value = age
        _userSex.value = sex
        _chronicIllnesses.value = illnesses

        healthRepository.saveUserData(age, sex)
        healthRepository.saveChronicIllnesses(illnesses)
    }

    fun loadUserProfile() {
        healthRepository.loadUserData { age, sex, illnesses ->
            _userAge.postValue(age)
            _userSex.postValue(sex)
            _chronicIllnesses.postValue(illnesses ?: emptyList())
        }
    }

    fun addChronicIllness(illness: String) {
        val current = _chronicIllnesses.value ?: emptyList()
        if (!current.contains(illness)) {
            val updated = current + illness
            _chronicIllnesses.value = updated
            healthRepository.saveChronicIllnesses(updated)
        }
    }
    fun removeChronicIllness(illness: String) {
        val current = _chronicIllnesses.value ?: emptyList()
        val updated = current.filter { it != illness }
        _chronicIllnesses.value = updated
        healthRepository.saveChronicIllnesses(updated)
    }


    // LOGS
    fun addLog(newLog: LogEntry) {
        val currentList = _logs.value ?: emptyList()
        // _logs.value = currentList + newLog
        val updatedList = currentList + newLog
        _logs.value = updatedList.sortedByDescending { it.timestamp }

        //healthRepository.saveLog(newLog)
        healthRepository.saveNewLog(newLog)
    }
    private fun loadLogs() {
        healthRepository.loadLogs { logs ->
            //_logs.postValue(logs)
            _logs.postValue(logs.sortedByDescending { it.timestamp }) // sorts by date for UI
        }
    }
    fun deleteLog(logId: String) {
        _logs.value = _logs.value?.filter { it.id != logId}
        // firestore
        healthRepository.deleteLog(logId)
    }
    fun updateLog(updatedLog: LogEntry, removedSymptomIds: List<String>, removedRemediationIds: List<String>) {
        // update _logs LiveData
        _logs.value = _logs.value?.map {
            if (it.id == updatedLog.id) updatedLog else it
        }

        // persist log changes
        healthRepository.saveLog(updatedLog)

        // remove symptom from log in Firestore
        removedSymptomIds.forEach { id ->
            healthRepository.deleteSymptomFromLog(updatedLog.id, id)
        }

        // remove remediation from log in Firestore
        removedRemediationIds.forEach { id ->
            healthRepository.deleteRemediationFromLog(updatedLog.id, id)
        }

        // update _symptoms and _remediations LiveData so UI is updated
        _symptoms.value = _symptoms.value?.filterNot { it.id in removedSymptomIds}
        _remediations.value = _remediations.value?.filterNot { it.id in removedRemediationIds }
    }

    // MEDICATION
    fun addMedication(newMed: Medication) {
        val currentList = _medications.value ?: emptyList()
        _medications.value = currentList + newMed
        // firestore
        healthRepository.saveMedication(newMed)
    }
    fun deleteMedication(medId: String) {
        _medications.value = _medications.value?.filter { it.id != medId}
        healthRepository.deleteMedication(medId)
    }
    fun updateMedication(updatedMed: Medication) {
        _medications.value = _medications.value?.map {
            if (it.id == updatedMed.id) updatedMed else it
        }
    }
    private fun loadMedications() {
        healthRepository.loadMedications { meds ->
            _medications.postValue(meds)
        }
    }

    // SYMPTOMS (add, delete, update, load, uploadSymptomImage)
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
    fun uploadSymptomImage(
        symptomId: String,
        localUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        healthRepository.uploadSymptomImage(symptomId, localUri, onSuccess, onFailure)
    }

    // REMEDIATION (add, delete, update, load)
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

    // handle temp symptom and remediaiton data
    fun addTempSymptom(symptom: Symptom) {
        val current = _tempSymptoms.value ?: emptyList()
        _tempSymptoms.value = current + symptom
    }
    fun removeTempSymptom(symptomId: String) {
        _tempSymptoms.value = _tempSymptoms.value?.filter { it.id != symptomId }
    }
    fun addTempRemediation(remediation: Remediation) {
        val current = _tempRemediations.value ?: emptyList()
        _tempRemediations.value = current + remediation
    }
    fun removeTempRemediation(remediationId: String) {
        _tempRemediations.value = _tempRemediations.value?.filter { it.id != remediationId }
    }
    // Call this when the Log is finally saved or cancelled to clear the "cart"
    fun clearTempData() {
        _tempSymptoms.value = emptyList()
        _tempRemediations.value = emptyList()
    }

    // AUTOCOMPLETE SEARCH RESULTS
    // gets result of drug/reaction search and posts to LiveData to be observed
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

    // CHAT
    // gets chat data and posts to LiveData to be observed
    fun startChatService() {
        healthRepository.observeGeneralChat { messages ->
            _chatMessages.postValue(messages)
        }
    }
    fun sendChatMessage(message: ChatMessage) {
        healthRepository.sendMessage(message)
    }
}