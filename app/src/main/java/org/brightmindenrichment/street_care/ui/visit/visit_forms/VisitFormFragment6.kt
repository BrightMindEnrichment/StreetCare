package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm6Binding

class VisitFormFragment6 : Fragment() {
    private var _binding: FragmentVisitForm6Binding? = null
    private val binding get() = _binding!!
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVisitForm6Binding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // setting outreach options

        binding.increaseNoOfItems.setOnClickListener {
            val count = sharedVisitViewModel.increment(sharedVisitViewModel.visitLog.number_of_items.toInt())

            sharedVisitViewModel.visitLog.number_of_items = count.toLong()
            binding.editBox12.setText(sharedVisitViewModel.visitLog.number_of_items.toString())
        }
        binding.decreaseNoOfItems.setOnClickListener {
            val count = sharedVisitViewModel.decrement(sharedVisitViewModel.visitLog.number_of_items.toInt())

            sharedVisitViewModel.visitLog.number_of_items = count.toLong()
            binding.editBox12.setText(sharedVisitViewModel.visitLog.number_of_items.toString())
        }

        binding.txtNextAdd420.setOnClickListener {
            findNavController().navigate(R.id.action_visitFormFragment5_to_action_visitFormFragment6)
//            sharedVisitViewModel.visitLog.number_of_items = binding.editBox12.toLong()

            val count = binding.editBox12.text.toString()

            if (count.isNotEmpty() && count.isDigitsOnly()) {
                sharedVisitViewModel.visitLog.number_of_items = count.toLong()
            } else {
                sharedVisitViewModel.visitLog.number_of_items = 0L
            }

            val notes = binding.enterNotes.text.toString()
            sharedVisitViewModel.visitLog.notes = notes
        }
////            binding.tvNoOfPeople.text = sharedVisitViewModel.visitLog.peopleCount.toString()
//            sharedVisitViewModel.visitLog.number_of_items = count.toLong()
//        }

        binding.txtPreviousAdd420.setOnClickListener {

            findNavController().navigate(R.id.action_visitFormFragment5_to_action_visitFormFragment4)
        }
        binding.txtSkipAdd420.setOnClickListener {

            findNavController().navigate(R.id.action_visitFormFragment5_to_action_visitFormFragment6)
        }
    }


}