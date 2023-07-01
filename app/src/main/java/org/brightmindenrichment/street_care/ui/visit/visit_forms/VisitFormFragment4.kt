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
            sharedVisitViewModel.visitLog.food_drink = "Y"
        }
        binding.CB2.setOnClickListener {
            sharedVisitViewModel.visitLog.clothes = "Y"

        }
        binding.CB3.setOnClickListener {
            sharedVisitViewModel.visitLog.hygine = "Y"

        }
        binding.CB4.setOnClickListener {
            sharedVisitViewModel.visitLog.wellness = "Y"

        }
        binding.CB5.setOnClickListener {
            sharedVisitViewModel.visitLog.other = "Y"
                if(binding.CB5.isChecked()){
                    binding.edtOther.setVisibility(View.VISIBLE);
                    sharedVisitViewModel.visitLog.otherDetail = binding.edtOther.text.toString()
                }
                else
                    binding.edtOther.setVisibility(View.INVISIBLE);

        }
        binding.txtNext4.setOnClickListener {
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