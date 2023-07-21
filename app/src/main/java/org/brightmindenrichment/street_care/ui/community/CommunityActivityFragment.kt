package org.brightmindenrichment.street_care.ui.community


import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.brightmindenrichment.street_care.databinding.FragmentCommunityActivityBinding
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityViewModel

class CommunityActivityFragment : Fragment() {

    private lateinit var viewModel: CommunityViewModel
    private lateinit var binding: FragmentCommunityActivityBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(CommunityViewModel::class.java)
    }


}