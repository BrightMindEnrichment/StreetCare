package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentAdditional5Binding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog

class Additional5 : Fragment() {

    private lateinit var _binding: FragmentAdditional5Binding
    private val binding get() = _binding!!
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()

    private var numberOfPeople: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // No arguments handled here for now
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdditional5Binding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        setupCounter()
        updateProgressBar()
    }

    private fun setupButtons() {
        binding.txtNext3.setOnClickListener {
            saveFormData();
            findNavController().navigate(R.id.action_additional5_to_additional6)
        }

        binding.txtPrevious3.setOnClickListener {
            findNavController().navigate(R.id.action_additional5_to_additional2)
        }

        binding.txtSkip3.setOnClickListener {
            findNavController().navigate(R.id.action_additional5_to_additional6)
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

        binding.etNoOfPeople.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().toIntOrNull() ?: 0
                sharedVisitViewModel.visitLog.whoJoined = input
            }
        })
    }

    private fun updatePeopleCount() {
        binding.etNoOfPeople.setText(numberOfPeople.toString())
        // Save this value to ViewModel if needed:
        sharedVisitViewModel.visitLog.whoJoined = numberOfPeople
    }

    private fun saveFormData() {
        // Save the description entered by user
        sharedVisitViewModel.visitLog.numberOfHelpersComment = binding.descriptionExample.text.toString()

    }

    private fun updateProgressBar() {
        binding.simpleProgressBar.progress = 100
        binding.simpleProgressBar2.progress = 100
        binding.simpleProgressBar3.progress = 0
        binding.simpleProgressBar4.progress = 0
        binding.simpleProgressBar5.progress = 0
        binding.simpleProgressBar6.progress = 0
        binding.simpleProgressBar7.progress = 0
    }
}
