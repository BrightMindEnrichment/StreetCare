package org.brightmindenrichment.street_care.ui.community

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.brightmindenrichment.street_care.databinding.FragmentCommunityWantHelpBinding
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityWantHelpAdapter
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityWantHelpViewModel

class CommunityMyRequestFragment : Fragment() {
    private var _binding: FragmentCommunityWantHelpBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: CommunityWantHelpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityWantHelpBinding.inflate(inflater,container,false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CommunityWantHelpViewModel::class.java]
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = CommunityWantHelpAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        viewModel.helpListLiveData.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }
    }

}