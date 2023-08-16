package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm5Binding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import org.brightmindenrichment.street_care.util.Extensions

class VisitFormFragment5 : Fragment() {
    private lateinit var _binding: FragmentVisitForm5Binding
    private val binding get() = _binding!!
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVisitForm5Binding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSatisfied.setOnClickListener {
            sharedVisitViewModel.visitLog.experience = getString(R.string.satisfied)
            binding.btnSatisfied.setBackgroundColor(Color.YELLOW)
            binding.btnNeutral.setBackgroundColor(Color.WHITE)
            binding.btnDissatisfied.setBackgroundColor(Color.WHITE)
        }
        binding.btnNeutral.setOnClickListener {
            sharedVisitViewModel.visitLog.experience = getString(R.string.neutral)
            binding.btnNeutral.setBackgroundColor(Color.YELLOW)
            binding.btnSatisfied.setBackgroundColor(Color.WHITE)
            binding.btnDissatisfied.setBackgroundColor(Color.WHITE)
        }
        binding.btnDissatisfied.setOnClickListener {
            sharedVisitViewModel.visitLog.experience = getString(R.string.dissatisfied)
            binding.btnDissatisfied.setBackgroundColor(Color.YELLOW)
            binding.btnNeutral.setBackgroundColor(Color.WHITE)
            binding.btnSatisfied.setBackgroundColor(Color.WHITE)
        }

        binding.txtNext5.setOnClickListener {

            if (Firebase.auth.currentUser == null) {
                Extensions.showDialog(
                    requireContext(),
                    "Anonymous",
                    "Logging a visit without logging in may \n result in you, being unable to view your \n visit history.",
                    "Ok",
                    "Cancel"
                )
            } else {
                showDialog(
                    requireContext(),
                    "Additional Info",
                    "Would You like to answer additional questions?",
                    "Yes", "No"
                )

            }
        }
        binding.txtPrevious5.setOnClickListener {

            findNavController().navigate(R.id.action_visitFormFragment5_to_visitFormFragment4)
        }

    }

    fun  showDialog(context : Context, title: String, message : String, textPositive : String, textNegative: String){
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton(textPositive, DialogInterface.OnClickListener { dialog, _ ->
                sharedVisitViewModel.visitLog.comments = binding.edtcomment.text.toString()
                findNavController().navigate(R.id.action_visitFormFragment5_to_visitFormFragment_additional)
                dialog.dismiss()
            })
        builder.setNegativeButton(textNegative, DialogInterface.OnClickListener { dialog, _ ->
            sharedVisitViewModel.visitLog.comments = binding.edtcomment.text.toString()
            sharedVisitViewModel.saveVisitLog()
            Toast.makeText(context, "Log saved successfully ", Toast.LENGTH_SHORT).show()
            sharedVisitViewModel.visitLog = VisitLog()

            binding.txtProgress.text= "Completed"
            findNavController().navigate(R.id.surveySubmittedFragment)
            dialog.cancel()
        })
        val alert = builder.create()
        alert.show()
    }
}