package org.brightmindenrichment.street_care.ui.user

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import java.util.*


class SignUpFragment : Fragment() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextPassword2: EditText
    private lateinit var editTextCompany: EditText
    private lateinit var buttonSignUp: Button
    private var userName: String = ""
    private var email: String = ""
    private var password: String = ""
    private var password2: String = ""
    private var company: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editTextUsername = view.findViewById<EditText>(R.id.editTextSignUpUserName)
        editTextEmail = view.findViewById<EditText>(R.id.editTextSignUpEmail)
        editTextPassword = view.findViewById<EditText>(R.id.editTextSignUpPassword)
        editTextCompany = view.findViewById<EditText>(R.id.editTextSignUpCompany)
        buttonSignUp = view.findViewById(R.id.buttonSignUpSignUp)
        buttonSignUp.setOnClickListener {
            userName = editTextUsername.text.toString()
            email = editTextEmail.text.toString()
            password = editTextPassword.text.toString()
            company = editTextCompany.text.toString()
            if (TextUtils.isEmpty(userName)) {
                editTextUsername.setError("Mandatory")
            } else if (TextUtils.isEmpty(email)  ) {
                editTextEmail.setError("Mandatory")
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editTextEmail.setError("Enter Valid Email Address")
            } else if (TextUtils.isEmpty(password)) {
                editTextPassword.setError("Mandatory")
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
                            db.collection("users").add(userData).addOnCompleteListener { task ->
                                Toast.makeText(activity, "Successfully Register!!", Toast.LENGTH_SHORT).show();
                                findNavController().navigateUp()
                                editTextCompany.text.clear()
                                editTextEmail.text.clear()
                                editTextPassword.text.clear()
                                editTextUsername.text.clear()
                            }
                        } else {
                            Toast.makeText(activity,getString(R.string.error_failed_to_create_user),Toast.LENGTH_SHORT ).show();
                        }
                    }
            }
        }
    }
}