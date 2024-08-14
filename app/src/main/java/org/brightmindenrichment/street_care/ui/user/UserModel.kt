package org.brightmindenrichment.street_care.ui.user

import com.google.firebase.auth.FirebaseUser

data class UserModel(
    var currentUser: FirebaseUser? = null,
    var userName: String? = null,
    var imageUri: String? = null
)

object UserSingleton {
    var userModel = UserModel()
}

