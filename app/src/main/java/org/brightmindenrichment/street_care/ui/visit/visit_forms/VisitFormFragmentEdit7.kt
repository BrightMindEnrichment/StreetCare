//package org.brightmindenrichment.street_care.ui.visit.visit_forms
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ArrayAdapter
//import android.widget.AutoCompleteTextView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.google.firebase.firestore.FirebaseFirestore
//import org.brightmindenrichment.street_care.R
//
//class VisitFormFragmentEdit7 : Fragment() {
//
//    private lateinit var selectHour: AutoCompleteTextView
//    private lateinit var selectMinute: AutoCompleteTextView
//    private lateinit var txtBack: TextView
//    private lateinit var txtNext: TextView
//    private lateinit var firestore: FirebaseFirestore
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_additional2_edit, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Initialize Firebase
//        firestore = FirebaseFirestore.getInstance()
//
//        // Get references to views
//        selectHour = view.findViewById(R.id.selectHour)
//        selectMinute = view.findViewById(R.id.selectMinute)
//        txtBack = view.findViewById(R.id.txt_back)
//        txtNext = view.findViewById(R.id.txt_next)
//
//        // Sample data for dropdowns
//        val hoursList = (0..12).map { it.toString().padStart(2, '0') }
//        val minutesList = (0..59).map { it.toString().padStart(2, '0') }
//
//
//        // Set dropdown adapters
//        selectHour.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, hoursList))
//        selectMinute.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, minutesList))
//
//        // Get passed arguments and pre-fill fields
//        val docId = arguments?.getString("visitId")  // Firestore document ID to update
//        val hourArg = arguments?.getString("fieldName1")
//        val minuteArg = arguments?.getString("fieldName2")
//
//        hourArg?.let { selectHour.setText(it, false) }
//        minuteArg?.let { selectMinute.setText(it, false) }
//
//        // Handle Cancel (Back)
//        txtBack.setOnClickListener {
//            findNavController().popBackStack()
//        }
//
//        // Handle Update (Next)
//        txtNext.setOnClickListener {
//            val updatedHour = selectHour.text.toString().trim()
//            val updatedMinute = selectMinute.text.toString().trim()
//
//            if (updatedHour.isEmpty() || updatedMinute.isEmpty()) {
//                Toast.makeText(requireContext(), "Please select both hour and minute.", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (docId != null) {
//                val updateMap = mapOf(
//                    "hour" to updatedHour,
//                    "minute" to updatedMinute
//                )
//
//                firestore.collection("visit_logs").document(docId)
//                    .update(updateMap)
//                    .addOnSuccessListener {
//                        Toast.makeText(requireContext(), "Updated successfully!", Toast.LENGTH_SHORT).show()
//                        findNavController().popBackStack()
//                    }
//                    .addOnFailureListener { e ->
//                        Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
//                    }
//            } else {
//                Toast.makeText(requireContext(), "Invalid document reference.", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//}

package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import org.brightmindenrichment.street_care.R

class VisitFormFragmentEdit7 : Fragment() {

    private lateinit var selectHour: AutoCompleteTextView
    private lateinit var selectMinute: AutoCompleteTextView
    private lateinit var txtBack: TextView
    private lateinit var txtNext: TextView
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_additional2_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        selectHour = view.findViewById(R.id.selectHour)
        selectMinute = view.findViewById(R.id.selectMinute)
        txtBack = view.findViewById(R.id.txt_back)
        txtNext = view.findViewById(R.id.txt_next)

        val hoursList = (0..12).map { it.toString() }
        val minutesList = (0..59).map { it.toString() }

        val hourAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, hoursList)
        val minuteAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, minutesList)

        selectHour.setAdapter(hourAdapter)
        selectMinute.setAdapter(minuteAdapter)

        selectHour.setOnClickListener { selectHour.showDropDown() }
        selectMinute.setOnClickListener { selectMinute.showDropDown() }

        selectHour.setOnItemClickListener { _, _, position, _ ->
            val selected = hoursList[position]
            val formatted = "$selected hour${if (selected == "1") "" else "s"}"
            selectHour.setText(formatted, false)
        }

        selectMinute.setOnItemClickListener { _, _, position, _ ->
            val selected = minutesList[position]
            val formatted = "$selected minute${if (selected == "1") "" else "s"}"
            selectMinute.setText(formatted, false)
        }

        val docId = arguments?.getString("visitId")
        val hourArg = arguments?.getString("fieldName1")
        val minuteArg = arguments?.getString("fieldName2")

        hourArg?.let {
            val formatted = "$it hour${if (it == "1") "" else "s"}"
            selectHour.setText(formatted, false)
        }

        minuteArg?.let {
            val formatted = "$it minute${if (it == "1") "" else "s"}"
            selectMinute.setText(formatted, false)
        }

        txtBack.setOnClickListener {
            findNavController().popBackStack()
        }

        txtNext.setOnClickListener {
            val hourText = selectHour.text.toString().trim().split(" ").firstOrNull() ?: ""
            val minuteText = selectMinute.text.toString().trim().split(" ").firstOrNull() ?: ""

            if (hourText.isEmpty() || minuteText.isEmpty()) {
                Toast.makeText(requireContext(), "Please select both hour and minute.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (docId == null) {
                Toast.makeText(requireContext(), "Invalid document reference.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val deviceType = arguments?.getString("fieldName0") ?: ""
            val isAndroid = deviceType.equals("Android", ignoreCase = true)

            val db = FirebaseFirestore.getInstance()
            db.collection("VisitLogBook_New").document(docId).get()
                .addOnSuccessListener { doc ->
                    val collection: String
                    val updateData: Map<String, Any>

                    if (doc.exists()) {
                        // Use VisitLogBook_New and default keys
                        collection = "VisitLogBook_New"
                        updateData = mapOf(
                            "durationHours" to hourText,
                            "durationMinutes" to minuteText
                        )
                    } else {
                        // Use VisitLogBook and apply Android-specific keys if needed
                        Toast.makeText(
                            requireContext(),
                            "This log cannot be edited.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnSuccessListener
                    }

                    db.collection(collection).document(docId)
                        .update(updateData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Updated successfully!", Toast.LENGTH_SHORT).show()
                            val helpTime = "$hourText hr, $minuteText min"

                            setFragmentResult(
                                "visit_updated",
                                bundleOf(
                                    "updated" to true,
                                    "helpTime" to helpTime
                                )
                            )

                            findNavController().popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error checking document: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
