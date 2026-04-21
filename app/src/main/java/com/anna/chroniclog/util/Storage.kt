package com.anna.chroniclog.util

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.storage

// Referenced
// https://firebase.google.com/docs/storage/android/start
class Storage {
    private val storage = Firebase.storage

    fun uploadSymptomImage(
        uid: String,
        symptomId: String,
        localUri: Uri,
        onSuccess: (downloadUrl: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // create storage reference fc8 storage.kt
        val storageRef = storage.reference.child("users/$uid/symptoms/$symptomId.jpg")
        storageRef.putFile(localUri)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        onSuccess(downloadUri.toString())
                    }
                    .addOnFailureListener { e ->
                        Log.w("StorageHelper", "Failed to get download URL", e)
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("StorageHelper", "Upload failed", e)
                onFailure(e)
            }
    }

    //fun uploadImage() {}

    fun deleteSymptomImage(
        uid: String,
        symptomId: String,
        localUri: Uri,
        onSuccess: (downloadUrl: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storageRef = storage.reference.child("users/$uid/symptoms/$symptomId.jpg")
        storageRef.delete()
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "Deleted $localUri")
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "Delete FAILED of $localUri")
            }
    }
}