package org.brightmindenrichment.street_care.ui.user

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R


class GoogleSigninLifeCycleObserver(private val registry: ActivityResultRegistry, private val context: Context, private val signInListener:SignInListener)
    : DefaultLifecycleObserver {
    private lateinit var getContent : ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth

    
    override fun onCreate(owner: LifecycleOwner) {

            getContent = registry.register("GoogleSigInIntent", owner, ActivityResultContracts.StartActivityForResult()) { result ->
                // Handle the returned Uri
                val resultCode = result.resultCode
                var data = result.data
                if (resultCode == Activity.RESULT_OK){
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        val account = task.getResult(ApiException::class.java)!!
                        firebaseAuthWithGoogle(account.idToken!!)
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

    fun requestGoogleSignin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        val signInIntent = googleSignInClient.signInIntent
        getContent.launch(signInIntent)
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

