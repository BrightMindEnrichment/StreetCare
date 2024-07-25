package org.brightmindenrichment.street_care.ui.visit.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.brightmindenrichment.street_care.databinding.FragmentVisitLogDetailsBinding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import org.brightmindenrichment.street_care.ui.visit.AlertDialogFragment // Import your AlertDialogFragment
import java.text.SimpleDateFormat

class VisitLogDetailsFragment : Fragment() {

    lateinit var binding: FragmentVisitLogDetailsBinding

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
            showAlertDialog()
        }

        return binding.root
    }

    private fun showAlertDialog() {
        val dialogFragment = AlertDialogFragment()
        dialogFragment.show(parentFragmentManager, "alertDialog")
    }

    private fun getHelpType(visitLog: VisitLog): String? {
        return when {
            visitLog.food_drink == "Y" -> "food_drink"
            visitLog.clothes == "Y" -> "clothes"
            visitLog.hygine == "Y" -> "hygine"
            visitLog.wellness == "Y" -> "wellness"
            visitLog.other == "Y" -> "other"
            else -> null
        }
    }
}
