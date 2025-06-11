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
        // Set checkbox listeners to update boolean fields in ViewModel
        binding.CB1.setOnClickListener {
            sharedVisitViewModel.visitLog.food_drink = binding.CB1.isChecked
        }
        binding.CB2.setOnClickListener {
            sharedVisitViewModel.visitLog.clothes = binding.CB2.isChecked
        }
        binding.CB3.setOnClickListener {
            sharedVisitViewModel.visitLog.hygiene = binding.CB3.isChecked
        }
        binding.CB4.setOnClickListener {
            sharedVisitViewModel.visitLog.wellness = binding.CB4.isChecked
        }
        binding.CB5.setOnClickListener {
            val isChecked = binding.CB5.isChecked
            sharedVisitViewModel.visitLog.other = isChecked
            binding.edtOther.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        binding.CB6.setOnClickListener {
            sharedVisitViewModel.visitLog.medicalhelp = binding.CB6.isChecked
        }
        binding.CB7.setOnClickListener {
            sharedVisitViewModel.visitLog.socialWorker = binding.CB7.isChecked
        }
        binding.CB8.setOnClickListener {
            sharedVisitViewModel.visitLog.lawyerLegal = binding.CB8.isChecked
        }



        binding.txtNext4.setOnClickListener {
            sharedVisitViewModel.visitLog.whattogive.clear()

            with(sharedVisitViewModel.visitLog) {
                if (food_drink) whattogive.add("Food and Drink")
                if (clothes) whattogive.add("Clothes")
                if (hygiene) whattogive.add("Hygiene Products")
                if (wellness) whattogive.add("Wellness/ Emotional Support")
                if (medicalhelp) whattogive.add("Medical Help")
                if (socialWorker) whattogive.add("Social Worker/ Psychiatrist")
                if (lawyerLegal) whattogive.add("Legal/ Lawyer")
                if (other) {
                    otherDetail = binding.edtOther.text.toString()
                    if (otherDetail.isNotBlank()) {
                        whattogive.add(otherDetail)
                    }
                }
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