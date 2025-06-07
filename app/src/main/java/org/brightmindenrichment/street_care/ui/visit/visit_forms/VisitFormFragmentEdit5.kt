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

class VisitFormFragmentEdit5 : Fragment() {

    private lateinit var etItemCount: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnIncrease: ImageButton
    private lateinit var btnDecrease: ImageButton
    private lateinit var btnUpdate: TextView
    private lateinit var btnCancel: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_visit_form6_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etItemCount = view.findViewById(R.id.edit_box12)
        etDescription = view.findViewById(R.id.enter_notes)
        btnIncrease = view.findViewById(R.id.increase_no_of_items)
        btnDecrease = view.findViewById(R.id.decrease_no_of_items)
        btnUpdate = view.findViewById(R.id.txtNextAdd420)
        btnCancel = view.findViewById(R.id.txtPreviousAdd420)

        val visitId = arguments?.getString("visitId")
        val itemCount = arguments?.getString("fieldName1")
        val notes = arguments?.getString("fieldName2")

        Log.d("VisitFormFragmentEdit5", "visitId: $visitId")
        Log.d("VisitFormFragmentEdit5", "itemCount: $itemCount")
        Log.d("VisitFormFragmentEdit5", "notes: $notes")

        etItemCount.setText(itemCount ?: "0")
        etDescription.setText(notes ?: "")

        btnIncrease.setOnClickListener {
            val currentValue = etItemCount.text.toString().toIntOrNull() ?: 0
            etItemCount.setText((currentValue + 1).toString())
        }

        btnDecrease.setOnClickListener {
            val currentValue = etItemCount.text.toString().toIntOrNull() ?: 0
            if (currentValue > 0) {
                etItemCount.setText((currentValue - 1).toString())
            }
        }

        btnCancel.setOnClickListener {
            requireActivity().onBackPressed()
        }

        btnUpdate.setOnClickListener {
            val number = etItemCount.text.toString().toIntOrNull() ?: 0
            val description = etDescription.text.toString()

            if (visitId == null) {
                Toast.makeText(requireContext(), "Visit ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updateData = hashMapOf<String, Any>(
                "number_of_items_donated" to number,
                "notes" to description
            )

            db.collection("VisitLogBook")
                .document(visitId)
                .update(updateData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("VisitFormFragmentEdit5", "Error updating document", e)
                }
        }
    }
}
