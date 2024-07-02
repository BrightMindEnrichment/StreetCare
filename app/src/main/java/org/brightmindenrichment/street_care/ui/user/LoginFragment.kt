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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var loginObserver: LoginLifeCycleObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val signInListener = object : SignInListener {
            override fun onSignInSuccess() {
                findNavController().popBackStack()
                Log.d(ContentValues.TAG, "Firebase user signIn success")
            }

            override fun onSignInError() {
                Log.d(ContentValues.TAG, "Firebase user signIn fail")
            }
        }

        loginObserver = LoginLifeCycleObserver(requireContext(), signInListener)
        lifecycle.addObserver(loginObserver)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                binding.editTextTextEmailAddress.setError(getString(R.string.mandatory))
                binding.editTextTextPassword.setError(getString(R.string.mandatory))
            }   else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.editTextTextEmailAddress.setError(getString(R.string.enter_valid_email_address))
            }else {
                auth = Firebase.auth
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(activity,
                            getString(R.string.successfully_login), Toast.LENGTH_SHORT).show();
                        binding.editTextTextEmailAddress.text?.clear()
                        binding.editTextTextPassword.text?.clear()
                        findNavController().navigate(R.id.nav_user)
                    } else {
                        Toast.makeText(
                            activity,
                            getString(R.string.error_login_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.txtforget.setOnClickListener {
            findNavController().navigate(R.id.action_nav_login_to_nav_forgetPass)
        }

        /*
        *Commented out for final bug fixed version 2. Will be uncommented in version 3  when 3rd party authentication is enabled.
        *
            binding.layoutsiginmethod.cardFacebook.setOnClickListener {
                fbObserver.requestFacebookSignin()
            }
            binding.layoutsiginmethod.cardTwitter.setOnClickListener {
                twitterObserver.requestTwitterSignIn()
            }
        *
        */
        binding.layoutsiginmethod.cardGoogle.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                loginObserver.fetchGoogleSignInCredentials()
            }
        }

        binding.layoutsiginmethod.cardTwitter.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                loginObserver.launchTwitterXSignIn()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the observer when the Fragment is destroyed
        lifecycle.removeObserver(loginObserver)
    }
}