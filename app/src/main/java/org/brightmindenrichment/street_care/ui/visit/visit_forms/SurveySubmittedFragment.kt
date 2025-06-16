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
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView


class SurveySubmittedFragment : Fragment() {
   private var _binding : FragmentSurvaySubmittedBinding? = null
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()
    private val binding get() = _binding!!
    private var clicked = false
    private var sharedCommunity = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSurvaySubmittedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAnotherVisit.setOnClickListener{
            sharedCommunity = false
            clicked = false
            findNavController().navigate(R.id.action_surveySubmittedFragment_to_visitFormFragment2)
        }
        binding.btnShare.setOnClickListener{
//            findNavController().navigate(R.id.surveySubmittedFragment)
          //  showSharePopup()
            sharedCommunity  =true
            clicked = false
            showCustomDialogForSC()
        }
        binding.btnReturnHome.setOnClickListener{
            clicked =true
            sharedCommunity = false
            findNavController().navigate(R.id.action_surveySubmittedFragment_to_nav_visit)
        }
        // Handle back button press
       val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                clicked =true
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

    fun showCustomDialogForSC() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_popup_shared_comm_visit_log, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // This removes the black border and makes corners visible
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        val btnOK = dialogView.findViewById<TextView>(R.id.ok_btn)
        val cancel_btn = dialogView.findViewById<TextView>(R.id.cancel_btn)


        btnOK.setOnClickListener {
            // Logic for confirming the share action
            saveVisitLog()
            // Reset the visit log for future use
            sharedVisitViewModel.resetVisitLogPage()
            findNavController().navigate(R.id.action_surveySubmittedFragment_to_sharedCommunityVisitLogFragment)
            dialog.dismiss()

        }
        cancel_btn.setOnClickListener {
            dialog.dismiss()
        }




        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.80).toInt(), // 85% of screen width
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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
                    val visitData = hashMapOf(
                        "whenVisit" to visitLog.date, //1
                        //"whenVisitTime" to visitLog.whenVisitTime, //1
                        "whereVisit" to visitLog.whereVisit, //2
                        //"Location" to visitLog.locationmap, //2
                        "locationDescription" to visitLog.locationDescription, //2
                        "peopleHelped" to visitLog.peopleCount, //3
                        "peopleHelpedDescription" to visitLog.names, //3
                        "foodAndDrinks" to visitLog.food_drink, //4
                        "clothes" to visitLog.clothes, //4
                        "hygiene" to visitLog.hygiene, //4
                        "wellness" to visitLog.wellness, //4
                        "medical" to visitLog.medicalhelp, //4
                        "social" to visitLog.socialWorker, //4
                        "legal" to visitLog.lawyerLegal, //4
                        "other" to visitLog.other, //4
                        "whatGiven" to visitLog.whattogive, //4
                        "otherNotes" to visitLog.otherDetail, //4
                        "itemQty" to visitLog.number_of_items, //5
                        "itemQtyDescription" to visitLog.itemQtyDescription, //5
                        "rating" to visitLog.experience, //6
                        "ratingNotes" to visitLog.comments, //6

                        "durationHours" to visitLog.visitedHours, //1A
                        "durationMinutes" to visitLog.visitedMinutes, //1A
                        "numberOfHelpers" to visitLog.whoJoined, //2A
                        "numberOfHelpersComment" to visitLog.numberOfHelpersComment, //2A
                        "peopleNeedFurtherHelp" to visitLog.stillNeedSupport, //3A
                        "peopleNeedFurtherHelpComment" to visitLog.supportTypeNeeded, //3A
                        "peopleNeedFurtherHelpLocation" to visitLog.peopleNeedFurtherHelpLocation, //3A
                        "furtherFoodAndDrinks" to visitLog.add_food_drink, //4A
                        "furtherClothes" to visitLog.add_clothes, //4A
                        "furtherHygiene" to visitLog.add_hygine, //4A
                        "furtherWellness" to visitLog.add_wellness, //4A
                        "furtherMedical" to visitLog.add_medicalhelp, //4A
                        "furtherSocial" to visitLog.add_socialWorker, //4A
                        "furtherLegal" to visitLog.add_lawyerLegal, //4A
                        "furtherOther" to visitLog.add_other, //4A
                        "furtherOtherNotes" to visitLog.add_otherDetail, //4A
                        "whatGivenFurther" to visitLog.whatrequired, //4A
                        "followUpWhenVisit" to visitLog.followupDate, //5A
                        "futureNotes" to visitLog.futureNotes, //6A
                        "volunteerAgain" to visitLog.visitAgain, //7A

                        "lastEdited" to visitLog.lastEditedTime,
                        "type" to visitLog.typeofdevice,
                        "timeStamp" to visitLog.createdTime,
                        "uid" to user.uid,
                        "isPublic" to visitLog.share,
                        "isFlagged" to visitLog.isFlagged,
                        "flaggedByUser" to visitLog.flaggedByUser
//                //"public" to true,
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

        if (clicked && !sharedCommunity) {

            requireActivity()
                .findViewById<BottomNavigationView>(R.id.bottomNav)
                .selectedItemId = R.id.loginRedirectFragment
            clicked = false
        }
     /*   } else if (!clicked && !sharedCommunity) {
            requireActivity()
                .findViewById<BottomNavigationView>(R.id.bottomNav)
                .selectedItemId = R.id.loginRedirectFragment
        } else {

        }*/
    }
}// end of class
