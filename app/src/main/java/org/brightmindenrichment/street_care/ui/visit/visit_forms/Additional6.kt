package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentAdditional6Binding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog

class Additional6 : Fragment() {

    private lateinit var _binding: FragmentAdditional6Binding
    private val binding get() = _binding!!
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()

    private var numberOfPeople: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // No arguments handled currently
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdditional6Binding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtonClicks()
        setupCounter()
        updateProgressBars()
    }

    private fun setupButtonClicks() {
        binding.txtNext3.setOnClickListener {
            saveFormData()
            findNavController().navigate(R.id.action_additional6_to_additional8)
        }

        binding.txtPrevious3.setOnClickListener {
            findNavController().navigate(R.id.action_additional6_to_additional5)
        }

        binding.txtSkip3.setOnClickListener {
            findNavController().navigate(R.id.action_additional6_to_additional8)
        }
    }

    private fun setupCounter() {
        binding.increaseNoOfPeople.setOnClickListener {
            numberOfPeople++
            updatePeopleCount()
        }

        binding.decreaseNoOfPeople.setOnClickListener {
            if (numberOfPeople > 0) {
                numberOfPeople--
                updatePeopleCount()
            }
        }
    }

    private fun updatePeopleCount() {
        binding.etNoOfPeople.setText(numberOfPeople.toString())
        // Optionally: Save the number to ViewModel if needed
        sharedVisitViewModel.visitLog.stillNeedSupport = numberOfPeople

    }

    private fun saveFormData() {
        // Save the description entered by user
        sharedVisitViewModel.visitLog.supportTypeNeeded = binding.descriptionExample.text.toString()
        // Save the location description
        sharedVisitViewModel.visitLog.peopleNeedFurtherHelpLocation = binding.locationDescription.text.toString()

    }

    private fun updateProgressBars() {
        binding.simpleProgressBar.progress = 100
        binding.simpleProgressBar2.progress = 100
        binding.simpleProgressBar3.progress = 100
        binding.simpleProgressBar4.progress = 0
        binding.simpleProgressBar5.progress = 0
        binding.simpleProgressBar6.progress = 0
        binding.simpleProgressBar7.progress = 0
    }

}
