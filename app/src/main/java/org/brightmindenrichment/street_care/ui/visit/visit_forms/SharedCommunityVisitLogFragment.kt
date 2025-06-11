package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentLoginRedirectBinding
import org.brightmindenrichment.street_care.databinding.FragmentSharedCommunityVisitLogBinding


class SharedCommunityVisitLogFragment : Fragment() {
    private var _binding: FragmentSharedCommunityVisitLogBinding? = null
    val binding get() = _binding!!
    private var clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSharedCommunityVisitLogBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAnotherVisit.setOnClickListener{
            clicked = true
            findNavController().navigate(R.id.action_sharedCommunityVisitLogFragment_to_visit_form2)
        }
       binding.btnInteraction.setOnClickListener {

           findNavController().navigate(R.id.action_sharedCommunityVisitLogFragment_to_nav_visit)
       }
        // Handle back button press
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
               // clicked =true
                findNavController().navigate(R.id.action_sharedCommunityVisitLogFragment_to_nav_visit)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


    }
    override fun onDestroyView() {
        super.onDestroyView()


          if(!clicked) {
              requireActivity()
                  .findViewById<BottomNavigationView>(R.id.bottomNav)
                  .selectedItemId = R.id.loginRedirectFragment

          }

    }


}