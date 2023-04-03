package org.brightmindenrichment.street_care.ui.home.start_now.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentAfterBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AfterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AfterFragment : Fragment() {

    private var _binding : FragmentAfterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
      _binding =   FragmentAfterBinding.inflate(inflater, container, false)
      return  _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonLogYourVisit.setOnClickListener{
            findNavController().navigate(R.id.action_afterFragment_to_visitFormFragment1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding =null
    }


}