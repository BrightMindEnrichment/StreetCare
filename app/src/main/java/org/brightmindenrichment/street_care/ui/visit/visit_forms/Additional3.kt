package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.graphics.Color
import android.os.Bundle
import android.provider.CalendarContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentAdditional2Binding
import org.brightmindenrichment.street_care.databinding.FragmentAdditional3Binding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog

class Additional3 : Fragment() {

    private lateinit var _binding: FragmentAdditional3Binding
    private val binding get() = _binding!!
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAdditional3Binding.inflate(inflater, container, false)
        return _binding.root
    }

    private fun resetButtonStyles() {
        binding.btnYes.setStrokeColorResource(R.color.dark_green)
        binding.btnNo.setStrokeColorResource(R.color.dark_green)
        binding.btnMaybe.setStrokeColorResource(R.color.dark_green)

        binding.btnYes.setBackgroundColor(Color.TRANSPARENT)
        binding.btnNo.setBackgroundColor(Color.TRANSPARENT)
        binding.btnMaybe.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnYes.setOnClickListener {
            binding.btnYes.setBackgroundColor(Color.LTGRAY)
            binding.btnNo.setBackgroundColor(Color.WHITE)
            binding.btnMaybe.setBackgroundColor(Color.WHITE)
            sharedVisitViewModel.visitLog.visitAgain = "Yes"
        }

        binding.btnNo.setOnClickListener {
            sharedVisitViewModel.visitLog.visitAgain = "No"
            binding.btnNo.setBackgroundColor(Color.LTGRAY)
            binding.btnYes.setBackgroundColor(Color.WHITE)
            binding.btnMaybe.setBackgroundColor(Color.WHITE)
        }
        binding.btnMaybe.setOnClickListener {
            binding.btnMaybe.setBackgroundColor(Color.LTGRAY)
            binding.btnNo.setBackgroundColor(Color.WHITE)
            binding.btnYes.setBackgroundColor(Color.WHITE)
            sharedVisitViewModel.visitLog.visitAgain = "MayBe"
        }
        binding.txtFinish.setOnClickListener {
            findNavController().navigate(R.id.action_additional3_to_surveySubmittedFragment)
            sharedVisitViewModel.saveVisitLog()
            Toast.makeText(context, getString(R.string.log_saved_successfully), Toast.LENGTH_SHORT).show()
            //sharedVisitViewModel.visitLog = VisitLog()
            sharedVisitViewModel.resetVisitLogPage(forceReset = false)
        }


        binding.txtBack.setOnClickListener {
            findNavController().navigate(R.id.action_additional3_to_additional10)
        }

        }
    }
