package org.brightmindenrichment.street_care.ui.user

import android.app.AlertDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentProfileBinding
import org.brightmindenrichment.street_care.ui.visit.VisitDataAdapter
import org.brightmindenrichment.street_care.util.Extensions



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var currentUser: FirebaseUser? = null
    private val storage = Firebase.storage
    private val storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = Firebase.auth.currentUser!!
        getUserData()
        binding.txteditprofile.setOnClickListener{
            findNavController().navigate(R.id.action_nav_profile_to_nav_editprofile)
        }
        binding.btnsignout.setOnClickListener{
            buttonSignOutOnClick()
        }
        binding.textDeleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(getString(R.string.delete_acc_title))
            builder.setMessage(getString(R.string.delete_acc_msg))
                .setCancelable(false)
                .setPositiveButton(getString(
                    R.string.confirm), DialogInterface.OnClickListener { dialog, _ ->
                    deleteAccount()
                }
                )
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.cancel()
                }
            val alert = builder.create()
            alert.show()
        }

    }

    private fun buttonSignOutOnClick() {
        if (currentUser != null) {
            googleSignOut()
            Firebase.auth.signOut()
            currentUser = null
            Log.d(ContentValues.TAG, "Firebase user sign out")

            findNavController().popBackStack()
        }
    }

    private fun deleteAccount(){

        currentUser?.let { user ->
            val providerData = currentUser!!.providerData
            var isGoogle = false;
            var isFacebook = false;
            for (userInfo in providerData) {
                val providerId = userInfo.providerId
                Log.d("userInfo.providerId", "userInfo.providerId."+userInfo.providerId)
                if(providerId=="google.com"){
                    isGoogle=true
                    // Get the GoogleSignInAccount from the user
                    val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
                    // Create GoogleAuthProvider with the Google ID token and access token
                    val credential = GoogleAuthProvider.getCredential(googleSignInAccount?.idToken, null)
                    // Reauthenticate the user
                    user.reauthenticate(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("Reauthentication", "User reauthenticated successfully.")
                                googleSignOut()
                                deleteFirebaseUserAccount()
                            } else {
                                // Reauthentication failed
                                buttonSignOutOnClick()
                                Toast.makeText(
                                    context,
                                    "Please login again inorder to delete your account",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e(
                                    "Reauthentication",
                                    "Failed to reauthenticate user.",
                                    task.exception
                                )
                            }
                        }
                }
                if(providerId=="facebook.com"){
                    isFacebook = true
                    val accessToken = AccessToken.getCurrentAccessToken()
                    val credential = FacebookAuthProvider.getCredential(accessToken.toString())
                    // Reauthenticate the user
                    currentUser!!.reauthenticate(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("Reauthentication", "User reauthenticated successfully.")
                                deleteFirebaseUserAccount()
                            } else {
                                // Reauthentication failed
                                buttonSignOutOnClick()
                                Toast.makeText(context,"Please login again inorder to delete your account",Toast.LENGTH_LONG).show()
                                Log.e("Reauthentication", "Failed to reauthenticate user.", task.exception)
                            }
                        }
                }


            }
            if(!isGoogle && !isFacebook){
                deleteFirebaseUserAccount()
            }


        }


    }
    private fun googleSignOut(){
        val providerData = currentUser!!.providerData
        for (userInfo in providerData) {
            val providerId = userInfo.providerId
            if(providerId=="google.com"){
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(requireActivity().getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

                googleSignInClient.signOut().addOnCompleteListener(requireActivity(), OnCompleteListener<Void?> {
                    // ...
                    Log.d(ContentValues.TAG, "Google user sign out")

                    })
            }
        }
    }

    private fun deleteFirebaseUserAccount(){
        currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "User account deleted.")
                findNavController().popBackStack()
            } else {
                Log.w(TAG, "Problem deleting User account", task.exception)
                if (task.exception is FirebaseAuthRecentLoginRequiredException) {
                    buttonSignOutOnClick()
                    Toast.makeText(
                        context,
                        "Please login again inorder to delete your account",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("Reauthentication", "Failed to reauthenticate user.", task.exception)
                }

            }
        }
    }
    private fun getUserData(){
        Log.d(TAG, "getUserData")
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(currentUser?.uid ?: "??")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.data
                    if (!user.isNullOrEmpty()) {
                        binding.txtprofileusername.text = user["username"]?.toString() ?:currentUser?.displayName.toString()
                        Log.d(TAG, "currentUser "+currentUser?.displayName.toString())
                    }
                    else{
                        binding.txtprofileusername.text = currentUser?.displayName ?:"user name"
                    }
                } else {
                    Log.d(TAG, "No such document user")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        val fileName = "profile.jpg"
        val imageRef = storageRef.child("users").child(currentUser?.uid ?: "??").child(fileName)
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            if (uri != null) {
                Picasso.get().load(uri).into(binding.profileimageview)
                Log.d(TAG, "Get image:success")
            }
        }.addOnFailureListener {
            // Handle any errors
            Log.d(TAG, "No such document")
        }
        val visitDataAdapter = VisitDataAdapter()
        visitDataAdapter.refresh {
            var totalItemsDonated = visitDataAdapter.getTotalItemsDonated
            var totalOutreaches = visitDataAdapter.size
            var totalPeopleHelped = visitDataAdapter.getTotalPeopleCount
            binding.cardImpactLayout.itemsDonated.text = totalItemsDonated.toString()
            binding.cardImpactLayout.totalOutreaches.text = totalOutreaches.toString()
            binding.cardImpactLayout.textHelped.text = totalPeopleHelped.toString()
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}