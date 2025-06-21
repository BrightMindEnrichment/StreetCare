package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.firestore.FirebaseFirestore
import org.brightmindenrichment.street_care.BuildConfig
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm1EditBinding

import org.brightmindenrichment.street_care.util.StateAbbreviation
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import java.util.Date


class VisitFormFragmentEdit2 : Fragment() {

    private var _binding: FragmentVisitForm1EditBinding? = null
    private val binding get() = _binding!!

    private lateinit var placesAutocomplete: ActivityResultLauncher<android.content.Intent>
    private var visitId: String? = null
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register autocomplete
        placesAutocomplete = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    result.data?.let {
                        val place = Autocomplete.getPlaceFromIntent(it)
                        val fullAddress = place.address ?: place.name
                        val street = place.address?.split(',')?.firstOrNull()?.trim() ?: ""
                        binding.customAddressField.setText(street)

                        var city: String? = null
                        var state: String? = null
                        var zipCode: String? = null
                        var stateAbbr: String? = null

                        val components = place.addressComponents
                        components?.asList()?.forEach { comp ->
                            when {
                                comp.types.contains("locality") -> city = comp.name
                                comp.types.contains("administrative_area_level_1") -> {
                                    state = comp.name
                                    stateAbbr = StateAbbreviation.getStateOrProvinceAbbreviation(state ?: "")
                                }
                                comp.types.contains("postal_code") -> zipCode = comp.name
                            }
                        }

                        binding.edtCity2.setText(city)
                        binding.edtState3.setText(stateAbbr ?: state)
                        binding.edtZipcode5.setText(zipCode)

                        Log.d("BME", "Autocomplete selected: $fullAddress")
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(result.data!!)
                    Log.e("BME", "Autocomplete error: ${status.statusMessage}")
                }
                Activity.RESULT_CANCELED -> Log.d("BME", "Autocomplete cancelled")
            }
        }

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.API_KEY_PLACES)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVisitForm1EditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visitId = arguments?.getString("visitId")
        if (visitId == null) {
            Toast.makeText(requireContext(), "Visit ID missing", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        loadVisitData()

        binding.customAddressField.setOnClickListener {
            launchPlacesAutocomplete()
        }

        binding.txtGoToPage2.setOnClickListener {
            val street = binding.customAddressField.text.toString().trim()
            val city = binding.edtCity2.text.toString().trim()
            val state = binding.edtState3.text.toString().trim()
            val zip = binding.edtZipcode5.text.toString().trim()
            val description = binding.edtLocationDescription.text.toString().trim()

            if (city.isEmpty() || state.isEmpty()) {
                if (city.isEmpty()) binding.edtCity2.error = "City required"
                if (state.isEmpty()) binding.edtState3.error = "State required"
                Toast.makeText(requireContext(), "City and State are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val stateAbbr = if (state.length == 2) state else StateAbbreviation.getStateOrProvinceAbbreviation(state)
            val formattedAddress = listOfNotNull(street, city, stateAbbr, zip).joinToString(", ")

            val updateMap = mapOf(
                "whereVisit" to formattedAddress,
                "locationDescription" to description
            )

            val locationMap = mapOf(
                "street" to street,
                "city" to city,
                "state" to state,
                "stateAbbreviation" to state,
                "zipcode" to zip
            )

            val db = FirebaseFirestore.getInstance()
            val visitId = visitId!!
            val deviceType = arguments?.getString("fieldName0") ?: ""


            db.collection("VisitLogBook_New").document(visitId).get()
                .addOnSuccessListener { doc ->
                    val (collection, updateMap) = when {
                        doc.exists() -> {
                            "VisitLogBook_New" to mapOf(
                                "whereVisit" to formattedAddress,
                                "locationDescription" to description,
                                 "lastEdited" to Date()

                            )
                        }

                        else -> {

                            Toast.makeText(
                                requireContext(),
                                "This log cannot be edited.",
                                Toast.LENGTH_LONG
                            ).show()
                            return@addOnSuccessListener

                        }
                    }

                    db.collection(collection).document(visitId).update(updateMap)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Visit update successful", Toast.LENGTH_SHORT).show()
                            setFragmentResult("visit_updated", bundleOf("updated" to true, "whereVisit" to formattedAddress))
                            findNavController().navigateUp()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show()
                            Log.e("BME", "Error updating Firestore", it)
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to check document", Toast.LENGTH_SHORT).show()
                    Log.e("BME", "Error checking document existence", it)
                }

        }

        binding.txtBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadVisitData() {
        firestore.collection("visit_logs").document(visitId!!)
            .get()
            .addOnSuccessListener { doc ->
                val whereVisit = doc.getString("whereVisit") ?: ""
                val description = doc.getString("locationDescription") ?: ""

                val parts = whereVisit.split(",").map { it.trim() }
                binding.customAddressField.setText(parts.getOrNull(0) ?: "")
                binding.edtCity2.setText(parts.getOrNull(1) ?: "")
                binding.edtState3.setText(parts.getOrNull(2) ?: "")
                binding.edtZipcode5.setText(parts.getOrNull(3) ?: "")
                binding.edtLocationDescription.setText(description)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load visit data", Toast.LENGTH_SHORT).show()
                Log.e("BME", "Error loading visit doc", it)
            }
    }

    private fun launchPlacesAutocomplete() {
        try {
            val fields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS
            )
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(requireContext())
            placesAutocomplete.launch(intent)
        } catch (e: Exception) {
            Log.e("BME", "Failed to launch autocomplete: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
