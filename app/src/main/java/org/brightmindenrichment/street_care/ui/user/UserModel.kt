package org.brightmindenrichment.street_care.ui.user

import android.net.Uri
import com.google.firebase.auth.FirebaseUser

data class UserModel(
    var currentUser: FirebaseUser? = null,
) {
    var userName: String? = currentUser?.displayName
    var imageUri: Uri? = currentUser?.photoUrl
}

object UserSingleton {
    var userModel = UserModel()
//    val auth: FirebaseAuth by lazy { Firebase.auth }
}

