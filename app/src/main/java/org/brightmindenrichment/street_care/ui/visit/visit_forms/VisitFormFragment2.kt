package org.brightmindenrichment.street_care.ui.visit.visit_forms



import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.slider.Slider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm2Binding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import org.brightmindenrichment.street_care.util.Extensions


/**
 * A simple [Fragment] subclass.
 * Use the [VisitFormFragment2.newInstance] factory method to
 * create an instance of this fragment.
 */
class VisitFormFragment2 : Fragment() {
    private var _binding : FragmentVisitForm2Binding? = null
    val binding get() = _binding!!
    private val sharedVisitViewModel : VisitViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVisitForm2Binding.inflate(inflater,container,false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rangeSlider.addOnChangeListener(Slider.OnChangeListener { slider, value, fromUser ->
            // setting the hourly spent time for outreach
            binding.tvHours.text = getString(R.string.hours_spent, Extensions.floatToLong(value))
            sharedVisitViewModel.visitLog.hours = Extensions.floatToLong(value)

        })

        // setting outreach options
        binding.btnYes.setOnClickListener{
            sharedVisitViewModel.visitLog.visitAgain = getString(R.string.yes)

        }
        binding.btnNo.setOnClickListener{
            sharedVisitViewModel.visitLog.visitAgain = getString(R.string.no)

        }
        binding.btnUndecided.setOnClickListener{
            sharedVisitViewModel.visitLog.visitAgain = getString(R.string.undecided)

        }
        binding.increaseNoOfPeople.setOnClickListener{
            val count = sharedVisitViewModel.increment(sharedVisitViewModel.visitLog.peopleCount)
            sharedVisitViewModel.visitLog.peopleCount = count
            binding.tvNoOfPeople.text =sharedVisitViewModel.visitLog.peopleCount.toString()

        }
        binding.decreaseNoOfPeople.setOnClickListener{
           val count = sharedVisitViewModel.decrement(sharedVisitViewModel.visitLog.peopleCount)
            sharedVisitViewModel.visitLog.peopleCount = count
           binding.tvNoOfPeople.text =sharedVisitViewModel.visitLog.peopleCount.toString()

        }
        binding.btnSubmitHere.setOnClickListener{
            if(Firebase.auth.currentUser == null){
                Extensions.showDialog(requireContext(), "Anonymous","Logging a visit without logging in may \n result in you, being unable to view your \n visit history.", "Ok")
            }else {
                sharedVisitViewModel.saveVisitLog()
                Toast.makeText(context, "Log saved successfully ", Toast.LENGTH_SHORT).show()
                sharedVisitViewModel.visitLog = VisitLog()
            }
            findNavController().navigate(R.id.action_visitFormFragment2_to_nav_visit)
        }
        binding.btnGoToPage3.setOnClickListener{
            goToNextScreen()
        }

    }
    override fun onResume() {
        super.onResume()
        binding.tvNoOfPeople.text = sharedVisitViewModel.visitLog.peopleCount.toString()

    }


    private fun goToNextScreen(){
        findNavController().navigate(R.id.action_visitFormFragment2_to_visitFormFragment3)
    }



    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

