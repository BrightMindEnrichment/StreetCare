package org.brightmindenrichment.street_care.ui.home.what_to_give

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentWhatToGiveBinding


class WhatToGiveFragment : Fragment() {
    private var _binding : FragmentWhatToGiveBinding? = null
    val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentWhatToGiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //initialize data
        val myDataset = DataSource().loadWhatToGiveItems()
        binding.recyclerviewImage.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerviewImage.adapter = ItemToGiveAdapter(requireContext(),myDataset)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}