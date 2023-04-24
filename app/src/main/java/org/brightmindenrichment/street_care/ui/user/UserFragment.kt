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
        currentUser = Firebase.auth.currentUser
        if(currentUser==null) {
            buttonLogin = view.findViewById<Button>(R.id.user_button_login)
            buttonSignUp = view.findViewById<Button>(R.id.user_button_sign_up)
            textViewWelcome = view.findViewById<TextView>(R.id.textViewWelcome)


            buttonLogin.setOnClickListener {
                buttonLoginOnClick()
            }
            buttonSignUp.setOnClickListener {
                buttonSignUpOnClick()
            }

        }
        else{
            findNavController().navigate(R.id.action_nav_user_to_nav_profile)
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
    }

} // end class