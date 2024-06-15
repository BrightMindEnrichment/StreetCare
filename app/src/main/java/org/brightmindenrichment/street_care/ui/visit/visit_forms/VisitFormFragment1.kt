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

class VisitFormFragment1 : Fragment() {
    private var _binding: FragmentVisitForm1Binding? = null
    private val binding get() = _binding!!
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()

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
            searchLocation()
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

    // autocomplete places API Using Fragment
    private fun searchLocation() {
        // Initialize the SDK
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.API_KEY_PLACES)
        }
        // Create a new PlacesClient instance
        val autocompleteFragment: AutocompleteSupportFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setActivityMode(AutocompleteActivityMode.OVERLAY)
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // setting the place selected by user into our object
                sharedVisitViewModel.visitLog.location = place.name
                sharedVisitViewModel.visitLog.locationmap["street"] = sharedVisitViewModel.visitLog.location
                Log.d("BME", "Place: ${place.name}, ${place.id}")
            }
            override fun onError(status: Status) {
                Log.w("BME", "An error occurred: $status")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}


