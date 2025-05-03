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
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog

class Additional7 : Fragment() {

    private lateinit var _binding: FragmentAdditional7Binding
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
        _binding = FragmentAdditional7Binding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtNextAdd7.setBackgroundColor(Color.TRANSPARENT)
        binding.txtPreviousAdd7.setBackgroundColor(Color.TRANSPARENT)
        binding.txtSkipAdd7.setBackgroundColor(Color.TRANSPARENT)

        //write code to retrieve the data from form



            binding.txtNextAdd7.setOnClickListener {
                //findNavController().navigate(R.id.action_additional7_to_additional8)
                sharedVisitViewModel.visitLog.helpTime = binding.edtAddtimenew.text.toString()
            }

            binding.txtPreviousAdd7.setOnClickListener {
                //findNavController().navigate(R.id.action_additional7_to_additional6)
            }
            binding.txtSkipAdd7.setOnClickListener {
                //findNavController().navigate(R.id.action_additional7_to_additional8)
            }
        }
    }
