package org.brightmindenrichment.street_care.ui.community


import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentCommunityActivityBinding
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityActivityAdapter
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityViewModel

class CommunityActivityFragment : Fragment() {

    private lateinit var viewModel: CommunityViewModel
    private lateinit var binding: FragmentCommunityActivityBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommunityActivityBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CommunityViewModel::class.java]
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = CommunityActivityAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        val dividerItemDecoration = DividerItemDecorator(
            ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!
        )
        binding.recyclerView.addItemDecoration(dividerItemDecoration)

        viewModel = ViewModelProvider(this)[CommunityViewModel::class.java]
        viewModel.activitiesLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

}