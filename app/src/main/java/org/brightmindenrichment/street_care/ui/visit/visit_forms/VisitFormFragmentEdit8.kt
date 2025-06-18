package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import org.brightmindenrichment.street_care.R
import java.util.Date

class VisitFormFragmentEdit8 : Fragment() {

    private lateinit var etNoOfPeople: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnIncrease: ImageButton
    private lateinit var btnDecrease: ImageButton
    private lateinit var btnUpdate: TextView
    private lateinit var btnPrev: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_additional5_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etNoOfPeople = view.findViewById(R.id.et_no_of_people)
        etDescription = view.findViewById(R.id.description_example)
        btnIncrease = view.findViewById(R.id.increase_no_of_people)
        btnDecrease = view.findViewById(R.id.decrease_no_of_people)
        btnUpdate = view.findViewById(R.id.txt_Next3) // Adjust ID as per your layout
        btnPrev =  view.findViewById(R.id.txt_previous3)

        val visitId = arguments?.getString("visitId")
        val numberOfPeople = arguments?.getString("fieldName1")
        val description = arguments?.getString("fieldName2")

        Log.d("VisitFormFragmentEdit8", "visitId: $visitId")
        Log.d("VisitFormFragmentEdit8", "numberOfPeople: $numberOfPeople")
        Log.d("VisitFormFragmentEdit8", "description: $description")

        // Prepopulate fields
        etNoOfPeople.setText(numberOfPeople ?: "0")
        etDescription.setText(description ?: "")

        // Increase button increments the number by 1
        btnIncrease.setOnClickListener {
            val currentValue = etNoOfPeople.text.toString().toIntOrNull() ?: 0
            etNoOfPeople.setText((currentValue + 1).toString())
        }

        // Decrease button decrements the number by 1 but not below 0
        btnDecrease.setOnClickListener {
            val currentValue = etNoOfPeople.text.toString().toIntOrNull() ?: 0
            if (currentValue > 0) {
                etNoOfPeople.setText((currentValue - 1).toString())
            }
        }

        btnPrev.setOnClickListener {
            requireActivity().onBackPressed()
        }


        // Update button writes changes to Firestore
        btnUpdate.setOnClickListener {
            val updatedNumber = etNoOfPeople.text.toString().toIntOrNull() ?: 0
            val updatedDescription = etDescription.text.toString()

            if (visitId == null) {
                Toast.makeText(requireContext(), "Visit ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val deviceType = arguments?.getString("fieldName0") ?: ""
            val isAndroid = deviceType.equals("Android", ignoreCase = true)
            val db = FirebaseFirestore.getInstance()

            db.collection("VisitLogBook_New").document(visitId).get()
                .addOnSuccessListener { doc ->
                    val collection: String
                    val updateData: Map<String, Any>

                    if (doc.exists()) {
                        collection = "VisitLogBook_New"
                        updateData = mapOf(
                            "numberOfHelpers" to updatedNumber,
                            "numberOfHelpersComment" to updatedDescription,
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
                                    "whoJoined" to updatedNumber
                                )
                            )
                            findNavController().popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("VisitFormFragmentEdit8", "Error updating document", e)
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error checking document: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("VisitFormFragmentEdit8", "Error checking document existence", e)
                }
        }

    }
}
