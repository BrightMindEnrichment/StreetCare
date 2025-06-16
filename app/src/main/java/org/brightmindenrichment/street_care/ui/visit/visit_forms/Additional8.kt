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
            sharedVisitViewModel.visitLog.add_food_drink = binding.AD1.isChecked
        }
        binding.AD2.setOnClickListener {
            sharedVisitViewModel.visitLog.add_clothes = binding.AD2.isChecked
        }
        binding.AD3.setOnClickListener {
            sharedVisitViewModel.visitLog.add_hygine = binding.AD3.isChecked

        }
        binding.AD4.setOnClickListener {
            sharedVisitViewModel.visitLog.add_wellness = binding.AD4.isChecked

        }
        binding.AD5.setOnClickListener {
            val isChecked = binding.AD5.isChecked
            sharedVisitViewModel.visitLog.add_other = isChecked
            binding.edtOther.visibility = if (isChecked) View.VISIBLE else View.GONE

        }
        binding.AD6.setOnClickListener {
            sharedVisitViewModel.visitLog.add_medicalhelp = binding.AD6.isChecked
        }
        binding.AD7.setOnClickListener {
            sharedVisitViewModel.visitLog.add_socialWorker = binding.AD7.isChecked
        }
        binding.AD8.setOnClickListener {
            sharedVisitViewModel.visitLog.add_lawyerLegal = binding.AD8.isChecked

        }
            binding.txtNext3.setOnClickListener {

                sharedVisitViewModel.visitLog.whatrequired.clear()

                if(sharedVisitViewModel.visitLog.add_food_drink == true)
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Food and Drink")
                    //idx++
                }
                if(sharedVisitViewModel.visitLog.add_clothes == true)
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Clothes")

                }
                if(sharedVisitViewModel.visitLog.add_hygine == true)
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Hygine")

                }
                if(sharedVisitViewModel.visitLog.add_wellness == true)
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Wellness")

                }
                if(sharedVisitViewModel.visitLog.add_medicalhelp == true)
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Medical")

                }
                if(sharedVisitViewModel.visitLog.add_socialWorker == true)
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Social Worker")
                }
                if(sharedVisitViewModel.visitLog.add_lawyerLegal == true)
                {
                    sharedVisitViewModel.visitLog.whatrequired.add("Lawyer Legal")

                }
                if(sharedVisitViewModel.visitLog.add_other == true){
                    sharedVisitViewModel.visitLog.add_otherDetail = binding.edtOther.text.toString()
                    sharedVisitViewModel.visitLog.whatrequired.add(sharedVisitViewModel.visitLog.add_otherDetail)
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
