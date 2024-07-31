package org.brightmindenrichment.street_care.ui.visit.details

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.brightmindenrichment.street_care.databinding.FragmentVisitLogDetailsBinding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import java.io.IOException
import java.text.SimpleDateFormat

class VisitLogDetailsFragment : Fragment() {

    lateinit var binding: FragmentVisitLogDetailsBinding
    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVisitLogDetailsBinding.inflate(inflater)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Visit Log"

        val visitLog = arguments?.getParcelable<VisitLog>("visitLog")
        val sdf = SimpleDateFormat("dd MMM yyyy")
        binding.visitLogDateTV.text = visitLog?.date?.let { sdf.format(it) }
        binding.visitLogAddressTV.text = visitLog?.location.toString()
        binding.numberOfPeopleHelped.text = visitLog?.peopleHelped.toString()
        binding.typeOfHelpGiven.text = visitLog?.let { getHelpType(it) }
        binding.ratingBar.rating = visitLog?.experience?.toFloat() ?: 0f
        binding.commentsContent.text = visitLog?.comments ?: ""

        // Set click listener for delete button
        binding.removeBtn.setOnClickListener {
            if (visitLog != null) {
                showAlertDialog(visitLog)
            }
        }
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            googleMap = it

            val address = visitLog?.location

            val geocoder = Geocoder(requireContext())
            try {
                val addresses = address?.let { it1 -> geocoder.getFromLocationName(it1, 1) }
                if (addresses?.isNotEmpty() == true) {
                    val location = LatLng(addresses[0].latitude, addresses[0].longitude)

                    googleMap?.addMarker(MarkerOptions().position(location).title("Visit Location"))
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


        return binding.root
    }

    private fun showAlertDialog(visitLog: VisitLog) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Delete Visit Log")
        builder.setMessage("This will permanently delete your Visit Log. This can’t be undone.")

        builder.setPositiveButton("Confirm") { dialog, which ->
            deleteVisit(visitLog.id)
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun deleteVisit(entryId: String) {
        val db = Firebase.firestore
        db.collection("interimPersonalVisitLog").document(entryId).delete()
            .addOnSuccessListener {
                parentFragmentManager.popBackStack()
                Toast.makeText(requireContext(), "Entry deleted successfully", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to delete entry. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getHelpType(visitLog: VisitLog): String? {
        var helpType = ""

        if (visitLog.food_drink == "Y") helpType += "food/drink, "
        if (visitLog.clothes == "Y") helpType += "clothes, "
        if (visitLog.hygine == "Y") helpType += "hygine, "
        if (visitLog.wellness == "Y") helpType += "wellness, "
        if (visitLog.lawyerLegal == "Y") helpType += "lawyer/legal, "
        if (visitLog.medicalhelp == "Y") helpType += "medical, "
        if (visitLog.socialWorker == "Y") helpType += "social, "
        if (visitLog.other == "Y") helpType += "other, "

        // Remove the trailing comma if the string is not empty
        return if (helpType.isNotBlank()) helpType.removeSuffix(", ") else null
    }

}
