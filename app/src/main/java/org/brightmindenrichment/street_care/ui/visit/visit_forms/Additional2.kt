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
        binding.btnIncrease.setOnClickListener {
            val count = sharedVisitViewModel.increment(sharedVisitViewModel.visitLog.peopleCount)
            binding.txtPreview.text = sharedVisitViewModel.visitLog.peopleCount.toString()
            sharedVisitViewModel.visitLog.peopleCount = count
        }
        binding.btnDecrease.setOnClickListener {
            val count = sharedVisitViewModel.decrement(sharedVisitViewModel.visitLog.peopleCount)
            sharedVisitViewModel.visitLog.peopleCount = count
            binding.txtPreview.text = sharedVisitViewModel.visitLog.peopleCount.toString()
        }

        binding.txtNextAdd2.setOnClickListener {
            //  sharedVisitViewModel.visitLog.names = binding.edtNames.text.toString()
            findNavController().navigate(R.id.action_additional2_to_additional3)
        }
        binding.txtPreviousAdd2.setOnClickListener {
            findNavController().navigate(R.id.action_additional2_to_additional1)
        }
        binding.txtSkipAdd2.setOnClickListener {
            findNavController().navigate(R.id.action_additional2_to_additional3)
        }
    }
    override fun onResume() {
        super.onResume()
        binding.txtPreview.text = sharedVisitViewModel.visitLog.peopleCount.toString()
    }
  }
