package org.brightmindenrichment.street_care.ui.visit

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentVisitBinding
import org.brightmindenrichment.street_care.ui.community.CommunityRecyclerAdapter
import org.brightmindenrichment.street_care.ui.visit.visit_forms.VisitLogRecyclerAdapter
import org.brightmindenrichment.street_care.ui.visit.visit_forms.VisitViewModel
import java.util.*

class VisitFragment : Fragment() {
    private var _binding : FragmentVisitBinding? = null
    val binding get() = _binding!!
    private val sharedVisitViewModel : VisitViewModel by activityViewModels()
    private val visitDataAdapter = VisitDataAdapter()

    companion object {
        fun newInstance() = VisitFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVisitBinding.inflate(inflater, container, false)
        return _binding!!.root

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddVisit.setOnClickListener{
             // if user is submitting multiple visit log together, the view model field should reset
            sharedVisitViewModel.resetVisitLogPage()

            findNavController().navigate(R.id.action_nav_visit_to_visitFormFragment1)
        }
        if (Firebase.auth.currentUser != null) {
            updateUI()
        }
        else {
            // TODO : some message to user
            Log.d("BME", "not logged in")
        }

    }

    private fun updateUI() {

        visitDataAdapter.refresh {

            val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView_visit)

            recyclerView?.layoutManager = LinearLayoutManager(view?.context)
            recyclerView?.adapter = VisitLogRecyclerAdapter(requireContext(), visitDataAdapter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}