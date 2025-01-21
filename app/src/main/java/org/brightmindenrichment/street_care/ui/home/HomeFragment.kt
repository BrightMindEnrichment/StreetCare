package org.brightmindenrichment.street_care.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.slider.Slider
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.CardHomeFragmentBinding
import org.brightmindenrichment.street_care.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    // Companion object
    companion object {
        private var isBannerDismissed = false
    }

    private var _binding: FragmentHomeBinding? = null
    private val url: String = "https://streetcare.us/donations/"

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val images = arrayOf(R.drawable.image12, R.drawable.image26, R.drawable.image25)

    private lateinit var includedLayout: CardHomeFragmentBinding

    private var sliderAdapter: SliderAdapter = SliderAdapter(images)

    // Verification-Banner
    private lateinit var verificationBanner: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        includedLayout = binding.cardHomeFragment

        // Initialize banner
        verificationBanner = binding.verificationBanner.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // setupBanner call
        setupBanner()
        setUpSliderImagesView()


        includedLayout.cardStartNow.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_startNowFragment)
        }

        includedLayout.cardWhatToGive.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_whatToGiveFragment)

        }

        includedLayout.cardHowToVideos.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_howToVideosFragment)
        }
        includedLayout.cardDonate.setOnClickListener {
            goToUrl(url)
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpSliderImagesView() {
        val sliderView = binding.sliderView
        sliderView.setSliderAdapter(sliderAdapter)
        sliderView.autoCycleDirection = Slider.LAYOUT_DIRECTION_LTR
        sliderView.isAutoCycle = true
        sliderView.scrollTimeInSec = 2
        sliderView.startAutoCycle()
    }

    private fun goToUrl(url: String) {
        val uri: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    // Banner function
    private fun setupBanner() {
        if (isBannerDismissed) {
            verificationBanner.visibility = View.GONE
        } else {
            verificationBanner.visibility = View.VISIBLE

            // Set up close button
            verificationBanner.findViewById<ImageButton>(R.id.btn_close).setOnClickListener {
                verificationBanner.visibility = View.GONE
                isBannerDismissed = true
            }
        }
    }

}


