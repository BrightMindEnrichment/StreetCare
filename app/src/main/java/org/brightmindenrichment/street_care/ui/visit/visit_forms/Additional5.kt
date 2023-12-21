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
import org.brightmindenrichment.street_care.databinding.FragmentAdditional4Binding
import org.brightmindenrichment.street_care.databinding.FragmentAdditional5Binding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog

class Additional5 : Fragment() {

    private lateinit var _binding: FragmentAdditional5Binding
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
        _binding = FragmentAdditional5Binding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtNextAdd5.setBackgroundColor(Color.TRANSPARENT)
        binding.txtPreviousAdd5.setBackgroundColor(Color.TRANSPARENT)
        binding.txtSkipAdd5.setBackgroundColor(Color.TRANSPARENT)

        //write code to retrieve the data from form
        //var names = binding.edtNamesadd.text.toString()
//        sharedVisitViewModel.visitLog.addnames = binding.edtNamesadd.text.toString()
        //_binding.edtNamesadd.text = names
        //binding.edtNamesadd.text = sharedVisitViewModel.visitLog.addnames.toString()



            binding.txtNextAdd5.setOnClickListener {
                sharedVisitViewModel.visitLog.addnames = binding.edtNamesadd.text.toString()
                findNavController().navigate(R.id.action_additional5_to_additional6)

            }

            binding.txtPreviousAdd5.setOnClickListener {
                findNavController().navigate(R.id.action_additional5_to_additional2)
            }
            binding.txtSkipAdd5.setOnClickListener {
                findNavController().navigate(R.id.action_additional5_to_additional6)
            }
        }

//    override fun onResume() {
//        super.onResume()
//        binding.edtNamesadd.text = sharedVisitViewModel.visitLog.addnames.toString()
//    }
    }
