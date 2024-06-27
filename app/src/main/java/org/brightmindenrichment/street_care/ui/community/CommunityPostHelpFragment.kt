package org.brightmindenrichment.street_care.ui.community

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentCommunityPostHelpBinding
import org.brightmindenrichment.street_care.util.Extensions
import java.util.Date

private const val FLAG = "Help"

class CommunityPostHelpFragment : Fragment() {
    private lateinit var inputTitle: EditText
    private lateinit var contactInfo: EditText
    private lateinit var inputDescription: EditText
    private lateinit var inputLocation: EditText
    private var _binding: FragmentCommunityPostHelpBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCommunityPostHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnSubmit = binding.btnPost
        val btnDiscard = binding.btnDiscard
        inputTitle = binding.titleText
        inputDescription = binding.requestDetailText
        inputLocation = binding.locationText
        contactInfo = binding.contactText


        btnSubmit.setOnClickListener {
            if (Firebase.auth.currentUser == null) {
                context?.let { it1 ->
                    Extensions.showDialog(
                        it1,
                        "Alert",
                        "Please Login before submit the Event",
                        "Ok","Cancel"
                    )
                }
            } else {
                var title = inputTitle.text.toString()
                var contact = contactInfo.text.toString()
                var desc = inputDescription.text.toString()
                var location = inputLocation.text.toString()
                var anonymous = binding.anonymousCheck.isChecked
                if (TextUtils.isEmpty(title)) {
                    inputTitle.error = "Required"
                } else if (TextUtils.isEmpty(desc)) {
                    inputDescription.error = "Required"
                } else if (TextUtils.isEmpty(location)) {
                    inputLocation.error = "Required"
                } else {
                    addHelp(title, desc, contact, location, anonymous)
                }
            }
        }

        btnDiscard.setOnClickListener {
            clearFields()
        }
    }

    private fun addHelp(title: String, description: String, contact: String, location: String, anonymous: Boolean) {
        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return
        // create a map of event data so we can add to firebase
        val helpData = hashMapOf(
            "title" to title,
            "description" to description,
            "date" to Date(),
            "contact" to contact,
            "location" to location,
            "uid" to user.uid,
            "isVerified" to false,
            "anonymous" to anonymous
        )
        // save to firebase
        val db = Firebase.firestore
        db.collection("communityHelp").add(helpData).addOnSuccessListener { documentReference ->
            Log.d("BME", "Saved with id ${documentReference.id}")
            Log.i("db.collection", "communityHelp")
//            Extensions.showDialog(
//                requireContext(),
//                "Alert",
//                "Help registered for verification",
//                "Ok"
//            )
            clearFields()
            Toast.makeText(context, "Successfully Registered", Toast.LENGTH_LONG).show()
            navBack()
        }.addOnFailureListener { exception ->
            Log.w("BMR", "Error in add this Help ${exception.toString()}")
            Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearFields() {
        contactInfo.text.clear()
        inputLocation.text.clear()
        inputDescription.text.clear()
        inputTitle.text.clear()
    }

    private fun navBack() {
        val bundle = Bundle()
        bundle.putString("name", FLAG)
        findNavController().navigate(R.id.communityHelpFragment, bundle)
    }


}