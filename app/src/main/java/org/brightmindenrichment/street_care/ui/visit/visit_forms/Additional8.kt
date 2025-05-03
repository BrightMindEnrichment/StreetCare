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
import org.brightmindenrichment.street_care.databinding.FragmentAdditional5Binding
import org.brightmindenrichment.street_care.databinding.FragmentAdditional6Binding
import org.brightmindenrichment.street_care.databinding.FragmentAdditional7Binding
import org.brightmindenrichment.street_care.databinding.FragmentAdditional8Binding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog

class Additional8 : Fragment() {

    private lateinit var _binding: FragmentAdditional8Binding
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
        _binding = FragmentAdditional8Binding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //write code to retrieve the data from form
        binding.AD1.setOnClickListener{
            if(binding.AD1.isChecked()) {
                sharedVisitViewModel.visitLog.add_food_drink = "Y"
            }
            else{
                sharedVisitViewModel.visitLog.add_food_drink = "N"
            }
        }
        binding.AD2.setOnClickListener {
            if(binding.AD2.isChecked()) {
                sharedVisitViewModel.visitLog.add_clothes = "Y"
            }
            else{
                sharedVisitViewModel.visitLog.add_clothes = "N"
            }
        }
        binding.AD3.setOnClickListener {
            if(binding.AD3.isChecked()) {
                sharedVisitViewModel.visitLog.add_hygine = "Y"
            }
            else{
                sharedVisitViewModel.visitLog.add_hygine = "N"
            }

        }
        binding.AD4.setOnClickListener {
            if(binding.AD4.isChecked()) {
                sharedVisitViewModel.visitLog.add_wellness = "Y"
            }
            else{
                sharedVisitViewModel.visitLog.add_wellness = "N"
            }

        }
        binding.AD5.setOnClickListener {
            if(binding.AD5.isChecked()){
                binding.edtADOther.setVisibility(View.VISIBLE)
                sharedVisitViewModel.visitLog.add_other ="Y"
            }
            else {
                binding.edtADOther.setVisibility(View.GONE)
                sharedVisitViewModel.visitLog.add_other ="N"

            }

        }
        binding.AD6.setOnClickListener {
            if(binding.AD6.isChecked()) {
                sharedVisitViewModel.visitLog.add_medicalhelp = "Y"
            }
            else{
                sharedVisitViewModel.visitLog.add_medicalhelp = "N"
            }

        }
        binding.AD7.setOnClickListener {
            if(binding.AD7.isChecked()) {
                sharedVisitViewModel.visitLog.add_socialWorker = "Y"
            }
            else{
                sharedVisitViewModel.visitLog.add_socialWorker = "N"
            }

        }
        binding.AD8.setOnClickListener {
            if(binding.AD8.isChecked()) {
                sharedVisitViewModel.visitLog.add_lawyerLegal = "Y"
            }
            else{
                sharedVisitViewModel.visitLog.add_lawyerLegal = "N"
            }

        }
            binding.txtNext3.setOnClickListener {

                sharedVisitViewModel.visitLog.whatrequired.clear()
                if(binding.AD5.isChecked()){
                    sharedVisitViewModel.visitLog.add_otherDetail = binding.edtADOther.text.toString()
                    sharedVisitViewModel.visitLog.whatrequired.add(sharedVisitViewModel.visitLog.add_otherDetail)
                }

                if(sharedVisitViewModel.visitLog.add_food_drink == "Y")
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Food and Drink")
                    //idx++
                }
                if(sharedVisitViewModel.visitLog.add_clothes == "Y")
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Clothes")

                }
                if(sharedVisitViewModel.visitLog.add_hygine == "Y")
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Hygine")

                }
                if(sharedVisitViewModel.visitLog.add_wellness == "Y")
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Wellness")

                }
                if(sharedVisitViewModel.visitLog.add_medicalhelp == "Y")
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Medical")

                }
                if(sharedVisitViewModel.visitLog.add_socialWorker == "Y")
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Social Worker")
                }
                if(sharedVisitViewModel.visitLog.add_lawyerLegal == "Y")
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Lawyer Legal")

                }
                findNavController().navigate(R.id.action_additional8_to_additional9)

            }

            binding.txtPrevious3.setOnClickListener {
                findNavController().navigate(R.id.action_additional8_to_additional7)
            }
            binding.txtSkip6.setOnClickListener {
                findNavController().navigate(R.id.action_additional8_to_additional9)
            }
        }
    }
