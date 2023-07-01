package org.brightmindenrichment.street_care.ui.visit.visit_forms


import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm3Binding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import org.brightmindenrichment.street_care.util.Extensions


class VisitFormFragment3 : Fragment() {
    private var _binding: FragmentVisitForm3Binding? = null
    private val binding get() = _binding!!
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVisitForm3Binding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.increaseNoOfPeople.setOnClickListener {
            val count = sharedVisitViewModel.increment(sharedVisitViewModel.visitLog.peopleCount)
            binding.tvNoOfPeople.text = sharedVisitViewModel.visitLog.peopleCount.toString()
            sharedVisitViewModel.visitLog.peopleCount = count
        }
        binding.decreaseNoOfPeople.setOnClickListener {
            val count = sharedVisitViewModel.decrement(sharedVisitViewModel.visitLog.peopleCount)
            sharedVisitViewModel.visitLog.peopleCount = count
            binding.tvNoOfPeople.text = sharedVisitViewModel.visitLog.peopleCount.toString()
        }
        binding.txtNext3.setOnClickListener {
            sharedVisitViewModel.visitLog.names = binding.edtNames.text.toString()
            findNavController().navigate(R.id.action_visitFormFragment3_to_visitFormFragment4)
        }
        binding.txtPrevious3.setOnClickListener {
            findNavController().navigate(R.id.action_visitFormFragment3_to_visitFormFragment2)
        }
        binding.txtSkip3.setOnClickListener {
            findNavController().navigate(R.id.action_visitFormFragment3_to_visitFormFragment4)
        }
    }
    override fun onResume() {
        super.onResume()
        binding.tvNoOfPeople.text = sharedVisitViewModel.visitLog.peopleCount.toString()
    }

    /*   binding.btnSubmitVisit.setOnClickListener
       {
           if (Firebase.auth.currentUser == null) {
               Extensions.showDialog(
                   requireContext(),
                   "Anonymous",
                   "Logging a visit without logging in may \n result in you, being unable to view your \n visit history.",
                   "Ok"
               )
           } else {
               sharedVisitViewModel.visitLog.comments = getUserComments()
               sharedVisitViewModel.saveVisitLog()
               Toast.makeText(context, "Log saved successfully ", Toast.LENGTH_SHORT).show()
               sharedVisitViewModel.visitLog = VisitLog()
           }
           findNavController().navigate(R.id.action_visitFormFragment3_to_surveySubmittedFragment)
       }


       private fun getUserComments(): String {
          return binding.commentsEditText.text.toString()
       }

   */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

