package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentSurvaySubmittedBinding
import org.brightmindenrichment.street_care.util.Extensions


class SurveySubmittedFragment : Fragment() {
   private var _binding : FragmentSurvaySubmittedBinding? = null
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSurvaySubmittedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAnotherVisit.setOnClickListener{
            findNavController().navigate(R.id.action_surveySubmittedFragment_to_visitFormFragment1)
        }
        binding.btnReturnHome.setOnClickListener{
            findNavController().navigate(R.id.action_surveySubmittedFragment_to_nav_home)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}// end of class