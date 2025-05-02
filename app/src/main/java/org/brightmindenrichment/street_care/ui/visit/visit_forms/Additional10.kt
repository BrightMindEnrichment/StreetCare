package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import java.sql.Timestamp
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentAdditional10Binding
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import org.brightmindenrichment.street_care.util.Extensions
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class Additional10 : Fragment() {

    private lateinit var _binding: FragmentAdditional10Binding
    private val binding get() = _binding!!
    private val sharedVisitViewModel: VisitViewModel by activityViewModels()
    private val myCalendar: Calendar = Calendar.getInstance()
    private var displayDateFormat: String = "MM/dd/yyyy"
    private var selectedTimezone = TimeZone.getDefault().id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdditional10Binding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.datePickerActions.text = getString(R.string.enter_date)
        binding.timePicker.text = getString(R.string.enter_time)

        val defaultTz = TimeZone.getDefault()
        val abbreviation = defaultTz.getDisplayName(defaultTz.inDaylightTime(Date()), TimeZone.SHORT)
        val defaultTzDisplay = "${getRegionName(defaultTz.id)} ($abbreviation)"
        binding.timezoneText.text = defaultTzDisplay
        selectedTimezone = defaultTz.id

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

        binding.txtSkip3.setOnClickListener {
            findNavController().navigate(R.id.action_additional10_to_additional3)
        }

        binding.txtNext2.setOnClickListener {
            val date = binding.datePickerActions.text.toString().trim()
            val time = binding.timePicker.text.toString().trim()

            // Save date only if it's not placeholder
            if (!isInvalidDate(date)) {
                sharedVisitViewModel.visitLog.date = myCalendar.time
            }

            // Save time only if it's not placeholder
            if (time.contains(":")) {
                val splitTime = time.split(":")
                val hour = splitTime[0].toIntOrNull()
                val minute = splitTime.getOrNull(1)?.substring(0, 2)?.toIntOrNull()
                val isPM = time.lowercase().contains("pm")

                if (hour != null && minute != null) {
                    val hour24 = if (isPM && hour < 12) hour + 12 else if (!isPM && hour == 12) 0 else hour
                    myCalendar.set(Calendar.HOUR_OF_DAY, hour24)
                    myCalendar.set(Calendar.MINUTE, minute)
                    sharedVisitViewModel.visitLog.followupDate = myCalendar.time
                }
            }

            findNavController().navigate(R.id.action_additional10_to_additional3)
        }


        binding.txtPrevious2.setOnClickListener {
            findNavController().navigate(R.id.action_additional10_to_additional9)
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
        val seenAbbreviations = mutableSetOf<String>()
        val displayList = mutableListOf<String>()
        val displayToZoneId = mutableMapOf<String, String>()

        for (zoneId in TimeZone.getAvailableIDs()) {
            if (zoneId.startsWith("Etc/") || zoneId.startsWith("SystemV") || !zoneId.contains("/")) continue
            val tz = TimeZone.getTimeZone(zoneId)
            val abbreviation = tz.getDisplayName(tz.inDaylightTime(Date()), TimeZone.SHORT)

            if (abbreviation in seenAbbreviations) continue
            seenAbbreviations.add(abbreviation)
            val city = zoneId.substringAfterLast("/").replace("_", " ")
            val display = "$city ($abbreviation)"
            displayList.add(display)
            displayToZoneId[display] = zoneId
        }

        displayList.sort()
        val listView = ListView(requireContext())
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, displayList)
        listView.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Timezone")
            .setView(listView)
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedDisplay = adapter.getItem(position) ?: return@setOnItemClickListener
            selectedTimezone = displayToZoneId[selectedDisplay] ?: selectedTimezone
            binding.timezoneText.text = selectedDisplay
            dialog.dismiss()
        }

        dialog.show()
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

}
