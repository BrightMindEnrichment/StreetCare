package org.brightmindenrichment.street_care.ui.user

import com.google.firebase.auth.FirebaseUser


object User {
    var userModel = UserModel()
}

data class UserModel(
    var currentUser: FirebaseUser? = null,
    var userName: String? = null,
    var imageUri: String? = null
)