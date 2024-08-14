package org.brightmindenrichment.street_care.ui.user

import android.content.ContentValues
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class UserRepository {
    suspend fun fetchUserData(): UserModel {
        val userModel = UserModel()
        val currentUser = Firebase.auth.currentUser
        userModel.currentUser = currentUser
        val storageRef = Firebase.storage.reference

        if (currentUser == null) {
            Log.d(ContentValues.TAG, "No user")
            return userModel
        }

        Log.d(ContentValues.TAG, "getUserData")
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(currentUser.uid)

        try {
            val document = docRef.get().await()
            if (document != null && document.data != null) {
                val user = document.data!!
                userModel.userName =
                    user["username"]?.toString() ?: currentUser.displayName.toString()
                Log.d(
                    "ContentValues",
                    "UserSingleton.userModel: suspend :: ${UserSingleton.userModel}"
                )
            } else {
                Log.d(ContentValues.TAG, "No such document user")
            }

            val fileName = "profile.jpg"
            val imageRef = storageRef.child("users").child(currentUser.uid).child(fileName)
            val uri = imageRef.downloadUrl.await()
            userModel.imageUri = uri.toString()
            Log.d(ContentValues.TAG, "Get image: success")
        } catch (e: Exception) {
            Log.d(ContentValues.TAG, "Fetch user data failed with ", e)
        }

        Log.d(ContentValues.TAG, "fetchUserData finish: " + userModel.userName)
        return userModel
    }
}