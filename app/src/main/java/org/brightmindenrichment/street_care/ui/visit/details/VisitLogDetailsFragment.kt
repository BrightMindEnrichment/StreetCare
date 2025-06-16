package org.brightmindenrichment.street_care.ui.visit.details

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentVisitLogDetailsBinding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import java.io.IOException

class VisitLogDetailsFragment : Fragment() {

    lateinit var binding: FragmentVisitLogDetailsBinding
    private var googleMap: GoogleMap? = null
    private val viewModel: VisitLogDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVisitLogDetailsBinding.inflate(inflater)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = context?.getString(R.string.menu_visit)


        try {
            val visitLog = requireArguments().getParcelable<VisitLog>("visitLog")!!
            viewModel.setVisitLog(visitLog)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_LONG).show()
            return binding.root
        }

        setupObservers()
        setupClickListeners()
        setupMap(savedInstanceState)

        return binding.root
    }

    private fun setupObservers() {
        viewModel.visitLog.observe(viewLifecycleOwner) { visitLog ->
            binding.visitLogAddressTV.text = visitLog.whereVisit
            binding.numberOfPeopleHelped.text = visitLog.peopleCount.toString()
            binding.ratingBar.rating = visitLog.experience.toFloat()
            binding.commentsContent.text = visitLog.comments
        }

        viewModel.formattedDate.observe(viewLifecycleOwner) { formattedDate ->
            binding.visitLogDateTV.text = formattedDate
        }

        viewModel.helpType.observe(viewLifecycleOwner) { helpType ->
            binding.typeOfHelpGiven.text = helpType
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Entry deleted successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Failed to delete entry. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.removeBtn.setOnClickListener {
            showAlertDialog()
        }
    }

    private fun setupMap(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { map ->
            googleMap = map
            viewModel.visitLog.value?.let { visitLog ->
                updateMapLocation(visitLog.whereVisit)
            }
        }
    }

    private fun updateMapLocation(address: String) {
        val geocoder = Geocoder(requireContext())
        try {
            val addresses = geocoder.getFromLocationName(address, 1)
            if (addresses?.isNotEmpty() == true) {
                val location = LatLng(addresses[0].latitude, addresses[0].longitude)
                googleMap?.addMarker(MarkerOptions().position(location).title("Visit Location"))
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(requireActivity()).apply {
            setTitle("Delete Visit Log")
            setMessage("This will permanently delete your Visit Log. This can't be undone.")
            setPositiveButton("Confirm") { _, _ -> viewModel.deleteVisitLog() }
            setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            create().show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        googleMap = null // Avoid memory leaks
    }
}