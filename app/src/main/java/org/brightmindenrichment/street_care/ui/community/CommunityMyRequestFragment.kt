package org.brightmindenrichment.street_care.ui.community

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.brightmindenrichment.street_care.databinding.FragmentCommunityMyRequestBinding
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityMyRequestAdapter
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityMyRequestViewModel

class CommunityMyRequestFragment : Fragment() {
    private var _binding: FragmentCommunityMyRequestBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: CommunityMyRequestViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityMyRequestBinding.inflate(inflater,container,false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CommunityMyRequestViewModel::class.java]
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = CommunityMyRequestAdapter(viewModel)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        viewModel.requestListLiveData.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }

    }

}