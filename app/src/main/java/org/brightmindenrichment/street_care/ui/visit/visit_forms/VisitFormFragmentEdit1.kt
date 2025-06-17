package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import org.brightmindenrichment.street_care.databinding.FragmentVisitForm2EditBinding
import org.brightmindenrichment.street_care.util.Extensions
import java.text.SimpleDateFormat
import java.util.*

class VisitFormFragmentEdit1 : Fragment() {

    private var _binding: FragmentVisitForm2EditBinding? = null
    private val binding get() = _binding!!

    private val sharedVisitViewModel: VisitViewModel by activityViewModels()
    private val myCalendar: Calendar = Calendar.getInstance()
    private var selectedTimezone: String = TimeZone.getDefault().id

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitForm2EditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.datePickerActions.text = ""
        binding.timePicker.text = ""

        // Set default timezone display
        val defaultTz = TimeZone.getDefault()
        val abbreviation =
            defaultTz.getDisplayName(defaultTz.inDaylightTime(Date()), TimeZone.SHORT)
        val defaultTzDisplay = "${getRegionName(defaultTz.id)} ($abbreviation)"
        binding.timezoneText.text = defaultTzDisplay
        selectedTimezone = defaultTz.id

        // Date picker
        binding.datePickerActions.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                displayDate(Extensions.dateToString(myCalendar.time, "MM/dd/yyyy"))
            }

            DatePickerDialog(
                requireContext(),
                dateSetListener,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Time picker
        binding.timePicker.setOnClickListener {
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

        // Timezone selector
        binding.timezonePickerCard.setOnClickListener {
            showTimezoneDialog()
        }

        // Cancel
        binding.cancel.setOnClickListener {
            findNavController().popBackStack()
        }


        // Update
        binding.update.setOnClickListener {
            val date = binding.datePickerActions.text.toString().trim()
            val time = binding.timePicker.text.toString().trim()

            if (date.isBlank() || time.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter both date and time",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            try {
                val timeParts = time.split(":")
                val hour = timeParts[0].toIntOrNull()
                val minute = timeParts.getOrNull(1)?.substring(0, 2)?.toIntOrNull()
                val isPM = time.lowercase().contains("pm")

                if (hour != null && minute != null) {
                    val hour24 =
                        if (isPM && hour < 12) hour + 12 else if (!isPM && hour == 12) 0 else hour

                    val userZoneId = java.time.ZoneId.of(selectedTimezone)
                    val localDateTime = java.time.LocalDateTime.of(
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH) + 1,
                        myCalendar.get(Calendar.DAY_OF_MONTH),
                        hour24,
                        minute
                    )
                    val userZonedDateTime = java.time.ZonedDateTime.of(localDateTime, userZoneId)
                    val nyZonedDateTime =
                        userZonedDateTime.withZoneSameInstant(java.time.ZoneId.of("America/New_York"))
                    val newDate = Date.from(nyZonedDateTime.toInstant())


                    val visitId = arguments?.getString("visitId")
//                    val visitId = sharedVisitViewModel.visitLog.id
                    if (visitId.isNullOrEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Error: Visit ID is missing",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    val deviceType = arguments?.getString("fieldName0") ?: ""
                    val db = FirebaseFirestore.getInstance()

                    db.collection("VisitLogBook_New").document(visitId).get()
                        .addOnSuccessListener { doc ->
                            val (collection, key) = if (doc.exists()) {
                                "VisitLogBook_New" to "whenVisit"
                            } else {
                                return@addOnSuccessListener Toast.makeText(requireContext(), "This log cannot be edited.", Toast.LENGTH_LONG).show()
                            }

                            db.collection(collection).document(visitId).update(key, newDate)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Update successful",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    setFragmentResult(
                                        "visit_updated",
                                        bundleOf(
                                            "updated" to true,
                                            "visitDate" to newDate.toString()
                                        )
                                    )

                                    findNavController().popBackStack()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Update failed: ${it.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Failed to check document: ${it.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Invalid time format", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Something went wrong: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
