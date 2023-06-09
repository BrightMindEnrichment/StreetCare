package org.brightmindenrichment.street_care.ui.user

import android.content.ContentValues
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentLoginBinding


class LoginFragment : Fragment(){
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
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

        googleobserver = GoogleSigninLifeCycleObserver(requireActivity().activityResultRegistry, requireContext(), signInListener)
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
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return _binding!!.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonLogin = view.findViewById<Button>(R.id.loginButton)
        buttonLogin.setOnClickListener {
            var email = binding.editTextTextEmailAddress.text.toString()
            var password = binding.editTextTextPassword.text.toString()
            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password))
            {
                binding.editTextTextEmailAddress.setError("Mandatory")
                binding.editTextTextPassword.setError("Mandatory")
            }   else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.editTextTextEmailAddress.setError("Enter Valid Email Address")
            }else {
                auth = Firebase.auth
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(activity, "Successfully Login", Toast.LENGTH_SHORT).show();
                        binding.editTextTextEmailAddress.text?.clear()
                        binding.editTextTextPassword.text?.clear()
                        findNavController().navigate(R.id.nav_user)
                    } else {
                        Toast.makeText(activity, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        binding.txtforget.setOnClickListener {
                findNavController().navigate(R.id.action_nav_login_to_nav_forgetPass)
            }

        binding.layoutsiginmethod.cardGoogle.setOnClickListener {
            googleobserver.requestGoogleSignin()

        }
        binding.layoutsiginmethod.cardFacebook.setOnClickListener {
            fbObserver.requestFacebookSignin()
        }
        binding.layoutsiginmethod.cardTwitter.setOnClickListener {
            twitterObserver.requestTwitterSignIn()
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