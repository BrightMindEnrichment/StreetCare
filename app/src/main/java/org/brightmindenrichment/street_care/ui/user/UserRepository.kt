package org.brightmindenrichment.street_care.ui.user

import android.content.ContentValues
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class UserRepository {
    fun fetchUserData(): UserModel {
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
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.data
                    if (!user.isNullOrEmpty()) {
                        userModel.userName =
                            user["username"]?.toString() ?: currentUser.displayName.toString()
                        Log.d(
                            ContentValues.TAG,
                            "currentUser " + currentUser.displayName.toString()
                        )
                    }
                } else {
                    Log.d(ContentValues.TAG, "No such document user")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }
        val fileName = "profile.jpg"
        val imageRef = storageRef.child("users").child(currentUser.uid).child(fileName)
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            if (uri != null) {
                userModel.imageUri = uri.toString()
                Log.d(ContentValues.TAG, "Get image:success")
            }
        }.addOnFailureListener {
            // Handle any errors
            Log.d(ContentValues.TAG, "No such document")
        }
        Log.d(ContentValues.TAG, "fetchUserData finish: " + userModel.userName)
        return userModel
    }
}