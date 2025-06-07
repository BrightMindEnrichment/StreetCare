package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import org.brightmindenrichment.street_care.R

class VisitFormFragmentEdit12 : Fragment() {

    private lateinit var etAdditionalNotes: EditText
    private lateinit var btnUpdate: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_additional9_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etAdditionalNotes = view.findViewById(R.id.description_example)
        btnUpdate = view.findViewById(R.id.txt_Next3)

        val visitId = arguments?.getString("visitId")
        val notes = arguments?.getString("fieldName1")

        Log.d("VisitFormFragmentEdit12", "visitId: $visitId")
        Log.d("VisitFormFragmentEdit12", "additionalNotes: $notes")

        etAdditionalNotes.setText(notes ?: "")

        val btnPrev: View = view.findViewById(R.id.txt_previous3)
        btnPrev.setOnClickListener {
            requireActivity().onBackPressed()
        }

        btnUpdate.setOnClickListener {
            val updatedNotes = etAdditionalNotes.text.toString()

            if (visitId == null) {
                Toast.makeText(requireContext(), "Visit ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updateData = hashMapOf<String, Any>(
                "future_notes" to updatedNotes
            )

            db.collection("VisitLogBook")
                .document(visitId)
                .update(updateData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("VisitFormFragmentEdit12", "Error updating document", e)
                }
        }
    }
}
