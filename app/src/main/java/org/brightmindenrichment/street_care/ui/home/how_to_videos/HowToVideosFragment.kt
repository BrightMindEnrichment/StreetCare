package org.brightmindenrichment.street_care.ui.home.how_to_videos

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineExceptionHandler
import org.brightmindenrichment.street_care.YouTube.YouTubeController
import org.brightmindenrichment.street_care.databinding.FragmentHowToVideosBinding
import org.brightmindenrichment.street_care.R


class HowToVideosFragment : Fragment() {
    private var _binding : FragmentHowToVideosBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHowToVideosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonHowToUseApp.setOnClickListener {
            val videoId = "PLh7GZtyt8qiJKRJ0O_oJ5qRlvGeZUH2Zk"
            val bundle = bundleOf("videoId" to videoId)
            findNavController().navigate(R.id.action_howtovideos_to_howtouseapplist, bundle)
        }

        binding.buttonHowToIntro.setOnClickListener {
            val videoId = "PLh7GZtyt8qiLKwO_WoE0Vmcu6UMV1AtV9"
            val bundle = bundleOf("videoId" to videoId)
            findNavController().navigate(R.id.action_howtovideos_to_streetoutreachlist, bundle)
        }

        binding.buttonHowToVets.setOnClickListener {
            val videoId = "PLh7GZtyt8qiKevwz9gkTs0OyaglNnfUcl"
            val bundle = bundleOf("videoId" to videoId)
            findNavController().navigate(R.id.action_howtovideos_to_homelessvetslist, bundle)
        }

        binding.buttonHowToSafety.setOnClickListener {
            val videoId = "PLh7GZtyt8qiJsEwIitzTaZ2l3aA1Kiyx0"
            val bundle = bundleOf("videoId" to videoId)
            findNavController().navigate(R.id.action_howtovideos_to_streetsafetylist, bundle)
        }

        binding.buttonHowToMentalIllness.setOnClickListener {
            val videoId = "PLh7GZtyt8qiKCy8iYdDzMXttuw6s7fdkP"
            val bundle = bundleOf("videoId" to videoId)
            findNavController().navigate(R.id.action_howtovideos_to_mentallillnesslist, bundle)
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HowToVideosFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}