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
import org.brightmindenrichment.street_care.databinding.FragmentAdditional10Binding
import org.brightmindenrichment.street_care.databinding.FragmentAdditional9Binding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog

class Additional10 : Fragment() {

    private lateinit var _binding: FragmentAdditional10Binding
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
        _binding = FragmentAdditional10Binding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtNextAdd10.setBackgroundColor(Color.TRANSPARENT)
        binding.txtPreviousAdd10.setBackgroundColor(Color.TRANSPARENT)
        binding.txtSkipAdd10.setBackgroundColor(Color.TRANSPARENT)

        //write code to retrieve the data from form


            binding.txtNextAdd10.setOnClickListener {
                findNavController().navigate(R.id.action_additional10_to_additional3)
                sharedVisitViewModel.visitLog.add_volunteerDetail = binding.edtAddDetail.text.toString()
//                sharedVisitViewModel.saveVisitLog()
//                Toast.makeText(context, "Log saved successfully ", Toast.LENGTH_SHORT).show()
//                sharedVisitViewModel.visitLog = VisitLog()
            }

            binding.txtPreviousAdd10.setOnClickListener {
                findNavController().navigate(R.id.action_additional10_to_additional9)
            }
            binding.txtSkipAdd10.setOnClickListener {
                findNavController().navigate(R.id.action_additional10_to_additional3)
            }
        }
    }
