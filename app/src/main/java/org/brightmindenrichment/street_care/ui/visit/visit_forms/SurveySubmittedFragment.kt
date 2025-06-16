package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentSurvaySubmittedBinding
import android.widget.Toast
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.content.ContentValues.TAG
import com.google.android.material.bottomnavigation.BottomNavigationView


class SurveySubmittedFragment : Fragment() {
   private var _binding : FragmentSurvaySubmittedBinding? = null
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSurvaySubmittedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAnotherVisit.setOnClickListener{
            findNavController().navigate(R.id.action_surveySubmittedFragment_to_visitFormFragment1)
        }
        binding.btnShare.setOnClickListener{
//            findNavController().navigate(R.id.surveySubmittedFragment)
            showSharePopup()
        }
        binding.btnReturnHome.setOnClickListener{
            findNavController().navigate(R.id.action_surveySubmittedFragment_to_nav_home)
        }
        // Handle back button press
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_surveySubmittedFragment_to_nav_visit)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    // Function to show the share confirmation popup
    private fun showSharePopup() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.share_popup_title))
            .setMessage(getString(R.string.share_popup_message))
            .setPositiveButton(getString(R.string.share_popup_confirm)) { dialog, _ ->
                // Logic for confirming the share action
                sharedVisitViewModel.visitLog.share = true
                //update the isPublic to true
                val docId = sharedVisitViewModel.visitLog.documentId
                if (docId != null) {
                    updateVisitLogField(docId, "isPublic", true)
                }
                saveVisitLog()
                // Reset the visit log for future use
                sharedVisitViewModel.resetVisitLogPage()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.share_popup_cancel)) { dialog, _ ->
                // Simply dismiss the popup
                dialog.dismiss()
            }
            .create()
            .show()
    }

    fun updateVisitLogField(documentId: String, fieldName: String, newValue: Any) {
        val user = Firebase.auth.currentUser ?: return
        Log.d("BME", user.uid)
        val db = Firebase.firestore
        val docRef = db.collection("VisitLogBook_New").document(documentId)
        docRef.update(fieldName, newValue)
            .addOnSuccessListener {
                Log.d("Firestore", "Field updated successfully")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating field", e)
            }
    }


    // Function to save the visit log details to Firebase "PersonalVisitLog"
    private fun saveVisitLog() {
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore

        // Ensure a user is logged in
        if (user != null) {
            // Retrieve the current visitLog from ViewModel
            val visitLog = sharedVisitViewModel.visitLog

            val usersDocRef = db.collection("users").document(user.uid)

            usersDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val userType = document.getString("Type") ?: ""

                    // Determine status based on user type
                    val status =
                        if (userType == "Chapter Leader" || userType == "Street Care Hub Leader") "approved" else "pending"

                    //Prepare the data to save
                    val visit = visitLog.whereVisit ?: ""
                    val parts = visit.split(",").map { it.trim() }

                    val street = parts.getOrNull(0) ?: ""
                    val city = parts.getOrNull(1) ?: ""
                    val state = parts.getOrNull(2) ?: ""
                    val zipcode = parts.getOrNull(3) ?: ""
                    val visitData = hashMapOf(
                        "dateTime" to visitLog.date,
                        "state" to state,
                        "city" to city,
                        "stateAbbv" to state,
                        "street" to street,
                        "zipcode" to zipcode,
                        "numberPeopleHelped" to visitLog.peopleCount,
                        "whatGiven" to visitLog.whattogive,
                        "public" to "true",
                        "status" to status,
                        "itemQty" to visitLog.number_of_items,
                        "rating" to visitLog.experience,
                        "Description" to visitLog.peopleHelpedDescription,
                        "flaggedByUser" to visitLog.flaggedByUser,
                        "isFlagged" to visitLog.isFlagged
                    )

                    // Save data to the "visitLogWebProd" collection
                    db.collection("visitLogWebProd")
                        .add(visitData)
                        .addOnSuccessListener {
                            Log.d(TAG, "Visit Log successfully shared to visitLogWebProd")
                            Toast.makeText(
                                context,
                                getString(R.string.info_shared_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error sharing Visit Log: ", e)
                            Toast.makeText(
                                context,
                                getString(R.string.info_share_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }.addOnFailureListener { e ->
            Log.w(
                "syncWebApp",
                "Error getting user details",
                e
            )
        }
        } else {
            Log.e(TAG, "User is not logged in")
            Toast.makeText(context, "Please log in to share info", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    override fun onDestroyView() {
        super.onDestroyView()


            requireActivity()
                .findViewById<BottomNavigationView>(R.id.bottomNav)
                .selectedItemId = R.id.nav_home

    }
}// end of class
