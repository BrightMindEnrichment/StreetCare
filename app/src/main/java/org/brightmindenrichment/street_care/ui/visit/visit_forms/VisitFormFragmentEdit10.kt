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
import java.util.Date

class VisitFormFragmentEdit10 : Fragment() {

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

    private val TAG = "VisitFormFragmentEdit10"

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
        return inflater.inflate(R.layout.fragment_additional8_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind UI components with their IDs
        cbFoodDrink = view.findViewById(R.id.AD1)
        cbClothes = view.findViewById(R.id.AD2)
        cbHygiene = view.findViewById(R.id.AD3)
        cbWellness = view.findViewById(R.id.AD4)
        cbMedicalHelp = view.findViewById(R.id.AD6)
        cbSocialWorker = view.findViewById(R.id.AD7)
        cbLawyerLegal = view.findViewById(R.id.AD8)
        cbOther = view.findViewById(R.id.AD5)
        edtOtherDescription = view.findViewById(R.id.edt_other)

        btnPrevious = view.findViewById(R.id.txt_previous3)
        btnNext = view.findViewById(R.id.txt_Next3)




        // Show/hide EditText based on "Other" checkbox
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

            val db = FirebaseFirestore.getInstance()
            val deviceType = arguments?.getString("fieldName0") ?: ""

            db.collection("VisitLogBook_New").document(visitId!!).get()
                .addOnSuccessListener { doc ->
                    val (collection, isNew, isIOS) = if (doc.exists()) {
                        Triple("VisitLogBook_New", true, false)
                    } else {
                        val isIOSPlatform = deviceType == "iOS"
                        Triple("VisitLogBook", false, isIOSPlatform)
                    }

                    val updatesMap = if (isNew) {
                        mapOf(
                            "furtherFoodAndDrinks" to cbFoodDrink.isChecked,
                            "furtherClothes" to cbClothes.isChecked,
                            "furtherHygiene" to cbHygiene.isChecked,
                            "furtherWellness" to cbWellness.isChecked,
                            "furtherLegal" to cbLawyerLegal.isChecked,
                            "furtherMedical" to cbMedicalHelp.isChecked,
                            "furtherSocial" to cbSocialWorker.isChecked,
                            "furtherOther" to cbOther.isChecked,
                            "furtherOtherNotes" to edtOtherDescription.text.toString().trim(),
                            "whatGivenFurther" to helpTypeList,
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

                    db.collection(collection).document(visitId!!)
                        .update(updatesMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "Visit log updated successfully")
                            Toast.makeText(requireContext(), "Update successful", Toast.LENGTH_SHORT).show()
                            setFragmentResult("visit_updated", bundleOf("updated" to true, "whatGivenFurther" to helpTypeList))
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
