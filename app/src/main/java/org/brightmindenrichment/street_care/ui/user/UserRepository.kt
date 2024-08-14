package org.brightmindenrichment.street_care.ui.user


import android.content.ContentValues
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UserRepository {

    suspend fun fetchUserData(): UserModel {
        val userModel = UserModel()
        val currentUser = FirebaseAuth.getInstance().currentUser
        userModel.currentUser = currentUser
        val storageRef = FirebaseStorage.getInstance().reference

        if (currentUser == null) {
            Log.d(ContentValues.TAG, "No user")
            return userModel
        }

        try {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(currentUser.uid)
            val document: DocumentSnapshot = docRef.get().awaitTask()

            if (document.exists()) {
                val user = document.data
                if (!user.isNullOrEmpty()) {
                    userModel.userName = user["username"]?.toString() ?: currentUser.displayName.toString()
                    Log.d(ContentValues.TAG, "UserSingleton.userModel: suspend :: ${UserSingleton.userModel}")
                } else {
                    Log.d(ContentValues.TAG, "Document is empty")
                }
            } else {
                Log.d(ContentValues.TAG, "No such document")
            }

            val fileName = "profile.jpg"
            val imageRef = storageRef.child("users/${currentUser.uid}/$fileName")
            val uri = imageRef.downloadUrl.awaitTask()
            userModel.imageUri = uri.toString()

            Log.d(ContentValues.TAG, "Get image: success")
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error fetching user data", e)
        }

        Log.d(ContentValues.TAG, "fetchUserData finish: ${userModel.userName}")
        return userModel
    }
}

suspend fun <T> Task<T>.awaitTask(): T {
    return suspendCoroutine { continuation ->
        this.addOnSuccessListener { result ->
            continuation.resume(result)
        }
        this.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }
}