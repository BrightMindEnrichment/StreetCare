package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.brightmindenrichment.street_care.R
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class VisitFormFragmentEdit1 : Fragment() {

    private lateinit var rootView: View

    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var timezoneTextView: TextView
    private lateinit var cancelBtn: TextView
    private lateinit var updateBtn: TextView

    private var selectedCalendar = Calendar.getInstance()
    private var selectedTimezone: TimeZone = TimeZone.getDefault()

    private lateinit var firestore: FirebaseFirestore
    private var visitId: String? = null

    private val TAG = "VisitFormFragmentEdit1"

    private val timezonesList: List<Pair<String, String>> by lazy {
        TimeZone.getAvailableIDs()
            .filter { it.contains("/") && !it.contains("Etc") }
            .map { id ->
                val tz = TimeZone.getTimeZone(id)
                val now = System.currentTimeMillis()
                val abbreviation =
                    tz.getDisplayName(tz.inDaylightTime(Date(now)), TimeZone.SHORT, Locale.US)
                val city = id.substringAfter("/")
                    .replace("_", " ")
                id to "$city ($abbreviation)"
            }
            .sortedBy { it.second }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()

        arguments?.let {
            visitId = it.getString("visitId")
            Log.d(TAG, "Received visitId: $visitId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_visit_form2_edit, container, false)

        dateTextView = rootView.findViewById(R.id.date_picker_actions)
        timeTextView = rootView.findViewById(R.id.time_picker)
        timezoneTextView = rootView.findViewById(R.id.timezoneText)
        cancelBtn = rootView.findViewById(R.id.cancel)
        updateBtn = rootView.findViewById(R.id.update)

        setupInitialValues()
        setupListeners()

        return rootView
    }

    private fun setupInitialValues() {
        val now = Calendar.getInstance()
        selectedCalendar.time = now.time
        selectedTimezone = TimeZone.getDefault()

        updateDateText()
        updateTimeText()
        updateTimezoneText()

        Log.d(TAG, "Initial Date: ${dateTextView.text}")
        Log.d(TAG, "Initial Time: ${timeTextView.text}")
        Log.d(TAG, "Initial Timezone: ${selectedTimezone.id}")
    }

    private fun updateDateText() {
        // Always display date based on local time in selectedCalendar (which is just a Date)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        // Don't set timezone here — show date as-is (selectedCalendar time)
        dateTextView.text = sdf.format(selectedCalendar.time)
    }

    private fun updateTimeText() {
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        // Don't set timezone here — show time as-is (selectedCalendar time)
        timeTextView.text = sdf.format(selectedCalendar.time)
    }

    private fun updateTimezoneText() {
        val now = System.currentTimeMillis()
        val abbreviation = selectedTimezone.getDisplayName(
            selectedTimezone.inDaylightTime(Date(now)),
            TimeZone.SHORT,
            Locale.US
        )
        val city = selectedTimezone.id.substringAfter("/").replace("_", " ")
        timezoneTextView.text = "$city ($abbreviation)"
    }

    private fun setupListeners() {
        rootView.findViewById<View>(R.id.date_picker_card).setOnClickListener {
            val c = selectedCalendar
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateText()
                Log.d(TAG, "Date picked: ${dateTextView.text}")
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        rootView.findViewById<View>(R.id.time_picker_card).setOnClickListener {
            val c = selectedCalendar

            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(c.get(Calendar.HOUR_OF_DAY))
                .setMinute(c.get(Calendar.MINUTE))
                .setTitleText("Select Time")
                .build()

            picker.show(parentFragmentManager, "MATERIAL_TIME_PICKER")

            picker.addOnPositiveButtonClickListener {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, picker.hour)
                selectedCalendar.set(Calendar.MINUTE, picker.minute)
                updateTimeText()
                Log.d(TAG, "Time picked: ${timeTextView.text}")
            }
        }

        rootView.findViewById<View>(R.id.timezonePickerCard).setOnClickListener {
            val tzArray = timezonesList.map { it.second }.toTypedArray()

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Timezone")
                .setItems(tzArray) { _, which ->
                    val oldTz = selectedTimezone
                    val oldTime = selectedCalendar.time

                    selectedTimezone = TimeZone.getTimeZone(timezonesList[which].first)

                    // When timezone changes, keep the date/time on screen unchanged:
                    // Convert the oldTime (which is in oldTz) to new time in selectedTimezone, but keep same wall-clock time
                    // So we compute the time difference offset and adjust the Calendar accordingly

                    // Get offsets in millis at the given time
                    val oldOffset = oldTz.getOffset(oldTime.time)
                    val newOffset = selectedTimezone.getOffset(oldTime.time)

                    // Calculate time difference between old and new timezone offsets
                    val offsetDiff = oldOffset - newOffset

                    // Adjust selectedCalendar time by offsetDiff to keep displayed date/time the same
                    selectedCalendar.time = Date(oldTime.time + offsetDiff)

                    updateTimezoneText()
                    updateDateText()
                    updateTimeText()

                    Log.d(TAG, "Timezone selected: ${selectedTimezone.id}")
                    Log.d(TAG, "Date after TZ change: ${dateTextView.text}")
                    Log.d(TAG, "Time after TZ change: ${timeTextView.text}")
                }
                .show()
        }

        cancelBtn.setOnClickListener {
            Log.d(TAG, "Cancel clicked - going back")
            requireActivity().onBackPressed()
        }

        updateBtn.setOnClickListener {
            Log.d(TAG, "Update clicked")
            saveTimestampToFirebase()
        }
    }


    private fun saveTimestampToFirebase() {
        if (visitId.isNullOrEmpty()) {
            Log.e(TAG, "visitId is null or empty. Cannot save timestamp.")
            return
        }

        val year = selectedCalendar.get(Calendar.YEAR)
        val month = selectedCalendar.get(Calendar.MONTH) + 1  // Java Calendar months are 0-based, but java.time.Month is 1-based
        val day = selectedCalendar.get(Calendar.DAY_OF_MONTH)
        val hour = selectedCalendar.get(Calendar.HOUR_OF_DAY)
        val minute = selectedCalendar.get(Calendar.MINUTE)

        val selectedZoneId = selectedTimezone.toZoneId()

        // Create ZonedDateTime from fields + selected timezone
        val localZonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, selectedZoneId)

        // Convert to America/New_York timezone (UTC-5 or UTC-4 depending on DST)
        val targetZoneId = ZoneId.of("America/New_York")
        val convertedZonedDateTime = localZonedDateTime.withZoneSameInstant(targetZoneId)

        val finalDate = Date.from(convertedZonedDateTime.toInstant())
        Log.d(TAG, "Converted Date for America/New_York timezone: $finalDate")

        val timestamp = Timestamp(finalDate)
        Log.d(TAG, "Firebase Timestamp to save: $timestamp")

        val data = hashMapOf<String, Any>(
            "whenVisit" to timestamp
        )

        firestore.collection("VisitLogBook")
            .document(visitId!!)
            .update(data)
            .addOnSuccessListener {
                Log.d(TAG, "Timestamp successfully saved for visitId: $visitId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving timestamp", e)
            }
    }


}
