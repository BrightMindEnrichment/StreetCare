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
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import org.brightmindenrichment.street_care.R

class VisitFormFragmentEdit3 : Fragment() {

    private lateinit var etPeopleHelped: EditText
    private lateinit var etPeopleHelpedDescription: EditText
    private lateinit var btnIncrease: ImageButton
    private lateinit var btnDecrease: ImageButton
    private lateinit var btnUpdate: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_visit_form3_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etPeopleHelped = view.findViewById(R.id.et_no_of_people)
        etPeopleHelpedDescription = view.findViewById(R.id.description_example)
        btnIncrease = view.findViewById(R.id.increase_no_of_people)
        btnDecrease = view.findViewById(R.id.decrease_no_of_people)
        btnUpdate = view.findViewById(R.id.txt_Next3)

        val visitId = arguments?.getString("visitId")
        val peopleHelped = arguments?.getString("fieldName1")
        val description = arguments?.getString("fieldName2")

        Log.d("VisitFormFragmentEdit3", "visitId: $visitId")
        Log.d("VisitFormFragmentEdit3", "peopleHelped: $peopleHelped")
        Log.d("VisitFormFragmentEdit3", "PeopleHelpedDescription: $description")

        etPeopleHelped.setText(peopleHelped ?: "0")
        etPeopleHelpedDescription.setText(description ?: "")

        // Increase button increments the number by 1
        btnIncrease.setOnClickListener {
            val currentValue = etPeopleHelped.text.toString().toIntOrNull() ?: 0
            etPeopleHelped.setText((currentValue + 1).toString())
        }

        // Decrease button decrements the number by 1 but not below 0
        btnDecrease.setOnClickListener {
            val currentValue = etPeopleHelped.text.toString().toIntOrNull() ?: 0
            if (currentValue > 0) {
                etPeopleHelped.setText((currentValue - 1).toString())
            }
        }

        val btnPrev: View = view.findViewById(R.id.txt_previous3)

        btnPrev.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Update button writes changes to Firestore
        btnUpdate.setOnClickListener {
            val numberOfPeople = etPeopleHelped.text.toString().toIntOrNull() ?: 0
            val descriptionText = etPeopleHelpedDescription.text.toString()

            if (visitId == null) {
                Toast.makeText(requireContext(), "Visit ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updateData = hashMapOf<String, Any>(
                "NumberOfPeopleHelped" to numberOfPeople,
                "PeopleHelpedDescription" to descriptionText
            )

            db.collection("VisitLogBook")
                .document(visitId)
                .update(updateData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("VisitFormFragmentEdit3", "Error updating document", e)
                }
        }
    }
}
