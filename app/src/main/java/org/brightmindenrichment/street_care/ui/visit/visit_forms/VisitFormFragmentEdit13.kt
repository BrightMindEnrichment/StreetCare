package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import org.brightmindenrichment.street_care.R
import java.util.Date

class VisitFormFragmentEdit13 : Fragment() {

    private lateinit var btnYes: MaterialButton
    private lateinit var btnNo: MaterialButton
    private lateinit var btnMaybe: MaterialButton
    private lateinit var btnCancel: TextView
    private lateinit var btnUpdate: TextView

    private val db = FirebaseFirestore.getInstance()

    // To keep track of current selection
    private var selectedOption: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_additional3_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnYes = view.findViewById(R.id.btn_Yes)
        btnNo = view.findViewById(R.id.btn_No)
        btnMaybe = view.findViewById(R.id.btn_Maybe)
        btnCancel = view.findViewById(R.id.txt_back)
        btnUpdate = view.findViewById(R.id.txt_finish)

        val visitId = arguments?.getString("visitId")
        val passedAnswer = arguments?.getString("fieldName1")

        if (visitId == null) {
            Toast.makeText(requireContext(), "Visit ID missing", Toast.LENGTH_SHORT).show()
            return
        }

        selectedOption = passedAnswer
        updateButtonStates(selectedOption)


        // Set click listeners for Yes / No / Maybe
        btnYes.setOnClickListener {
            selectedOption = "Yes"
            updateButtonStates(selectedOption)
        }

        btnNo.setOnClickListener {
            selectedOption = "No"
            updateButtonStates(selectedOption)
        }

        btnMaybe.setOnClickListener {
            selectedOption = "Maybe"
            updateButtonStates(selectedOption)
        }

        btnCancel.setOnClickListener {
            requireActivity().onBackPressed()  // Just go back without saving
        }

        btnUpdate.setOnClickListener {
            if (selectedOption == null) {
                Toast.makeText(requireContext(), "Please select an option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val deviceType = arguments?.getString("fieldName0") ?: ""
            val db = FirebaseFirestore.getInstance()

            if (visitId == null) {
                Toast.makeText(requireContext(), "Visit ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("VisitLogBook_New").document(visitId).get()
                .addOnSuccessListener { doc ->
                    val (collection, updateData) = if (doc.exists()) {
                        // Document exists in VisitLogBook_New — field name is always lowercase
                        "VisitLogBook_New" to hashMapOf<String, Any>(
                            "visitAgain" to selectedOption!!,
                            "lastEdited" to Date()
                        )
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "This log cannot be edited.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnSuccessListener
                    }

                    db.collection(collection).document(visitId)
                        .update(updateData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_SHORT).show()
                            setFragmentResult(
                                "visit_updated",
                                bundleOf(
                                    "updated" to true,
                                    "visitAgain" to selectedOption
                                )
                            )
                            requireActivity().onBackPressed()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("VisitFormFragmentEdit13", "Error updating document", e)
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error checking document: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }


    }

    private fun updateButtonStates(selected: String?) {
        // Reset all buttons to default style
        btnYes.isChecked = false
        btnNo.isChecked = false
        btnMaybe.isChecked = false

        // Set the selected button as checked/highlighted
        when (selected) {
            "Yes" -> btnYes.isChecked = true
            "No" -> btnNo.isChecked = true
            "Maybe" -> btnMaybe.isChecked = true
        }
    }
}
