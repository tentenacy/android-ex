package com.tenutz.firestorecrud.util

import android.net.Uri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.tenutz.firestorecrud.BuildConfig
import kotlinx.coroutines.tasks.await

val auth get() = Firebase.auth
val user get() = auth.currentUser
val db get() = Firebase.firestore
val storage get() = Firebase.storage(BuildConfig.STORAGE_BASE_URL)

suspend fun uploadImageIfExists(uri: Uri?) = uri?.let {
    val storageRef = storage.reference
    val riversRef = storageRef.child("images/${user!!.uid}/${it.lastPathSegment}")
    val uploadTask = riversRef.putFile(it)
    uploadTask
        .continueWith { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            riversRef.downloadUrl
        }.await()
}