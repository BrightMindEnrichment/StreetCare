package org.brightmindenrichment.street_care.ui.user

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    lateinit var edtEmail: EditText
    lateinit var edtPass: EditText
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
        return inflater.inflate(R.layout.fragment_login, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonLogin = view.findViewById<Button>(R.id.loginButton)
        buttonLogin.setOnClickListener {
            edtEmail = view.findViewById<EditText>(R.id.editTextTextEmailAddress)
            edtPass = view.findViewById<EditText>(R.id.editTextTextPassword2)
            var email = edtEmail.text.toString()
            var password = edtPass.text.toString()
            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password))
            {
                edtEmail.setError("Mandatory")
                edtPass.setError("Mandatory")
            }   else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Enter Valid Email Address")
            }else {
                auth = Firebase.auth
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(activity, "Successfully Login", Toast.LENGTH_SHORT).show();
                        edtEmail.text.clear()
                        edtPass.text.clear()
                        findNavController().navigate(R.id.nav_user)
                    } else {
                        Toast.makeText(activity, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        val txtForgot = view.findViewById<TextView>(R.id.txtforget)
            txtForgot.setOnClickListener {
                findNavController().navigate(R.id.action_nav_login_to_nav_forgetPass)


            }
        }


}