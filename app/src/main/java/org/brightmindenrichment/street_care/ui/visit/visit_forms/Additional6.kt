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
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog

class Additional6 : Fragment() {

    private lateinit var _binding: FragmentAdditional6Binding
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
        _binding = FragmentAdditional6Binding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtNextAdd6.setBackgroundColor(Color.TRANSPARENT)
        binding.txtPreviousAdd6.setBackgroundColor(Color.TRANSPARENT)
        binding.txtSkipAdd6.setBackgroundColor(Color.TRANSPARENT)

        //write code to retrieve the data from form
        //val address = binding.edtLandmark.text.toString()
//        sharedVisitViewModel.visitLog.address = binding.edtLandmark.text.toString()


            binding.txtNextAdd6.setOnClickListener {
                findNavController().navigate(R.id.action_additional6_to_additional7)
                sharedVisitViewModel.visitLog.address = binding.edtLandmark.text.toString()
            }

            binding.txtPreviousAdd6.setOnClickListener {
                findNavController().navigate(R.id.action_additional6_to_additional5)
            }
            binding.txtSkipAdd6.setOnClickListener {
                findNavController().navigate(R.id.action_additional6_to_additional7)
            }
        }

    override fun onResume() {
        super.onResume()
        //binding.edtLandmark.text = sharedVisitViewModel.visitLog.address.toString()
    }
    }
