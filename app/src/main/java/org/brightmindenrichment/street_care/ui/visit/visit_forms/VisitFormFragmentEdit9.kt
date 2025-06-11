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

class VisitFormFragmentEdit9 : Fragment() {

    private lateinit var etPeopleHelped: EditText
    private lateinit var etDescriptionExample: EditText
    private lateinit var etLocationDescription: EditText
    private lateinit var btnIncrease: ImageButton
    private lateinit var btnDecrease: ImageButton
    private lateinit var btnUpdate: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_additional6_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etPeopleHelped = view.findViewById(R.id.et_no_of_people)
        etDescriptionExample = view.findViewById(R.id.description_example)
        etLocationDescription = view.findViewById(R.id.location_description)
        btnIncrease = view.findViewById(R.id.increase_no_of_people)
        btnDecrease = view.findViewById(R.id.decrease_no_of_people)
        btnUpdate = view.findViewById(R.id.txt_Next3)

        val visitId = arguments?.getString("visitId")
        val peopleHelped = arguments?.getString("fieldName1")
        val description = arguments?.getString("fieldName2")
        val locationDesc = arguments?.getString("fieldName3")

        Log.d("VisitFormFragmentEdit9", "visitId: $visitId")
        Log.d("VisitFormFragmentEdit9", "peopleHelped: $peopleHelped")
        Log.d("VisitFormFragmentEdit9", "descriptionExample: $description")
        Log.d("VisitFormFragmentEdit9", "locationDescription: $locationDesc")

        etPeopleHelped.setText(peopleHelped ?: "0")
        etDescriptionExample.setText(description ?: "")
        etLocationDescription.setText(locationDesc ?: "")

        btnIncrease.setOnClickListener {
            val currentValue = etPeopleHelped.text.toString().toIntOrNull() ?: 0
            etPeopleHelped.setText((currentValue + 1).toString())
        }

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

        btnUpdate.setOnClickListener {
            val numberOfPeople = etPeopleHelped.text.toString().toIntOrNull() ?: 0
            val descriptionText = etDescriptionExample.text.toString()
            val locationText = etLocationDescription.text.toString()

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
                            "peopleNeedFurtherHelp" to numberOfPeople,
                            "peopleNeedFurtherHelpComment" to descriptionText,
                            "peopleNeedFurtherHelpLocation" to locationText
                        )
                    } else {
                        collection = "VisitLogBook"
                        updateData = if (isAndroid) {
                            mapOf(
                                "stillNeedSupport" to numberOfPeople,
                                "peopleNeedFurtherHelpComment" to descriptionText,
                                "peopleNeedFurtherHelpLocation" to locationText
                            )
                        } else {
                            mapOf(
                                "peopleNeedFurtherHelp" to numberOfPeople,
                                "peopleNeedFurtherHelpComment" to descriptionText,
                                "peopleNeedFurtherHelpLocation" to locationText
                            )
                        }
                    }

                    db.collection(collection).document(visitId)
                        .update(updateData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_SHORT).show()
                            setFragmentResult(
                                "visit_updated",
                                bundleOf(
                                    "updated" to true,
                                    "stillNeedSupport" to numberOfPeople
                                )
                            )
                            findNavController().popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("VisitFormFragmentEdit9", "Error updating document", e)
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error checking document: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("VisitFormFragmentEdit9", "Error checking collection existence", e)
                }
        }

    }
}
