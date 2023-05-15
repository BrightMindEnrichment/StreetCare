package org.brightmindenrichment.street_care.ui.user

import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import java.lang.ref.WeakReference


class SigninLifeCycleObserver(private val registry: ActivityResultRegistry, private val mactivity: Activity, private val googleSignInListener:GoogleSignInListener)
    : DefaultLifecycleObserver {
    private lateinit var getContent : ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth


    
    override fun onCreate(owner: LifecycleOwner) {

            getContent = registry.register("Key", owner, ActivityResultContracts.StartActivityForResult()) { result ->
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
            .requestIdToken(mactivity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(mactivity, gso)
        val signInIntent = googleSignInClient.signInIntent
        getContent.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(mactivity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    Log.d(TAG, "signInWithCredential:success")
                    googleSignInListener.onSignInSuccess()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "Google firebase login fail", task.exception)
                    googleSignInListener.onSignInError()

                }
            }
    }




}

