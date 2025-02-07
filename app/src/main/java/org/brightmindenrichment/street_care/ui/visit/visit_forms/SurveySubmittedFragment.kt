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
                shareVisitLog()
                // Reset the visit log for future use
                sharedVisitViewModel.resetVisitLogPage(forceReset = true)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.share_popup_cancel)) { dialog, _ ->
                // Simply dismiss the popup
                dialog.dismiss()
            }
            .create()
            .show()
    }


    // Function to save the visit log details to Firebase "PersonalVisitLog"
    private fun shareVisitLog() {
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore

        // Ensure a user is logged in
        if (user != null) {
            // Retrieve the current visitLog from ViewModel
            val visitLog = sharedVisitViewModel.visitLog

             //Prepare the data to save
            val visitData = hashMapOf(
                "HelpTime" to visitLog.helpTime,
                "dateTime" to visitLog.date,
                "whenVisitTime" to visitLog.whenVisitTime,
                "NumberOfPeopleHelped" to visitLog.peopleCount,
                "PeopleHelpedDescription" to visitLog.names,
                "rating" to visitLog.experience,
                "share" to visitLog.share,
                "uid" to user.uid,
                "number_of_items_donated" to visitLog.number_of_items,
                "WhatGiven" to visitLog.whattogive,
                "Location" to visitLog.locationmap,
                "Type" to visitLog.typeofdevice,
                "food_drink" to visitLog.food_drink,
                "clothes" to visitLog.clothes,
                "hygine" to visitLog.hygine,
                "wellness" to visitLog.wellness,
                "lawyerLegal" to visitLog.lawyerLegal,
                "medicalhelp" to visitLog.medicalhelp,
                "social" to visitLog.socialWorker,
                "other" to visitLog.other,
                "status" to "pending"
//                //"public" to true,
            )

            // Save data to the "visitLogWebProd" collection
            db.collection("visitLogWebProd")
                .add(visitData)
                .addOnSuccessListener {
                    Log.d(TAG, "Visit Log successfully shared to visitLogWebProd")
                    Toast.makeText(context, getString(R.string.info_shared_success), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error sharing Visit Log: ", e)
                    Toast.makeText(context, getString(R.string.info_share_failed), Toast.LENGTH_SHORT).show()
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
}// end of class
