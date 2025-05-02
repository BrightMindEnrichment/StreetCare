package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R

/**
 * Fragment for Visit Form Question 7a with Yes/No options.
 * Represents a step in the visit log form flow.
 */

class VisitForm7a : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_visit_form7a, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the Yes button and set its click listener
        val yesButton = view.findViewById<View>(R.id.txt_yes)
        yesButton?.setOnClickListener {
            // Navigate to VisitFormFragment1
            findNavController().navigate(R.id.action_visitForm7a_to_additional1)
        }

        // Find the No button and set its click listener
        val noButton = view.findViewById<View>(R.id.txt_no)
        noButton?.setOnClickListener {
            // Navigate to SurveySubmittedFragment
            findNavController().navigate(R.id.action_visitForm7a_to_surveySubmittedFragment)
        }
    }
}