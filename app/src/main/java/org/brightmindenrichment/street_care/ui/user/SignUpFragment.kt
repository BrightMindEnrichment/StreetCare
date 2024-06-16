package org.brightmindenrichment.street_care.ui.user

import android.content.ContentValues
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentSignUpBinding
import java.util.*


class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private var userName: String = ""
    private var email: String = ""
    private var password: String = ""
    private var company: String = ""
    lateinit var googleobserver : GoogleSigninLifeCycleObserver
    lateinit var fbObserver : FacebookSignInLifeCycleObserver
    lateinit var twitterObserver : TwitterSignInLifeCycleObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val signInListener = object : SignInListener {
            override fun onSignInSuccess(){
                findNavController().popBackStack()
                Log.d(ContentValues.TAG, "Firebase user signin success")
            }

            override fun onSignInError() {
                Log.d(ContentValues.TAG, "Firebase user signin fail")
            }
        }
        val activityResultRegistryOwner = requireActivity() as? ActivityResultRegistryOwner

        googleobserver = GoogleSigninLifeCycleObserver(requireContext(), signInListener)
        fbObserver = FacebookSignInLifeCycleObserver(activityResultRegistryOwner!!, signInListener,lifecycle)
        twitterObserver = TwitterSignInLifeCycleObserver(requireActivity(), signInListener)

        lifecycle.addObserver(googleobserver)
        lifecycle.addObserver(fbObserver)
        lifecycle.addObserver(twitterObserver)
        arguments?.let {
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return _binding!!.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSignUpSignUp.setOnClickListener {
            userName = binding.editTextSignUpUserName.text.toString()
            email = binding.editTextSignUpEmail.text.toString()
            password = binding.editTextSignUpPassword.text.toString()
            company = binding.editTextSignUpCompany.text.toString()
            if (TextUtils.isEmpty(userName)) {
                binding.editTextSignUpUserName.setError(getString(R.string.mandatory))
            } else if (TextUtils.isEmpty(email)  ) {
                binding.editTextSignUpEmail.setError(getString(R.string.mandatory))
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.editTextSignUpEmail.setError(getString(R.string.enter_valid_email_address))
            } else if (TextUtils.isEmpty(password)) {
                binding.editTextSignUpPassword.setError(getString(R.string.mandatory))
            } else {
                Firebase.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val currentUser = Firebase.auth.currentUser
                            val userData = hashMapOf<String, Any>(
                                "dateCreated" to Date(),
                                "deviceType" to "Android",
                                "email" to email,
                                "isValid" to true,
                                "organization" to company,
                                "username" to userName,
                                "uid" to (currentUser?.uid ?: "??")
                            )
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(currentUser?.uid ?: "??").set(userData).addOnCompleteListener { task ->
                                Toast.makeText(activity,
                                    getString(R.string.successfully_register), Toast.LENGTH_SHORT).show();
                                findNavController().navigateUp()
                                binding.editTextSignUpCompany.text?.clear()
                                binding.editTextSignUpEmail.text?.clear()
                                binding.editTextSignUpPassword.text?.clear()
                                binding.editTextSignUpUserName.text?.clear()
                            }
                        } else {
                            Toast.makeText(activity,getString(R.string.error_failed_to_create_user),Toast.LENGTH_SHORT ).show();
                        }
                    }
            }
        }

        /*
        *Commented out for final bug fixed version 2. Will be uncommented in version 3  when 3rd party authentication is enabled.
        *
        *
        binding.layoutsiginmethod.cardFacebook.setOnClickListener {
            fbObserver.requestFacebookSignin()
        }
        binding.layoutsiginmethod.cardTwitter.setOnClickListener {
            twitterObserver.requestTwitterSignIn()
        }
        */

        binding.layoutsiginmethod.cardGoogle.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                googleobserver.requestGoogleSignin()
            }
        }

        
    }
    override fun onDestroy() {
        super.onDestroy()

        // Remove the observer when the Fragment is destroyed
        lifecycle.removeObserver(googleobserver)
        lifecycle.removeObserver(fbObserver)
        lifecycle.removeObserver(twitterObserver)
    }
}