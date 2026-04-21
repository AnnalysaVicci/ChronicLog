package com.anna.chroniclog.data

import android.net.Uri
import android.util.Log
import com.anna.chroniclog.model.ChatMessage
import com.anna.chroniclog.model.LogEntry
import com.anna.chroniclog.model.Medication
import com.anna.chroniclog.model.Remediation
import com.anna.chroniclog.model.Symptom
import com.anna.chroniclog.util.Storage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

// firestore data

class HealthRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val userId get() = auth.currentUser?.uid
    private val storage = Storage()

    // User Profile
    fun saveUserData(age: Int, sex: String) {
        val uid = userId ?: return
        val userMap = hashMapOf(
            "age" to age,
            "sex" to sex,
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener { Log.d("Firestore", "User profile saved") }
            .addOnFailureListener { e -> Log.w("Firestore", "Error saving profile", e) }
    }

    fun loadUserData(onResult: (age: Int?, sex: String?) -> Unit) {
        val uid = userId ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onResult(doc.getLong("age")?.toInt(), doc.getString("sex"))
                } else {
                    onResult(null, null)
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error loading profile", e)
                onResult(null, null)
            }
    }

    // CHRONIC ILLNESS/ISSUES
    fun saveChronicIllnesses(illnesses: List<String>) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .update("chronicIllnesses", illnesses)
    }

    // MEDICATIONS (save, load, delete)
    fun saveMedication(med: Medication) {
        val uid = userId ?: return
        val medMap = hashMapOf(
            "id" to med.id,
            "name" to med.name,
            "dosage" to med.dosage,
            "frequency" to med.frequency,
            "adherence" to med.adherence,
            "startDate" to med.startDate,
            "endDate" to med.endDate,
            "currentlyTaking" to med.currentlyTaking
        )
        db.collection("users").document(uid)
            .collection("medications").document(med.id)
            .set(medMap)
            .addOnSuccessListener { Log.d("Firestore", "Medication saved") }
            .addOnFailureListener { e -> Log.w("Firestore", "Error saving medication", e) }
    }

    fun loadMedications(onResult: (List<Medication>) -> Unit) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("medications").get()
            .addOnSuccessListener { snapshot ->
                val meds = snapshot.documents.mapNotNull { doc ->
                    try {
                        Medication(
                            id = doc.getString("id") ?: doc.id,
                            name = doc.getString("name") ?: "",
                            dosage = doc.getString("dosage") ?: "",
                            frequency = doc.getString("frequency") ?: "",
                            adherence = doc.getString("adherence") ?: "",
                            startDate = doc.getString("startDate") ?: "",
                            endDate = doc.getString("endDate") ?: "",
                            currentlyTaking = doc.getBoolean("currentlyTaking") ?: false
                        )
                    } catch (e: Exception) {
                        Log.w("Firestore", "Error parsing medication", e)
                        null
                    }
                }
                onResult(meds)

                // could try firestore POJO
                //val meds = snapshot.toObjects(Medication::class.java)
                //onResult(meds)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error loading medications", e)
                onResult(emptyList())
            }
    }

    fun deleteMedication(medicationId: String) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("medications").document(medicationId)
            .delete()
            .addOnSuccessListener { Log.d("Firestore", "Medication deleted") }
            .addOnFailureListener { e -> Log.w("Firestore", "Error deleting medication", e) }
    }

    // LOGS (save, load, delete)
    fun saveLog(log: LogEntry) {
        val uid = userId ?: return

        val logMap = hashMapOf(
            "id" to log.id,
            "date" to log.date,
            "sentiment" to log.sentiment,
            "notes" to log.notes,
            "symptoms" to log.symptoms,
            "remediations" to log.remediations
        )
        db.collection("users").document(uid)
            .collection("logs").document(log.id)
            .set(logMap)
            .addOnSuccessListener { Log.d("Firestore", "Log saved") }
            .addOnFailureListener { e -> Log.w("Firestore", "Error saving log", e) }

        // save symptoms and remediations as subcollections of the log
        log.symptoms.forEach { saveSymptom(it, log.id) }
        log.remediations.forEach { saveRemediation(it, log.id) }

        // save symptom to symptom_stats document in firestore
        updateSymptomSummary(log.symptoms)
    }

    fun loadLogs(onResult: (List<LogEntry>) -> Unit) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("logs").get()
            .addOnSuccessListener { snapshot ->
                val logs = snapshot.documents.mapNotNull { doc ->
                    try {
                        LogEntry(
                            id = doc.getString("id") ?: doc.id,
                            date = doc.getString("date") ?: "",
                            sentiment = doc.getString("sentiment") ?: "",
                            notes = doc.getString("notes") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.w("Firestore", "Error parsing log", e)
                        null
                    }
                }
                onResult(logs)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error loading logs", e)
                onResult(emptyList())
            }
    }

    fun deleteLog(logId: String) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("logs").document(logId)
            .delete()
            .addOnSuccessListener { Log.d("Firestore", "Log deleted") }
            .addOnFailureListener { e -> Log.w("Firestore", "Error deleting log", e) }
    }

    // SYMPTOMS (save, load, delete, update)
    private fun saveSymptom(symptom: Symptom, logId: String) {
        val uid = userId ?: return
        val map = hashMapOf(
            "id" to symptom.id,
            "name" to symptom.name,
            "severity" to symptom.severity,
            "logId" to logId,
            "imageUri" to symptom.imageUri,
            "userId" to uid
        )
        db.collection("users").document(uid)
            .collection("logs").document(logId)
            .collection("symptoms").document(symptom.id)
            .set(map)
    }

    fun loadSymptoms(onResult: (List<Symptom>) -> Unit) {
        val uid = userId ?: return

        db.collectionGroup("symptoms")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val symptoms = snapshot.toObjects(Symptom::class.java)
                Log.d("Firestore", "Loaded ${symptoms.size} symptoms")
                onResult(symptoms)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to load symptoms: ${e.message}")
                onResult(emptyList())
            }
    }

    fun deleteSymptom(symptomId: String) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("symptoms").document(symptomId)
            .delete()
            .addOnSuccessListener { Log.d("Firestore", "Symptom deleted") }
            .addOnFailureListener { e -> Log.w("Firestore", "Error deleting Symptom", e) }
    }

    fun getSymptomSummary(onResult: (Map<String, Int>) -> Unit) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("aggregates").document("symptom_stats")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // firestore stores numbers as Long, but we need Int for the chart
                    val rawData = doc.get("frequencies") as? Map<String, Long> ?: emptyMap()
                    val convertedData = rawData.mapValues { it.value.toInt() }
                    onResult(convertedData)
                } else {
                    onResult(emptyMap())
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching summary", e)
                onResult(emptyMap())
            }
    }
    // searching for every symptom in every log will get expensive
    // symptom_stats will hold user's symptoms
    fun updateSymptomSummary(symptoms: List<Symptom>) {
        val uid = userId ?: return
        val statsRef = db.collection("users").document(uid)
            .collection("aggregates").document("symptom_stats")

        val updates = mutableMapOf<String, Any>()
        symptoms.forEach { symptom ->
            // This increment happens directly on the server!
            updates["frequencies.${symptom.name}"] = FieldValue.increment(1)
        }

        statsRef.update(updates).addOnFailureListener {
            // If the document doesn't exist yet, use .set() instead
            statsRef.set(updates)
        }
    }

    fun uploadSymptomImage(
        symptomId: String,
        localUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = userId ?: return
        storage.uploadSymptomImage(uid, symptomId, localUri, onSuccess, onFailure)
    }


    // REMEDIATIONS (save, load, delete)
    private fun saveRemediation(remediation: Remediation, logId: String) {
        val uid = userId ?: return
        val map = hashMapOf(
            "id" to remediation.id,
            "name" to remediation.name,
            "outcome" to remediation.outcome,
            "logId" to logId,
            "userId" to uid
        )
        db.collection("users").document(uid)
            .collection("logs").document(logId)
            .collection("remediations").document(remediation.id)
            .set(map)
    }
    fun loadRemediations(onResult: (List<Remediation>) -> Unit) {
        val uid = userId ?: return

        db.collectionGroup("remediations")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val remediations = snapshot.toObjects(Remediation::class.java)
                Log.d("Firestore", "Loaded ${remediations.size} remediations")
                onResult(remediations)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to load remediations: ${e.message}")
                onResult(emptyList())
            }
    }

    fun deleteRemediation(remediationId: String) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("remediations").document(remediationId)
            .delete()
            .addOnSuccessListener { Log.d("Firestore", "Remediation deleted") }
            .addOnFailureListener { e -> Log.w("Firestore", "Error deleting Remediation", e) }
    }

    // CHAT/MESSAGE
    fun sendMessage(message: ChatMessage) {
        db.collection("chat_general")
            .add(message)
            .addOnFailureListener { e -> Log.w("Firestore", "Error sending message", e) }
    }

    fun observeGeneralChat(onUpdate: (List<ChatMessage>) -> Unit) {
        db.collection("chat_general")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val messages = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
                onUpdate(messages)
            }
    }
    /*
    fun observeChatByTopic(topicId: String, onUpdate: (List<ChatMessage>) -> Unit) {
        db.collection("chats")
            .whereEqualTo("topicId", topicId) // Only get messages for this specific topic
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                val messages = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
                onUpdate(messages)
            }
    } */

}