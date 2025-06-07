package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import org.brightmindenrichment.street_care.R

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

            val updateData = hashMapOf<String, Any>(
                "visitAgain" to selectedOption!!
            )

            db.collection("VisitLogBook")
                .document(visitId)
                .update(updateData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("VisitFormFragmentEdit13", "Error updating document", e)
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
