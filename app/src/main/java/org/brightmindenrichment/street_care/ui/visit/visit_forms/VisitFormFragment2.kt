package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm2Binding
import org.brightmindenrichment.street_care.util.Extensions
import java.text.SimpleDateFormat
import android.widget.ListView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import java.util.*

class VisitFormFragment2 : Fragment() {
    private var _binding: FragmentVisitForm2Binding? = null
    val binding get() = _binding!!

    private val sharedVisitViewModel: VisitViewModel by activityViewModels()
    private val myCalendar: Calendar = Calendar.getInstance()
    private var displayDateFormat: String = "MM/dd/yyyy"
    private var selectedTimezone = TimeZone.getDefault().id

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitForm2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Display current date and time
        displayDate(Extensions.dateToString(myCalendar.time, displayDateFormat))
        updateTimeDisplay()

        // Set default timezone
        val defaultTz = TimeZone.getDefault()
        val abbreviation = defaultTz.getDisplayName(defaultTz.inDaylightTime(Date()), TimeZone.SHORT)
        val defaultTzDisplay = "${getRegionName(defaultTz.id)} ($abbreviation)"
        binding.timezoneText.text = defaultTzDisplay
        selectedTimezone = defaultTz.id

        // Save initial date to view model
        sharedVisitViewModel.visitLog.date = myCalendar.time

        binding.datePickerActions.setOnClickListener {
            binding.datePickerActions.error = null
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                displayDate(Extensions.dateToString(myCalendar.time, displayDateFormat))
                sharedVisitViewModel.visitLog.date = myCalendar.time
            }

            DatePickerDialog(
                requireContext(),
                dateSetListener,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.timePicker.setOnClickListener {
            binding.timePicker.error = null
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                myCalendar.set(Calendar.HOUR_OF_DAY, hour)
                myCalendar.set(Calendar.MINUTE, minute)
                updateTimeDisplay()
            }

            TimePickerDialog(
                context,
                timeSetListener,
                myCalendar.get(Calendar.HOUR_OF_DAY),
                myCalendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        binding.timezonePickerCard.setOnClickListener {
            showTimezoneDialog()
        }

        binding.txtNext2.setOnClickListener {
            val date = binding.datePickerActions.text.toString().trim()
            val time = binding.timePicker.text.toString().trim()
            var hasError = false

            if (isInvalidDate(date)) {
                binding.datePickerActions.error = getString(R.string.error_date_required)
                hasError = true
            } else {
                binding.datePickerActions.error = null
            }

            if (isInvalidTime(time)) {
                binding.timePicker.error = getString(R.string.error_time_required)
                hasError = true
            } else {
                binding.timePicker.error = null
            }

            if (hasError) {
                Toast.makeText(requireContext(), getString(R.string.error_date_time_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sharedVisitViewModel.visitLog.whenVisitTime = time
            sharedVisitViewModel.visitLog.timeZone = selectedTimezone

            val userTz = TimeZone.getTimeZone(selectedTimezone)
            val userCalendar = Calendar.getInstance(userTz)

            userCalendar.set(
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )

            if (time.contains(":")) {
                val splitTime = time.split(":")
                val hour = splitTime[0].toIntOrNull()
                val minutePart = splitTime.getOrNull(1) ?: ""
                val minute = minutePart.substring(0, 2).toIntOrNull() ?: 0
                val isPM = time.lowercase().contains("pm")

                if (hour != null) {
                    val hour24 = if (isPM && hour < 12) hour + 12 else if (!isPM && hour == 12) 0 else hour
                    userCalendar.set(Calendar.HOUR_OF_DAY, hour24)
                    userCalendar.set(Calendar.MINUTE, minute)
                    userCalendar.set(Calendar.SECOND, 0)

                    val backendDate = convertToBackendTime(userCalendar.time, selectedTimezone)
                    sharedVisitViewModel.visitLog.date = backendDate
                }
            }

            findNavController().navigate(R.id.action_visitFormFragment2_to_visitFormFragment1)
        }

        binding.txtPrevious2.setOnClickListener {
            findNavController().navigate(R.id.action_visitFormFragment2_to_nav_visit)
        }
    }

    private fun displayDate(dateString: String) {
        binding.datePickerActions.text = dateString
    }

    private fun updateTimeDisplay() {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val formattedTime = timeFormat.format(myCalendar.time)
        binding.timePicker.text = formattedTime
    }

    private fun getRegionName(timezoneId: String): String {
        return timezoneId.split("/").lastOrNull()?.replace("_", " ") ?: timezoneId
    }

    private fun showTimezoneDialog() {
        val displayList = mutableListOf<String>()
        val zoneIdList = mutableListOf<String>()
        val currentDate = Date()

        TimeZone.getAvailableIDs().forEach { zoneId ->
            if (zoneId.startsWith("SystemV")) return@forEach

            val tz = TimeZone.getTimeZone(zoneId)
            val abbreviation = tz.getDisplayName(tz.inDaylightTime(currentDate), TimeZone.SHORT)

            // Friendly display name
            val displayName = if (zoneId.contains("/")) {
                zoneId.split("/").last().replace("_", " ")
            } else {
                zoneId.replace("_", " ")
            }

            val displayText = "$displayName ($abbreviation)"
            displayList.add(displayText)
            zoneIdList.add(zoneId)
        }

        val sortedPairs = displayList.zip(zoneIdList).sortedBy { it.first }
        val sortedDisplayList = sortedPairs.map { it.first }
        val sortedIdList = sortedPairs.map { it.second }

        val listView = ListView(requireContext())
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, sortedDisplayList)
        listView.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Timezone")
            .setView(listView)
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            selectedTimezone = sortedIdList[position]
            binding.timezoneText.text = sortedDisplayList[position]
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun convertToBackendTime(userDate: Date, userTzId: String): Date {
        val userTz = TimeZone.getTimeZone(userTzId)
        val backendTz = TimeZone.getTimeZone("GMT-5")

        val userOffset = userTz.getOffset(userDate.time)
        val backendOffset = backendTz.getOffset(userDate.time)
        val diff = backendOffset - userOffset

        return Date(userDate.time + diff)
    }

    private fun isInvalidDate(date: String): Boolean {
        val placeholders = listOf(
            getString(R.string.enter_date),
            getString(R.string.enter_date) + "*",
            getString(R.string.error_date_required)
        )
        return date.isEmpty() || placeholders.contains(date)
    }

    private fun isInvalidTime(time: String): Boolean {
        val placeholders = listOf(
            getString(R.string.enter_time),
            getString(R.string.enter_time) + "*",
            getString(R.string.error_time_required)
        )
        return time.isEmpty() || placeholders.contains(time)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
