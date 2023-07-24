package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentAdditional3Binding
import org.brightmindenrichment.street_care.databinding.FragmentAdditional4Binding


class Additional4 : Fragment() {
    private lateinit var _binding: FragmentAdditional4Binding
    private val binding get() = _binding!!

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
        _binding = FragmentAdditional4Binding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setBackgroundColor(Color.TRANSPARENT)
        binding.btnShare.setBackgroundColor(Color.TRANSPARENT)

        binding.btnBack.setOnClickListener {
            binding.btnBack.setBackgroundColor(R.color.colorPrimary)
            binding.btnShare.setBackgroundColor(Color.TRANSPARENT)
            findNavController().navigate(R.id.action_additional4_to_nav_visit)
        }
        binding.btnShare.setOnClickListener {
            binding.btnShare.setBackgroundColor(R.color.colorPrimary)
            binding.btnBack.setBackgroundColor(Color.TRANSPARENT)
            Toast.makeText(context, "This Feature will come in upcoming version", Toast.LENGTH_SHORT).show()
        }
    }
}
