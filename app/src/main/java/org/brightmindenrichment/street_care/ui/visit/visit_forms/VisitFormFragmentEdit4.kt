//package org.brightmindenrichment.street_care.ui.visit.visit_forms
//
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.CheckBox
//import android.widget.EditText
//import android.widget.TextView
//import android.widget.Toast
//import androidx.core.os.bundleOf
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.setFragmentResult
//import androidx.navigation.fragment.findNavController
//import com.google.firebase.firestore.FirebaseFirestore
//import org.brightmindenrichment.street_care.R
//
//class VisitFormFragmentEdit4 : Fragment() {
//
//    private lateinit var cbFoodDrink: CheckBox
//    private lateinit var cbClothes: CheckBox
//    private lateinit var cbHygiene: CheckBox
//    private lateinit var cbWellness: CheckBox
//    private lateinit var cbLawyerLegal: CheckBox
//    private lateinit var cbMedicalHelp: CheckBox
//    private lateinit var cbSocialWorker: CheckBox
//    private lateinit var cbOther: CheckBox
//    private lateinit var edtOtherDescription: EditText
//
//    private lateinit var btnPrevious: TextView
//    private lateinit var btnNext: TextView
//
//    private var visitId: String? = null
//
//    private val TAG = "VisitFormFragmentEdit4"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        visitId = arguments?.getString("visitId")
//        Log.d(TAG, "Received visitId: $visitId")
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_visit_form4_edit, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Initialize views
//        cbFoodDrink = view.findViewById(R.id.CB1)
//        cbClothes = view.findViewById(R.id.CB2)
//        cbHygiene = view.findViewById(R.id.CB3)
//        cbWellness = view.findViewById(R.id.CB4)
//        cbMedicalHelp= view.findViewById(R.id.CB6)
//        cbSocialWorker= view.findViewById(R.id.CB7)
//        cbLawyerLegal= view.findViewById(R.id.CB8)
//        cbOther = view.findViewById(R.id.CB5)
//        edtOtherDescription = view.findViewById(R.id.edt_other)
//
//        btnPrevious = view.findViewById(R.id.txtPrevious4)
//        btnNext = view.findViewById(R.id.txtNext4)
//
//        // Show or hide description based on "Other" checkbox
//        cbOther.setOnCheckedChangeListener { _, isChecked ->
//            edtOtherDescription.visibility = if (isChecked) View.VISIBLE else View.GONE
//        }
//
//        // Previous button simply goes back
//        btnPrevious.setOnClickListener {
//            findNavController().popBackStack()
//        }
//
//        // Next button collects selections and updates Firestore
//        btnNext.setOnClickListener {
//            if (visitId == null) {
//                Log.e(TAG, "visitId is null, cannot update")
//                return@setOnClickListener
//            }
//
//            val helpTypeList = mutableListOf<String>()
//
//            if (cbFoodDrink.isChecked) helpTypeList.add("food/drink")
//            if (cbClothes.isChecked) helpTypeList.add("clothes")
//            if (cbHygiene.isChecked) helpTypeList.add("hygiene")
//            if (cbWellness.isChecked) helpTypeList.add("wellness")
//            if (cbLawyerLegal.isChecked) helpTypeList.add("lawyer/legal")
//            if (cbMedicalHelp.isChecked) helpTypeList.add("medical")
//            if (cbSocialWorker.isChecked) helpTypeList.add("social")
//            if (cbOther.isChecked) {
//                val otherText = edtOtherDescription.text.toString().trim()
//                if (otherText.isNotEmpty()) {
//                    helpTypeList.add(otherText)
//                } else {
//                    // If "Other" is checked but no text provided, add generic "other"
//                    helpTypeList.add("other")
//                }
//            }
//
//            Log.d(TAG, "Help types selected: $helpTypeList")
//            val helpTypeString = helpTypeList.joinToString(", ")
//
//            val db = FirebaseFirestore.getInstance()
//            val deviceType = arguments?.getString("fieldName0") ?: ""
//
//
//            db.collection("VisitLogBook_New").document(visitId!!).get()
//                .addOnSuccessListener { doc ->
//                    val (collection, field) = if (doc.exists()) {
//                        "VisitLogBook_New" to "whatGiven"
//
//                    } else {
//                        "VisitLogBook" to if (deviceType == "Android") "WhatGiven" else "whatGiven"
//                    }
//
//                    db.collection(collection).document(visitId!!)
//                        .update(field, helpTypeList)
//                        .addOnSuccessListener {
//                            Log.d(TAG, "Visit log updated successfully")
//                            Toast.makeText(requireContext(), "Update successful", Toast.LENGTH_SHORT).show()
//
//                            setFragmentResult(
//                                "visit_updated",
//                                bundleOf(
//                                    "updated" to true,
//                                    "whatGiven" to helpTypeString
//                                )
//                            )
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e(TAG, "Error updating visit log", e)
//                        }
//                }
//                .addOnFailureListener { e ->
//                    Log.e(TAG, "Error checking document existence", e)
//                }
//
//        }
//    }
//}



package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import org.brightmindenrichment.street_care.R

class VisitFormFragmentEdit4 : Fragment() {

    private lateinit var cbFoodDrink: CheckBox
    private lateinit var cbClothes: CheckBox
    private lateinit var cbHygiene: CheckBox
    private lateinit var cbWellness: CheckBox
    private lateinit var cbLawyerLegal: CheckBox
    private lateinit var cbMedicalHelp: CheckBox
    private lateinit var cbSocialWorker: CheckBox
    private lateinit var cbOther: CheckBox
    private lateinit var edtOtherDescription: EditText

