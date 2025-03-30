package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.findFragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import org.brightmindenrichment.street_care.BuildConfig
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm1Binding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import org.brightmindenrichment.street_care.util.Extensions
import java.util.*
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher

class VisitFormFragment1 : Fragment() {
    private var _binding: FragmentVisitForm1Binding? = null
    private val binding get() = _binding!!
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()

    // Define the ActivityResultLauncher
    private lateinit var placesAutocomplete: ActivityResultLauncher<android.content.Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for activity result in onCreate
        placesAutocomplete = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                android.app.Activity.RESULT_OK -> {
                    result.data?.let {
                        val place = Autocomplete.getPlaceFromIntent(it)
                        val fullAddress = place.address ?: place.name
                        val completeStreetAddress = place.address?.split(',')?.firstOrNull()?.trim() ?: ""

                        // Set the full address to the custom field
                        binding.customAddressField.setText(completeStreetAddress)

                        // Save the location to viewModel
                        sharedVisitViewModel.visitLog.location = fullAddress.toString()
                        sharedVisitViewModel.visitLog.locationmap["street"] = fullAddress.toString()

                        // Extract address components
                        var city: String? = null
                        var state: String? = null
                        var zipCode: String? = null

                        val addressComponents = place.addressComponents
                        if (addressComponents != null) {
                            for (component in addressComponents.asList()) {
                                val types = component.types
                                when {
                                    types.contains("locality") -> {
                                        city = component.name
                                    }
                                    types.contains("administrative_area_level_1") -> {
                                        state = component.name
                                    }
                                    types.contains("postal_code") -> {
                                        zipCode = component.name
                                    }
                                }
                            }
                        }

                        // Set the extracted values to fields
                        binding.edtCity2.setText(city)
                        binding.edtState3.setText(state)
                        binding.edtZipcode5.setText(zipCode)

                        Log.d("BME", "Selected address in visit log page: $fullAddress")
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    result.data?.let {
                        val status = Autocomplete.getStatusFromIntent(it)
                        Log.e("BME", "Error in Autocomplete in visit log page: ${status.statusMessage}")
                        //Toast.makeText(context, "Error finding address: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
                android.app.Activity.RESULT_CANCELED -> {
                    // User canceled the operation
                    Log.d("BME", "User canceled the autocomplete operation")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVisitForm1Binding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewStateRestored(savedInstanceState)
        if (Firebase.auth.currentUser == null) {
            Extensions.showDialog(
                requireContext(),
                view.context.getString(R.string.anonymous_user_title),
                view.context.getString(R.string.anonymous_user_message),
                view.context.getString(R.string.ok),
                view.context.getString(R.string.cancel))
        }

        // Initialize Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.API_KEY_PLACES)
        }

        // Set up click listener for the custom address field
        binding.customAddressField.setOnClickListener {
            launchPlacesAutocomplete()
        }

            binding.txtGoToPage2.setOnClickListener {
                //sharedVisitViewModel.saveVisitLog()
                //  sharedVisitViewModel.visitLog = VisitLog()
                findNavController().navigate(R.id.action_visitFormFragment1_to_visitFormFragment2)
                sharedVisitViewModel.visitLog.locationmap["city"] = binding.edtCity2.text.toString()
                sharedVisitViewModel.visitLog.locationmap["state"] = binding.edtState3.text.toString()
                sharedVisitViewModel.visitLog.locationmap["zipcode"] = binding.edtZipcode5.text.toString()

        }


        binding.txtBack.setOnClickListener {

            findNavController().navigate(R.id.action_visitFormFragment1_to_nav_visit)
        }
        binding.txtSkip.setOnClickListener {

            findNavController().navigate(R.id.action_visitFormFragment1_to_visitFormFragment2)
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

            // Start the autocomplete intent
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(requireContext())

            // Launch using the ActivityResultLauncher
            placesAutocomplete.launch(intent)
        } catch (e: Exception) {
            Log.e("BME", "Error launching Places Autocomplete in visit log page: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}