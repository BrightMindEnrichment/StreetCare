package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import org.brightmindenrichment.street_care.R

class VisitFormFragmentEdit6 : Fragment() {

    private lateinit var starViews: List<ImageView>
    private lateinit var edtComment: EditText
    private var currentRating = 0
    private var visitId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_visit_form5_edit, container, false)

        // Initialize star views
        starViews = listOf(
            view.findViewById(R.id.star1),
            view.findViewById(R.id.star2),
            view.findViewById(R.id.star3),
            view.findViewById(R.id.star4),
            view.findViewById(R.id.star5)
        )

        // Set star click listeners
        for ((index, star) in starViews.withIndex()) {
            star.setOnClickListener {
                setRating(index + 1)
            }
        }



        // Get arguments
        visitId = arguments?.getString("visitId")
        val existingRating = arguments?.getInt("fieldName1") ?: 0
        val existingComment = arguments?.getString("fieldName2") ?: ""
        Log.d("V", "User-entered rating notes: $existingRating")
        Log.d("V", "User-entered rating notes: $existingComment")

        // Set initial rating and comment
        setRating(existingRating)
        edtComment = view.findViewById(R.id.edtcomment)
        edtComment.setText(existingComment)

        // Set click listener for the "Update" button
        view.findViewById<TextView>(R.id.txt_next5).setOnClickListener {
            if (visitId.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Invalid visit ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updateData = hashMapOf<String, Any>(
                "rating" to currentRating,
                "comments" to edtComment.text.toString()
            )

            FirebaseFirestore.getInstance()
                .collection("VisitLogBook")
                .document(visitId!!)
                .update(updateData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                }
        }

        // "Cancel" button listener
        view.findViewById<TextView>(R.id.txt_previous5).setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }

    private fun setRating(rating: Int) {
        currentRating = rating
        for (i in starViews.indices) {
            val drawableId = if (i < rating) R.drawable.filled_star else R.drawable.empty_star
            starViews[i].setImageResource(drawableId)
        }
    }
}
