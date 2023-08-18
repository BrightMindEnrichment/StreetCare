package org.brightmindenrichment.street_care.ui.community

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentCommunityMyPostBinding
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityHelpViewModel

class CommunityMyPostFragmentFragment : Fragment() {

    private lateinit var helpBtn: Button
    private lateinit var requestBtn: Button
    private var requestFlag: String = "Request"
    private var helpFlag: String = "Help"
    private var _binding: FragmentCommunityMyPostBinding? = null
    private var fragmentFlag: String = "Request"
    private val binding get() = _binding!!
    companion object {
        fun newInstance() = CommunityHelpFragment()
    }

    private lateinit var viewModel: CommunityHelpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityMyPostBinding.inflate(inflater, container, false)
        helpBtn = binding.helpBtn
        requestBtn = binding.requestBtn


        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CommunityHelpViewModel::class.java]
        //by passing string in the bundle and if else to see which fragment to start
        helpBtn.setOnClickListener {
            startMyHelpFragment()
        }

        requestBtn.setOnClickListener {
            startRequestHelpFragment()
        }

        arguments?.let {
            initSubFragment(it)
        }

    }

    private fun initSubFragment(args: Bundle){
        val key = "name"
        if (args.getString(key)=="Help") {
            startMyHelpFragment()
        } else if (args.getString(key)=="Request") {
            startRequestHelpFragment()
        }
    }
    private fun startMyHelpFragment(){

        helpBtn.setTextAppearance(R.style.RightRoundButtonActivate)
        helpBtn.setBackgroundResource(R.drawable.green_right_round_button)
        requestBtn.setTextAppearance(R.style.LeftRoundButtonStyle)
        requestBtn.setBackgroundResource(R.drawable.left_round_button)
        //TODO: Change the fragment
        val wantHelpFragment = CommunityWantHelpFragment()
        fragmentFlag = helpFlag
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, wantHelpFragment)
            .commit()
    }
    private fun startRequestHelpFragment(){
        helpBtn.setTextAppearance(R.style.RightRoundButtonStyle)
        helpBtn.setBackgroundResource(R.drawable.right_round_button)
        requestBtn.setTextAppearance(R.style.LeftRoundButtonActivate)
        requestBtn.setBackgroundResource(R.drawable.green_left_round_button)
        //TODO: Change the fragment
        val requestHelpFragment = CommunityNeedHelpFragment()
        fragmentFlag = requestFlag
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, requestHelpFragment)
            .commit()
    }

}