package org.brightmindenrichment.street_care.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.brightmindenrichment.street_care.databinding.FragmentCommunityNeedHelpBinding
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityMyHelpAdapter
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityNeedHelpAdapter
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityMyHelpViewModel
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityNeedHelpViewModel

class CommunityMyHelpFragment : Fragment() {
    private var _binding: FragmentCommunityNeedHelpBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var viewModel: CommunityMyHelpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityNeedHelpBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CommunityMyHelpViewModel::class.java]

        setupRecyclerView()
    }
    private fun setupRecyclerView() {
        val adapter = CommunityMyHelpAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        viewModel.helpListLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

}