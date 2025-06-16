package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm3Binding
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm4Binding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog

class VisitFormFragment4 : Fragment() {
    private var _binding: FragmentVisitForm4Binding? = null
    private val binding get() = _binding!!
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVisitForm4Binding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // setting outreach options
        binding.CB1.setOnClickListener {
            if(binding.CB1.isChecked()) {
                sharedVisitViewModel.visitLog.food_drink = "Y"
            }
            else
            {
                sharedVisitViewModel.visitLog.food_drink = "N"
            }
        }
        binding.CB2.setOnClickListener {
            if(binding.CB2.isChecked()) {
                sharedVisitViewModel.visitLog.clothes = "Y"
            }
            else{
                sharedVisitViewModel.visitLog.clothes = "N"
            }
        }
        binding.CB3.setOnClickListener {
            if(binding.CB3.isChecked()) {
                sharedVisitViewModel.visitLog.hygiene = "Y"
            }
            else{
                sharedVisitViewModel.visitLog.hygiene = "N"
            }

        }
        binding.CB4.setOnClickListener {
            if(binding.CB4.isChecked()) {
                sharedVisitViewModel.visitLog.wellness = "Y"
            }
            else{
                sharedVisitViewModel.visitLog.wellness = "N"
            }

        }
        binding.CB5.setOnClickListener {

                if(binding.CB5.isChecked()){
                    binding.edtOther.setVisibility(View.VISIBLE)
                    sharedVisitViewModel.visitLog.other = "Y"

                }
                else{
                    binding.edtOther.setVisibility(View.GONE)
                }

        }

        binding.CB6.setOnClickListener {
            if(binding.CB6.isChecked()) {
                sharedVisitViewModel.visitLog.medicalhelp = "Y"
            }
            else{
                sharedVisitViewModel.visitLog.medicalhelp = "N"
            }

        }
        binding.CB7.setOnClickListener {
            if(binding.CB7.isChecked()) {
                sharedVisitViewModel.visitLog.socialWorker = "Y"
            }
            else{
                sharedVisitViewModel.visitLog.socialWorker = "N"
            }

        }
        binding.CB8.setOnClickListener {
            if(binding.CB8.isChecked()) {
                sharedVisitViewModel.visitLog.lawyerLegal = "Y"
            }
            else{
                sharedVisitViewModel.visitLog.lawyerLegal = "N"
            }

        }


        binding.txtNext4.setOnClickListener {
            sharedVisitViewModel.visitLog.whattogive.clear()
            if(sharedVisitViewModel.visitLog.food_drink == "Y")
            {
                sharedVisitViewModel.visitLog.whattogive.add("Food and Drink")
            }
            if(sharedVisitViewModel.visitLog.clothes == "Y")
            {
                sharedVisitViewModel.visitLog.whattogive.add("Clothes")

            }
            if(sharedVisitViewModel.visitLog.hygiene == "Y")
            {
                sharedVisitViewModel.visitLog.whattogive.add("Hygiene Products")

            }
            if(sharedVisitViewModel.visitLog.wellness == "Y")
            {
                sharedVisitViewModel.visitLog.whattogive.add("Wellness/ Emotional Support")

            }
            if(sharedVisitViewModel.visitLog.medicalhelp == "Y")
            {
                sharedVisitViewModel.visitLog.whattogive.add("Medical Help")

            }
            if(sharedVisitViewModel.visitLog.socialWorker == "Y")
            {
                sharedVisitViewModel.visitLog.whattogive.add("Social Worker/ Psychiatrist")
            }
            if(sharedVisitViewModel.visitLog.lawyerLegal == "Y")
            {
                sharedVisitViewModel.visitLog.whattogive.add("Legal/ Lawyer")

            }
            if(sharedVisitViewModel.visitLog.other == "Y")
            {
                sharedVisitViewModel.visitLog.otherDetail = binding.edtOther.text.toString()
                sharedVisitViewModel.visitLog.whattogive.add(sharedVisitViewModel.visitLog.otherDetail)
            }

            findNavController().navigate(R.id.action_visitFormFragment4_to_action_visitFormFragment5)
        }
        binding.txtPrevious4.setOnClickListener {

            findNavController().navigate(R.id.action_visitFormFragment4_to_action_visitFormFragment3)
        }
        binding.txtSkip4.setOnClickListener {

            findNavController().navigate(R.id.action_visitFormFragment4_to_action_visitFormFragment5)
        }
    }


}