    private lateinit var btnPrevious: TextView
    private lateinit var btnNext: TextView

    private var visitId: String? = null
    private val TAG = "VisitFormFragmentEdit4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitId = arguments?.getString("visitId")
        Log.d(TAG, "Received visitId: $visitId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_visit_form4_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cbFoodDrink = view.findViewById(R.id.CB1)
        cbClothes = view.findViewById(R.id.CB2)
        cbHygiene = view.findViewById(R.id.CB3)
        cbWellness = view.findViewById(R.id.CB4)
        cbMedicalHelp = view.findViewById(R.id.CB6)
        cbSocialWorker = view.findViewById(R.id.CB7)
        cbLawyerLegal = view.findViewById(R.id.CB8)
        cbOther = view.findViewById(R.id.CB5)
        edtOtherDescription = view.findViewById(R.id.edt_other)

        btnPrevious = view.findViewById(R.id.txtPrevious4)
        btnNext = view.findViewById(R.id.txtNext4)

        cbOther.setOnCheckedChangeListener { _, isChecked ->
            edtOtherDescription.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        btnPrevious.setOnClickListener {
            findNavController().popBackStack()
        }

        btnNext.setOnClickListener {
            if (visitId == null) {
                Log.e(TAG, "visitId is null, cannot update")
                return@setOnClickListener
            }

            val helpTypeList = mutableListOf<String>()
            if (cbFoodDrink.isChecked) helpTypeList.add("food/drink")
            if (cbClothes.isChecked) helpTypeList.add("clothes")
            if (cbHygiene.isChecked) helpTypeList.add("hygiene")
            if (cbWellness.isChecked) helpTypeList.add("wellness")
            if (cbLawyerLegal.isChecked) helpTypeList.add("lawyer/legal")
            if (cbMedicalHelp.isChecked) helpTypeList.add("medical")
            if (cbSocialWorker.isChecked) helpTypeList.add("social")
            if (cbOther.isChecked) {
                val otherText = edtOtherDescription.text.toString().trim()
                if (otherText.isNotEmpty()) {
                    helpTypeList.add(otherText)
                } else {
                    helpTypeList.add("other")
                }
            }

            val helpTypeString = helpTypeList.joinToString(", ")
            val deviceType = arguments?.getString("fieldName0") ?: ""
            val db = FirebaseFirestore.getInstance()

            db.collection("VisitLogBook_New").document(visitId!!).get()
                .addOnSuccessListener { doc ->
                    val (collection, isNew, isIOS) = if (doc.exists()) {
                        Triple("VisitLogBook_New", true, false)
                    } else {
                        val isIOSPlatform = deviceType == "iOS"
                        Triple("VisitLogBook", false, isIOSPlatform)
                    }


                    val updatesMap = if (isNew) {
                        // For VisitLogBook_New → lower camelCase
                        mapOf(
                            "foodAndDrinks" to cbFoodDrink.isChecked,
                            "clothes" to cbClothes.isChecked,
                            "hygiene" to cbHygiene.isChecked,
                            "wellness" to cbWellness.isChecked,
                            "legal" to cbLawyerLegal.isChecked,
                            "medical" to cbMedicalHelp.isChecked,
                            "social" to cbSocialWorker.isChecked,
                            "other" to cbOther.isChecked,
                            "otherNotes" to edtOtherDescription.text.toString().trim(),
                            "whatGiven" to helpTypeList
                        )
                    } else {
                        // For VisitLogBook → platform-specific field names
                        if (isIOS) {
                            mapOf(
                                "foodAndDrinks" to cbFoodDrink.isChecked,
                                "clothes" to cbClothes.isChecked,
                                "hygine" to cbHygiene.isChecked,
                                "wellness" to cbWellness.isChecked,
                                "legal" to cbLawyerLegal.isChecked,
                                "medical" to cbMedicalHelp.isChecked,
                                "social" to cbSocialWorker.isChecked,
                                "other" to cbOther.isChecked,
                                "otherNotes" to edtOtherDescription.text.toString().trim(),
                                "whatGiven" to helpTypeList
                            )
                        } else {
                            mapOf(
                                "FoodAndDrinks" to cbFoodDrink.isChecked,
                                "Clothes" to cbClothes.isChecked,
                                "Hygiene" to cbHygiene.isChecked,
                                "Wellness" to cbWellness.isChecked,
                                "LawyerLegal" to cbLawyerLegal.isChecked,
                                "Medical" to cbMedicalHelp.isChecked,
                                "SocialWorker" to cbSocialWorker.isChecked,
                                "OtherChecked" to cbOther.isChecked,
                                "OtherDescription" to edtOtherDescription.text.toString().trim(),
                                "WhatGiven" to helpTypeList
                            )
                        }
                    }

                    db.collection(collection).document(visitId!!)
                        .update(updatesMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "Visit log updated successfully")
                            Toast.makeText(requireContext(), "Update successful", Toast.LENGTH_SHORT).show()

                            setFragmentResult(
                                "visit_updated",
                                bundleOf("updated" to true, "whatGiven" to helpTypeString)
                            )
                            findNavController().popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error updating visit log", e)
                            Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error checking document existence", e)
                    Toast.makeText(requireContext(), "Failed to load document", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
