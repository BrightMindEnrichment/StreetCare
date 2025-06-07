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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Interaction Log"


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
//            binding.numberOfPeopleHelped.text = visitLog.peopleCount.toString()
            binding.ratingBar.rating = visitLog.experience.toFloat()
//            binding.commentsContent.text = visitLog.comments

            binding.whereWasYourInteractionAnswer.text = visitLog.whereVisit ?: "Not specified"
            binding.howManyYouHelpedAnswer.text= visitLog.peopleCount.toString() ?: "Not specified"
            binding.howManyItemsDonatedAnswer.text= visitLog.number_of_items.toString()
            binding.howMuchTimeSpentAnswer.text= visitLog.helpTime.toString()
            binding.whoJoinedYouAnswer.text= visitLog.whoJoined.toString()
            binding.howManyNeedHelpAnswer.text= visitLog.stillNeedSupport.toString()
            binding.whatSupportNeededAnswer.text= visitLog.whatGivenFurther
            binding.nextPlannedDateAnswer.text= visitLog.followupDate.toString()
            binding.whatVolunteersShouldKnowAnswer.text= visitLog.futureNotes.toString()
            binding.wouldYouVolunteerAnswer.text= visitLog.visitAgain.toString()
            binding.whatSupportYouProvidedAnswer.text= visitLog.whatGiven

        }

//        viewModel.helpType.observe(viewLifecycleOwner) { helpType ->
//            binding.whatSupportYouProvidedAnswer.text = helpType
//            setupClickListeners()
//        }


        viewModel.formattedDateTime.observe(viewLifecycleOwner) { formattedDateTime ->
            binding.whenWasYourInteractionAnswer.text = formattedDateTime
        }


        viewModel.formattedDate.observe(viewLifecycleOwner) { formattedDate ->
            binding.visitLogDateTV.text = formattedDate
        }

//        viewModel.helpType.observe(viewLifecycleOwner) { helpType ->
//            binding.typeOfHelpGiven.text = helpType
//        }



        viewModel.deleteResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Entry deleted successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Failed to delete entry. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showShareConfirmationPopup(visitLog: VisitLog) {
        AlertDialog.Builder(requireContext())
            .setTitle("Share Log")
            .setMessage("Are you sure you want to share this interaction publicly?")
            .setPositiveButton("Yes") { dialog, _ ->
                shareVisitLogToWeb(visitLog)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun shareVisitLogToWeb(visitLog: VisitLog) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        if (user != null) {
            val userDocRef = db.collection("users").document(user.uid)

            userDocRef.get().addOnSuccessListener { document ->
                val userType = document.getString("Type") ?: ""
                val status = if (userType == "Chapter Leader" || userType == "Street Care Hub Leader") "approved" else "pending"

                val visit = visitLog.whereVisit ?: ""
                val parts = visit.split(",").map { it.trim() }

                val street = parts.getOrNull(0) ?: ""
                val city = parts.getOrNull(1) ?: ""
                val state = parts.getOrNull(2) ?: ""
                val zipcode = parts.getOrNull(3) ?: ""


                val visitData = hashMapOf(
                    "uid" to user.uid,
                    "dateTime" to visitLog.date,
                    "state" to state,
                    "city" to city,
                    "stateAbbv" to state,
                    "street" to street,
                    "zipcode" to zipcode,
                    "numberPeopleHelped" to visitLog.peopleCount,
                    "whatGiven" to visitLog.whattogive,
                    "public" to "true",
                    "status" to status,
                    "itemQty" to visitLog.number_of_items,
                    "rating" to visitLog.experience,
                    "Description" to visitLog.peopleHelpedDescription,
                    "flaggedByUser" to visitLog.flaggedByUser,
                    "isFlagged" to visitLog.isFlagged
                )

                db.collection("visitLogWebProd")
                    .add(visitData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Visit log shared successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to share. Try again.", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "User type check failed.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupClickListeners() {

        binding.shareBtn.setOnClickListener {
            viewModel.visitLog.value?.let { visitLog ->
                showShareConfirmationPopup(visitLog)
            }
        }


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