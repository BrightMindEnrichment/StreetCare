package org.brightmindenrichment.street_care.ui.user

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import org.brightmindenrichment.street_care.R


class GoogleSigninLifeCycleObserver(private val registry: ActivityResultRegistry, private val context: Context, private val signInListener:SignInListener)
    : DefaultLifecycleObserver {
    private lateinit var getContent : ActivityResultLauncher<IntentSenderRequest>
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signUpRequest: BeginSignInRequest


    override fun onCreate(owner: LifecycleOwner) {

        initGoogleSignInSignUp()

        getContent = registry.register("intentSender", owner, ActivityResultContracts.StartIntentSenderForResult()) { result ->
                // Handle the returned Uri
                val resultCode = result.resultCode
                var data = result.data
                if (resultCode == Activity.RESULT_OK){
                    try {
                        val signInClient = Identity.getSignInClient(context)
                        val credentials = signInClient.getSignInCredentialFromIntent(data)
                        val googleIdToken = credentials.googleIdToken
                        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
                        // Google Sign In was successful, authenticate with Firebase
                        if (googleIdToken != null) {
                            firebaseAuthWithGoogle(googleIdToken )
                        } else {
                            Log.w(TAG, "Google sign in failed with null googleIdToken")
                        }
                    } catch (e: ApiException) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(TAG, "Google sign in failed", e)
                    }
                }
                else{
                    Log.d(TAG, "Google sigin:Fail")
                }

            }

        auth = Firebase.auth
    }

    private fun handleSuccessGoogleSignIn(signInResult: BeginSignInResult) {
        val intent = IntentSenderRequest.Builder(signInResult.pendingIntent.intentSender).build()
        getContent.launch(intent)
    }

    private fun initGoogleSignInSignUp() {
        oneTapClient = Identity.getSignInClient(context)


        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
    }

    suspend fun requestGoogleSignin() {
        try {
            val signInResult = oneTapClient.beginSignIn(signInRequest).await()
            handleSuccessGoogleSignIn(signInResult)
        } catch (e: Exception) {
            try {
                val signUpResult = oneTapClient.beginSignIn(signUpRequest).await()
            } catch (e: Exception) {
                Log.e(TAG, "Google sign in failed", e)
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
                    if(isNew){
                        Log.d(TAG, "uploading user data to firebase:success "+currentUser?.email.toString())
                        val userData = Users(currentUser?.displayName.toString(),currentUser?.uid ?: "??",currentUser?.email.toString())
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(currentUser?.uid ?: "??").set(userData).addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                Log.d(TAG, "uploading user data to firebase:success")
                                signInListener.onSignInSuccess()
                            }
                            else{
                                Log.w(TAG, "Error uploading user data to firebase", task.exception)
                                signInListener.onSignInError()
                            }
                        }
                    }
                    else{
                        signInListener.onSignInSuccess()
                    }


                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "Google firebase login fail", task.exception)
                    signInListener.onSignInError()

                }
            }
    }




}

