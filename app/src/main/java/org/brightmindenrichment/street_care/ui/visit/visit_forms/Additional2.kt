package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentAdditional2Binding

class Additional2 : Fragment() {

    private lateinit var _binding: FragmentAdditional2Binding
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
        _binding = FragmentAdditional2Binding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupDropdowns()
        binding.txtNextAdd2.setOnClickListener {
            val selectedHours = binding.selectHour.text.toString()
            val selectedMinutes = binding.selectMinute.text.toString()
            findNavController().navigate(R.id.action_additional2_to_additional5)
        }
        binding.txtPreviousAdd2.setOnClickListener {
            findNavController().navigate(R.id.action_additional2_to_additional1)
        }
        binding.txtSkipAdd2.setOnClickListener {
            findNavController().navigate(R.id.action_additional2_to_additional5)
        }
    }
    private fun setupDropdowns() {
        val hoursList = (0..12).map { it.toString() }
        val minutesList = listOf("0", "15", "30", "45")

        val hoursAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, hoursList)
        val minutesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, minutesList)

        val hourView = binding.selectHour
        val minuteView = binding.selectMinute

        hourView.setAdapter(hoursAdapter)
        minuteView.setAdapter(minutesAdapter)

        hourView.setOnClickListener {
            hourView.showDropDown()
        }
        minuteView.setOnClickListener {
            minuteView.showDropDown()
        }
        hourView.setOnItemClickListener { _, _, position, _ ->
            val selected = hoursList[position]
            val formatted = "$selected hour${if (selected == "1") "" else "s"}"
            hourView.setText(formatted, false)
        }

        minuteView.setOnItemClickListener { _, _, position, _ ->
            val selected = minutesList[position]
            val formatted = "$selected minute${if (selected == "1") "" else "s"}"
            minuteView.setText(formatted, false)
        }

    }
    override fun onResume() {
        super.onResume()

    }
  }
