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
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog

class Additional3 : Fragment() {

    private lateinit var _binding: FragmentAdditional3Binding
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
        _binding = FragmentAdditional3Binding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNo.setBackgroundColor(Color.TRANSPARENT)
        binding.btnYes.setBackgroundColor(Color.TRANSPARENT)
        binding.btnMaybe.setBackgroundColor(Color.TRANSPARENT)

        binding.btnYes.setOnClickListener {
            binding.btnYes.setBackgroundColor(R.color.colorPrimary)
            binding.btnNo.setBackgroundColor(Color.TRANSPARENT)
            binding.btnMaybe.setBackgroundColor(Color.TRANSPARENT)
            sharedVisitViewModel.visitLog.visitAgain = "Yes"
        }

        binding.btnNo.setOnClickListener {
            sharedVisitViewModel.visitLog.visitAgain = "No"
            binding.btnNo.setBackgroundColor(R.color.colorPrimary)
            binding.btnYes.setBackgroundColor(Color.TRANSPARENT)
            binding.btnMaybe.setBackgroundColor(Color.TRANSPARENT)
        }
        binding.btnMaybe.setOnClickListener {
            binding.btnMaybe.setBackgroundColor(R.color.colorPrimary)
            binding.btnNo.setBackgroundColor(Color.TRANSPARENT)
            binding.btnYes.setBackgroundColor(Color.TRANSPARENT)
            sharedVisitViewModel.visitLog.visitAgain = "MayBe"
        }
            binding.txtNextAdd3.setOnClickListener {
                findNavController().navigate(R.id.action_additional3_to_additional4)
                sharedVisitViewModel.saveVisitLog()
                Toast.makeText(context, "Log saved successfully ", Toast.LENGTH_SHORT).show()
                sharedVisitViewModel.visitLog = VisitLog()


            }
            binding.txtPreviousAdd3.setOnClickListener {
                findNavController().navigate(R.id.action_additional2_to_additional3)
            }
            binding.txtSkipAdd3.setOnClickListener {
                findNavController().navigate(R.id.action_additional3_to_additional4)
            }
        }
    }
