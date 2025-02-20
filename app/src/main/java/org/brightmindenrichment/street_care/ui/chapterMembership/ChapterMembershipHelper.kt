package org.brightmindenrichment.street_care.ui.chapterMembership

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.brightmindenrichment.street_care.ui.user.UserType

fun checkUserChapterMembership(
    userId: String,
    onResult: (UserType?) -> Unit
) {
    val userDocRef = Firebase.firestore.collection("users").document(userId)
    userDocRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val userType = document.getString("Type")
                if (userType == "Chapter Member") {
                    onResult(UserType.CHAPTER_MEMBER)
                } else {
                    onResult(UserType.REGISTERED_USER)
                }
            } else {
                onResult(null)
            }
        }
        .addOnFailureListener {
            onResult(null)
        }
}