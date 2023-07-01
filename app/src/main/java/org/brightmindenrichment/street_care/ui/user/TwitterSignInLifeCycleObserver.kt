package org.brightmindenrichment.street_care.ui.user

import android.app.Activity
import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class TwitterSignInLifeCycleObserver(private val activity: Activity,private val signInListener:SignInListener) : DefaultLifecycleObserver {
    private lateinit var provider: OAuthProvider.Builder
    private lateinit var auth: FirebaseAuth
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        provider = OAuthProvider.newBuilder("twitter.com")
        auth = Firebase.auth
    }

    fun requestTwitterSignIn(){
        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) {
            Log.d(ContentValues.TAG, " pendingResultTask TwittersignInWithCredential:start")
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "pendingResultTask TwittersignInWithCredential:success")
                    val currentUser = Firebase.auth.currentUser
                    val isNew = pendingResultTask.result.additionalUserInfo!!.isNewUser
                    if(isNew){
                        val userData = Users(currentUser?.displayName.toString(),currentUser?.uid ?: "??",currentUser?.email.toString())
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(currentUser?.uid ?: "??").set(userData).addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                Log.d(ContentValues.TAG, "uploading user data to firebase:success")
                                signInListener.onSignInSuccess()
                            }
                            else{
                                Log.w(ContentValues.TAG, "Error uploading user data to firebase", task.exception)
                                signInListener.onSignInError()
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.d(ContentValues.TAG, "pendingResultTask TwittersignInWithCredential:fail")
                    signInListener.onSignInError()
                }
        } else {
            // There's no pending result so you need to start the sign-in flow.
            Log.d(ContentValues.TAG, "TwittersignInWithCredential:start")
            auth
                .startActivityForSignInWithProvider(activity, provider.build())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        val currentUser = Firebase.auth.currentUser
                        val isNew = task.result.additionalUserInfo!!.isNewUser
                        if(isNew){
                            val userData = Users(currentUser?.displayName.toString(),currentUser?.uid ?: "??",currentUser?.email ?: "Unknown")
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(currentUser?.uid ?: "??").set(userData).addOnCompleteListener { task ->
                                if (task.isSuccessful){
                                    Log.d(ContentValues.TAG, "uploading user data to firebase:success")
                                    signInListener.onSignInSuccess()
                                }
                                else{
                                    Log.w(ContentValues.TAG, "Error uploading user data to firebase", task.exception)
                                    signInListener.onSignInError()
                                }
                            }
                        }
                        else{
                            signInListener.onSignInSuccess()
                        }
                    }
                    else {
                        // If sign in fails, display a message to the user.
                        Log.w(ContentValues.TAG, "Twitter firebase login fail", task.exception)
                        signInListener.onSignInError()

                    }
                }

        }
    }

}