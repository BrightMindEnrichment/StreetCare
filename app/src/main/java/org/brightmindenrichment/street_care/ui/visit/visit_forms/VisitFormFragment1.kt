package org.brightmindenrichment.street_care.ui.visit.visit_forms


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm1Binding
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm2Binding
import org.brightmindenrichment.street_care.util.Extensions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class VisitFormFragment1 : Fragment() {
    private var _binding: FragmentVisitForm1Binding? = null

    val binding get() = _binding!!
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()
    private val myCalendar: Calendar = Calendar.getInstance()
    private val myCalendar1: Calendar = Calendar.getInstance()
    private var displayDateFormat: String = "MM/dd/yyyy"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVisitForm1Binding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewStateRestored(savedInstanceState)

        binding.datePickerActions.setOnClickListener {
            myCalendar.time = populateCalendarToSelectVisitDate()
        }



        binding.timePicker.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListner = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                myCalendar.set(Calendar.HOUR_OF_DAY, hour)
                myCalendar.set(Calendar.MINUTE, minute)
                binding.timePicker.text = SimpleDateFormat("HH:mm").format(myCalendar.time)

            }
            TimePickerDialog(
                context,
                timeSetListner,
                myCalendar.get(Calendar.HOUR_OF_DAY),
                myCalendar.get(Calendar.MINUTE),
                false
            ).show()
        }
        binding.txtNext2.setOnClickListener {

            //Adding code to fix Date and Time issue in whenVisit and andWhenVisitTime
            var time = binding.timePicker.text.toString()
            sharedVisitViewModel.visitLog.whenVisitTime = time
            var offset = 0
            if(time.length > 5){
                val timeFormat = time.substring(5)
                time = time.substring(0,5)
                if(timeFormat.contains("pm", false)){
                    offset = 12
                }
            }
            if(time.contains(":")) {
                val splitTime = time.split(":")
                if (splitTime.size > 1) {
                    myCalendar.set(Calendar.HOUR_OF_DAY, (splitTime[0].toString().toInt() + offset))
                    myCalendar.set(Calendar.MINUTE, splitTime[1].toString().toInt()) // getting error when tested with single minute time such as 11:08am.
                    //displayDate(Extensions.dateToString(myCalendar.time, displayDateFormat))
                    sharedVisitViewModel.visitLog.date = myCalendar.time
                }
            }

            findNavController().navigate(R.id.action_visitFormFragment1_to_visitFormFragment2)
        }
        binding.txtPrevious2.setOnClickListener {
            findNavController().navigate(R.id.action_visitFormFragment1_to_nav_visit)
        }
        binding.txtSkip2.setOnClickListener {
            findNavController().navigate(R.id.action_visitFormFragment1_to_visitFormFragment2)
        }
        }

    private fun populateCalendarToSelectVisitDate(): Date {
        val date = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, day)

            displayDate(Extensions.dateToString(myCalendar.time, displayDateFormat))
            //setting the user selected date into object
            sharedVisitViewModel.visitLog.date = myCalendar.time
        }

        context?.let { it1 ->
            DatePickerDialog(
                it1,
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        return myCalendar.time
    }

    private fun displayDate(dateString: String) {
        binding.datePickerActions.setText(dateString)
    }




    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}


