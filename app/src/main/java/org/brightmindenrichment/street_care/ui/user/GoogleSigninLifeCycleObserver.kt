package org.brightmindenrichment.street_care.ui.user

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.brightmindenrichment.street_care.R


class GoogleSigninLifeCycleObserver(
    private val context: Context,
    private val signInListener: SignInListener
) : DefaultLifecycleObserver {
    private lateinit var auth: FirebaseAuth
    private val credentialManager = CredentialManager.create(context)

    override fun onCreate(owner: LifecycleOwner) {
        Log.d(TAG, "GoogleSigninLifeCycleObserver created")
        auth = Firebase.auth
    }

    suspend fun requestGoogleSignin() {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
//            .setNonce()
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope {
            launch(Dispatchers.IO) {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = context,
                    )
                    handleSignIn(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting credential Google", e)
                }
            }
        }

    }

    private fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {
            // GoogleIdToken credential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information

                    Log.d(TAG, "signInWithCredential:success")
                    val currentUser = Firebase.auth.currentUser
                    val isNew = task.result.additionalUserInfo!!.isNewUser
                    if (isNew) {
                        Log.d(
                            TAG,
                            "uploading user data to firebase:success " + currentUser?.email.toString()
                        )
                        val userData = Users(
                            currentUser?.displayName.toString(),
                            currentUser?.uid ?: "??",
                            currentUser?.email.toString()
                        )
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(currentUser?.uid ?: "??").set(userData)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "uploading user data to firebase:success")
                                    signInListener.onSignInSuccess()
                                } else {
                                    Log.e(
                                        TAG,
                                        "Error uploading user data to firebase",
                                        task.exception
                                    )
                                    signInListener.onSignInError()
                                }
                            }
                    } else {
                        signInListener.onSignInSuccess()
                    }


                } else {
                    // If sign in fails, display a message to the user.
                    Log.e(TAG, "Google firebase login fail", task.exception)
                    signInListener.onSignInError()

                }
            }
    }
}

