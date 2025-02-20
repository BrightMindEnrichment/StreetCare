package org.brightmindenrichment.street_care.ui.chapterMembership

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

enum class MembershipStatus {
    CHAPTER_MEMBER,
    NON_CHAPTER_MEMBER,
    ERROR
}

fun checkUserChapterMembership(
    userId: String,
    onResult: (MembershipStatus) -> Unit
) {
    val userDocRef = Firebase.firestore.collection("users").document(userId)
    userDocRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val userType = document.getString("Type")
                if (userType == "Chapter Member") {
                    onResult(MembershipStatus.CHAPTER_MEMBER)
                } else {
                    onResult(MembershipStatus.NON_CHAPTER_MEMBER)
                }
            } else {
                onResult(MembershipStatus.ERROR)
            }
        }
        .addOnFailureListener {
            onResult(MembershipStatus.ERROR)
        }
}