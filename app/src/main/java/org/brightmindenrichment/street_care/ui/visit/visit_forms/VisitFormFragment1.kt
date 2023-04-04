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
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm1Binding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import org.brightmindenrichment.street_care.util.Extensions
import java.util.*


/*
 * A simple [Fragment] subclass.
 * Use the [VisitFormFragment1.newInstance] factory method to
 * create an instance of this fragment.
 */
class VisitFormFragment1 : Fragment() {
    private var _binding: FragmentVisitForm1Binding? = null
    private val binding get() = _binding!!
    private val myCalendar: Calendar = Calendar.getInstance()
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()
    private var displayDateFormat: String = "MM/dd/yyyy"

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

        searchLocation()

        binding.datePickerActions.setOnClickListener{
          myCalendar.time =  populateCalendarToSelectVisitDate()
        }
        binding.btnSubmitHere.setOnClickListener {
            if (!sharedVisitViewModel.validateDate(sharedVisitViewModel.visitLog.date)) {
                Extensions.showDialog(requireContext(), "Alert","Please fill your past visit date", "Ok")
            } else if(Firebase.auth.currentUser ==null){

                    Extensions.showDialog(requireContext(), "Anonymous","Logging a visit without logging in may \n result in you, being unable to view your \n visit history.", "Ok")
                }else{
                sharedVisitViewModel.saveVisitLog()
                Toast.makeText(context, "Log saved successfully ", Toast.LENGTH_SHORT).show()
                sharedVisitViewModel.visitLog = VisitLog()
                findNavController().navigate(R.id.action_visitFormFragment1_to_nav_visit)
            }
        }

        binding.btnGoToPage2.setOnClickListener {

                if (!sharedVisitViewModel.validateDate(sharedVisitViewModel.visitLog.date)) {
                Extensions.showDialog(requireContext(), "Alert","Please fill your past visit date", "Ok")
            } else {
                findNavController().navigate(R.id.action_visitFormFragment1_to_visitFormFragment2)
            }

        }

    }


    // autocomplete places API Using Fragment
    private fun searchLocation(){
        val apiKey = getString(R.string.api_key)


        // Initialize the SDK
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey)

        }
        // Create a new PlacesClient instance
        //val placesClient = Places.createClient(requireActivity().applicationContext)

        val autocompleteFragment : AutocompleteSupportFragment = childFragmentManager
            .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setActivityMode(AutocompleteActivityMode.OVERLAY)
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {

            override fun onPlaceSelected(place: Place) {
                // setting the place selected by user into our object
                sharedVisitViewModel.visitLog.location = place.name
                Log.d("BME", "Place: ${place.name}, ${place.id}")
            }

            override fun onError(status: Status) {
                Log.w("BME", "An error occurred: $status")
            }
        })
    }


    private fun populateCalendarToSelectVisitDate() : Date{
        val date =
            OnDateSetListener { _, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                displayDate(Extensions.dateToString(myCalendar.time, displayDateFormat))
                //setting the user selected date into object
                sharedVisitViewModel.visitLog.date = myCalendar.time

            }

        context?.let { it1 ->
            DatePickerDialog(
                it1,
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)


            ).show()

        }
        return myCalendar.time

    }


    private fun displayDate(dateString: String) {
        binding.datePickerActions.setText(dateString)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}


