package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentAdditional1Binding
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm5Binding

class Additional1 : Fragment() {
    private lateinit var _binding: FragmentAdditional1Binding
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
        _binding = FragmentAdditional1Binding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.datePicker1.setIs24HourView(true)
        binding.txtNextAdd1.setOnClickListener {
            //  sharedVisitViewModel.visitLog.names = binding.edtNames.text.toString()
            findNavController().navigate(R.id.action_additional1_to_additional2)
        }
        binding.txtPreviousAdd1.setOnClickListener {
            findNavController().navigate(R.id.action_additional2_to_additional1)
        }
        binding.txtSkipAdd1.setOnClickListener {
            findNavController().navigate(R.id.action_additional1_to_additional2)
        }
    }
}