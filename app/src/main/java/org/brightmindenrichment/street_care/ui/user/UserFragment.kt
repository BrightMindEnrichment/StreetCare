package org.brightmindenrichment.street_care.ui.user

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.ui.community.Event
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import java.util.*


class UserFragment : Fragment() {
private var currentUser: FirebaseUser? = null
    private lateinit var buttonLogin: Button
    private lateinit var buttonSignUp: Button
    private lateinit var buttonRemoveAccount: Button
    private lateinit var buttonSignOut: Button
    private lateinit var buttonEvent: Button
    private lateinit var textViewWelcome: TextView

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
        return inflater.inflate(R.layout.fragment_user, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonLogin = view.findViewById<Button>(R.id.user_button_login)
        buttonSignUp = view.findViewById<Button>(R.id.user_button_sign_up)
        buttonRemoveAccount = view.findViewById<Button>(R.id.user_button_remove_account)
        buttonSignOut = view.findViewById<Button>(R.id.user_button_sign_out)
        buttonEvent = view.findViewById<Button>(R.id.btnEvents)
        textViewWelcome = view.findViewById<TextView>(R.id.textViewWelcome)
        buttonEvent.visibility = View.GONE
        // grab the logged in user, if there is one
        currentUser = Firebase.auth.currentUser
        //if (currentUser == null)
          //  findNavController().navigate(R.id.action_nav_user_to_nav_login)
        //else
        // setup click listeners
            buttonLogin.setOnClickListener {
                buttonLoginOnClick()
            }
        buttonSignUp.setOnClickListener {
            buttonSignUpOnClick()
        }
        buttonSignOut.setOnClickListener {
            buttonSignOutOnClick()
        }
        buttonRemoveAccount.setOnClickListener {
            buttonRemoveAccountOnClick()
        }
        buttonEvent.setOnClickListener {
            buttonEventOnClick()
        }
        updateUI()
    }

    private fun buttonEventOnClick() {
        findNavController().navigate(R.id.pendingEvents)

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (!hidden) {
            updateUI()
        }
    }


    private fun buttonRemoveAccountOnClick() {
        currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                currentUser = null
                updateUI()
            }
        }
    }


    private fun buttonLoginOnClick() {
        findNavController().navigate(R.id.action_nav_user_to_nav_login)
    }
    private fun buttonSignUpOnClick() {
        findNavController().navigate(R.id.action_nav_user_to_nav_sign_up)
    }
    private fun buttonSignOutOnClick() {
        if (currentUser != null) {
            Firebase.auth.signOut()
            currentUser = null
        }

        updateUI()
    }

    fun updateUI() {

        if (currentUser != null) {
            readUserdata()
           buttonLogin.visibility = View.GONE
           buttonSignUp.visibility = View.GONE
            buttonRemoveAccount.visibility = View.VISIBLE
            buttonSignOut.visibility = View.VISIBLE
            Log.d("BME::", currentUser.toString())
     } else {
//        findNavController().navigate(R.id.action_nav_user_to_nav_login)
            textViewWelcome.text = getString(R.string.welcome)
            buttonEvent.visibility = View.GONE
            buttonLogin.visibility = View.VISIBLE
            buttonSignUp.visibility = View.VISIBLE
            buttonEvent.visibility = View.GONE
            buttonRemoveAccount.visibility = View.GONE
            buttonSignOut.visibility = View.GONE
        }
    }

    private fun readUserdata() {
        val user = Firebase.auth.currentUser ?: return
        Log.d("BME", user.uid)
        val db = Firebase.firestore
        db.collection("users").whereEqualTo("uid", user.uid).get().addOnSuccessListener { result ->
            for (document in result) {
                var user = Users()
                user.email = document.get("email").toString()
                user.organization = document.get("organization").toString()
                user.role = document.get("role").toString()
                user.username = document.get("username").toString()
                textViewWelcome.text = "Welcome " + user.username.toString()+","
                if (user.role == "admin") {
                    buttonEvent.visibility = View.VISIBLE
                } else
                    buttonEvent.visibility = View.GONE

            }
        }
    }
} // end class