package org.brightmindenrichment.street_care.ui.visit

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentVisitBinding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import org.brightmindenrichment.street_care.ui.visit.visit_forms.DetailsButtonClickListener
import org.brightmindenrichment.street_care.ui.visit.visit_forms.VisitLogRecyclerAdapter
import org.brightmindenrichment.street_care.ui.visit.visit_forms.VisitViewModel
import org.brightmindenrichment.street_care.util.Extensions

class VisitFormFragment0 : Fragment() {
    private var _binding: FragmentVisitBinding? = null
    val binding get() = _binding!!
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()
    private val visitDataAdapter = VisitDataAdapter()
    companion object {
        fun newInstance() = VisitFormFragment0()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVisitBinding.inflate(inflater, container, false)
        return _binding!!.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding.btnAddNew.setOnClickListener {
            // if user is submitting multiple visit log together, the view model field should reset
            sharedVisitViewModel.resetVisitLogPage()
            if(Firebase.auth.currentUser != null) {
                showImpactDialog(requireContext())
            } else{
                Extensions.showDialog(
                    requireContext(),
                    requireContext().getString(R.string.alert),
                    requireContext().getString(R.string.visit_log_can_be_recorded_by_logged_in_users),
                    requireContext().getString(R.string.ok),
                    requireContext().getString(R.string.cancel)
                )
            }

        }
        if (Firebase.auth.currentUser != null) {

           updateUI()
        } else {
            Log.d("BME", "not logged in")
        }
    }


    fun showImpactDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("I provided help!")
            .setMessage("Please fill out this form each time you perform an outreach. This helps you track your contributions and allows StreetCare to bring more support and services to help the community!")
            .setPositiveButton("OK") { dialog, _ ->
                sharedVisitViewModel.resetVisitLogPage()
                findNavController().navigate(R.id.action_nav_visit_to_visitFormFragment1)
                dialog.dismiss()
            }
            .create()
            .show()
    }
    private fun updateUI() {
        visitDataAdapter.refresh {
            val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView_visit)
            recyclerView?.layoutManager = LinearLayoutManager(view?.context)
            recyclerView?.adapter = VisitLogRecyclerAdapter(
                requireContext(),
                visitDataAdapter,
                object : DetailsButtonClickListener {
                    override fun onClick(visitLog:VisitLog) {
                        val bundle = bundleOf("visitLog" to visitLog)
                        findNavController().navigate(
                           R.id.action_nav_visit_to_visitLogDetailsFragment,bundle
                        )
                    }
                })
            var totalItemsDonated = visitDataAdapter.getTotalItemsDonated
            var totalOutreaches = visitDataAdapter.size
            var totalPeopleHelped = visitDataAdapter.getTotalPeopleCount


            binding.txtItemDonate.text = totalItemsDonated.toString()
            binding.txtOutreaches.text = totalOutreaches.toString()
            binding.txtPplHelped.text = totalPeopleHelped.toString()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}