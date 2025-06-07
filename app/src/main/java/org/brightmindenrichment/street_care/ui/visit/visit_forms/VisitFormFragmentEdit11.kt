package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.brightmindenrichment.street_care.R

class VisitFormFragmentEdit11 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_additional10_edit, container, false)
    }
}

//package org.brightmindenrichment.street_care.ui.visit.visit_forms
//
//import android.app.DatePickerDialog
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import com.google.firebase.Timestamp
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.android.material.timepicker.MaterialTimePicker
//import com.google.android.material.timepicker.TimeFormat
//import org.brightmindenrichment.street_care.R
//import java.text.SimpleDateFormat
//import java.time.LocalDateTime
//import java.time.ZoneId
//import java.time.ZonedDateTime
//import java.util.Calendar
//import java.util.Date
//import java.util.Locale
//import java.util.TimeZone
//
//class VisitFormFragmentEdit11 : Fragment() {
//
//    private lateinit var rootView: View
//
//    private lateinit var dateTextView: TextView
//    private lateinit var timeTextView: TextView
//    private lateinit var timezoneTextView: TextView
//    private lateinit var cancelBtn: TextView
//    private lateinit var updateBtn: TextView
//
//    private var selectedCalendar = Calendar.getInstance()
//    private var selectedTimezone: TimeZone = TimeZone.getDefault()
//
//    private lateinit var firestore: FirebaseFirestore
//    private var visitId: String? = null
//
//    private val TAG = "VisitFormFragmentEdit11"
//
//    private val timezonesList: List<Pair<String, String>> by lazy {
//        TimeZone.getAvailableIDs()
//            .filter { it.contains("/") && !it.contains("Etc") }
//            .map { id ->
//                val tz = TimeZone.getTimeZone(id)
//                val now = System.currentTimeMillis()
//                val abbreviation =
//                    tz.getDisplayName(tz.inDaylightTime(Date(now)), TimeZone.SHORT, Locale.US)
//                val city = id.substringAfter("/")
//                    .replace("_", " ")
//                id to "$city ($abbreviation)"
//            }
//            .sortedBy { it.second }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        firestore = FirebaseFirestore.getInstance()
//
//        arguments?.let {
//            visitId = it.getString("visitId")
//            Log.d(TAG, "Received visitId: $visitId")
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        rootView = inflater.inflate(R.layout.fragment_additional10_edit, container, false)
//
//        dateTextView = rootView.findViewById(R.id.date_picker_actions)
//        timeTextView = rootView.findViewById(R.id.time_picker)
//        timezoneTextView = rootView.findViewById(R.id.timezoneText)
//        cancelBtn = rootView.findViewById(R.id.cancel)
//        updateBtn = rootView.findViewById(R.id.update)
//
//        setupInitialValues()
//        setupListeners()
//
//        return rootView
//    }
//
//    private fun setupInitialValues() {
//        val now = Calendar.getInstance()
//        selectedCalendar.time = now.time
//        selectedTimezone = TimeZone.getDefault()
//
//        updateDateText()
//        updateTimeText()
//        updateTimezoneText()
//
//        Log.d(TAG, "Initial Date: ${dateTextView.text}")
//        Log.d(TAG, "Initial Time: ${timeTextView.text}")
//        Log.d(TAG, "Initial Timezone: ${selectedTimezone.id}")
//    }
//
//    private fun updateDateText() {
//        // Always display date based on local time in selectedCalendar (which is just a Date)
//        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
//        // Don't set timezone here — show date as-is (selectedCalendar time)
//        dateTextView.text = sdf.format(selectedCalendar.time)
//    }
//
//    private fun updateTimeText() {
//        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
//        // Don't set timezone here — show time as-is (selectedCalendar time)
//        timeTextView.text = sdf.format(selectedCalendar.time)
//    }
//
//    private fun updateTimezoneText() {
//        val now = System.currentTimeMillis()
//        val abbreviation = selectedTimezone.getDisplayName(
//            selectedTimezone.inDaylightTime(Date(now)),
//            TimeZone.SHORT,
//            Locale.US
//        )
//        val city = selectedTimezone.id.substringAfter("/").replace("_", " ")
//        timezoneTextView.text = "$city ($abbreviation)"
//    }
//
//    private fun setupListeners() {
//        rootView.findViewById<View>(R.id.date_picker_card).setOnClickListener {
//            val c = selectedCalendar
//            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
//                selectedCalendar.set(Calendar.YEAR, year)
//                selectedCalendar.set(Calendar.MONTH, month)
//                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//                updateDateText()
//                Log.d(TAG, "Date picked: ${dateTextView.text}")
//            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
//        }
//
//        rootView.findViewById<View>(R.id.time_picker_card).setOnClickListener {
//            val c = selectedCalendar
//
//            val picker = MaterialTimePicker.Builder()
//                .setTimeFormat(TimeFormat.CLOCK_12H)
//                .setHour(c.get(Calendar.HOUR_OF_DAY))
//                .setMinute(c.get(Calendar.MINUTE))
//                .setTitleText("Select Time")
//                .build()
//
//            picker.show(parentFragmentManager, "MATERIAL_TIME_PICKER")
//
//            picker.addOnPositiveButtonClickListener {
//                selectedCalendar.set(Calendar.HOUR_OF_DAY, picker.hour)
//                selectedCalendar.set(Calendar.MINUTE, picker.minute)
//                updateTimeText()
//                Log.d(TAG, "Time picked: ${timeTextView.text}")
//            }
//        }
//
//        rootView.findViewById<View>(R.id.timezonePickerCard).setOnClickListener {
//            val tzArray = timezonesList.map { it.second }.toTypedArray()
//
//            androidx.appcompat.app.AlertDialog.Builder(requireContext())
//                .setTitle("Select Timezone")
//                .setItems(tzArray) { _, which ->
//                    val oldTz = selectedTimezone
//                    val oldTime = selectedCalendar.time
//
//                    selectedTimezone = TimeZone.getTimeZone(timezonesList[which].first)
//
//                    // When timezone changes, keep the date/time on screen unchanged:
//                    // Convert the oldTime (which is in oldTz) to new time in selectedTimezone, but keep same wall-clock time
//                    // So we compute the time difference offset and adjust the Calendar accordingly
//
//                    // Get offsets in millis at the given time
//                    val oldOffset = oldTz.getOffset(oldTime.time)
//                    val newOffset = selectedTimezone.getOffset(oldTime.time)
//
//                    // Calculate time difference between old and new timezone offsets
//                    val offsetDiff = oldOffset - newOffset
//
//                    // Adjust selectedCalendar time by offsetDiff to keep displayed date/time the same
//                    selectedCalendar.time = Date(oldTime.time + offsetDiff)
//
//                    updateTimezoneText()
//                    updateDateText()
//                    updateTimeText()
//
//                    Log.d(TAG, "Timezone selected: ${selectedTimezone.id}")
//                    Log.d(TAG, "Date after TZ change: ${dateTextView.text}")
//                    Log.d(TAG, "Time after TZ change: ${timeTextView.text}")
//                }
//                .show()
//        }
//
//        cancelBtn.setOnClickListener {
//            Log.d(TAG, "Cancel clicked - going back")
//            requireActivity().onBackPressed()
//        }
//
//        updateBtn.setOnClickListener {
//            Log.d(TAG, "Update clicked")
//            saveTimestampToFirebase()
//        }
//    }
//
//
//    private fun saveTimestampToFirebase() {
//        if (visitId.isNullOrEmpty()) {
//            Log.e(TAG, "visitId is null or empty. Cannot save timestamp.")
//            return
//        }
//
//        val localDateTime = LocalDateTime.of(
//            selectedCalendar.get(Calendar.YEAR),
//            selectedCalendar.get(Calendar.MONTH) + 1,
//            selectedCalendar.get(Calendar.DAY_OF_MONTH),
//            selectedCalendar.get(Calendar.HOUR_OF_DAY),
//            selectedCalendar.get(Calendar.MINUTE)
//        )
//
//        val userZoneId = selectedTimezone.toZoneId()
//        val userZonedDateTime = localDateTime.atZone(userZoneId)
//
//        val fixedUtcMinus5Zone = ZoneId.of("Etc/GMT+5")
//        val utcMinus5ZonedDateTime = userZonedDateTime.withZoneSameInstant(fixedUtcMinus5Zone)
//
//        val finalDate = Date.from(utcMinus5ZonedDateTime.toInstant())
//        Log.d(TAG, "Converted date/time in fixed UTC-5 zone: $finalDate")
//
//        val timestamp = Timestamp(finalDate)
//        Log.d(TAG, "Timestamp to save in Firebase: $timestamp")
//
//        val data = hashMapOf<String, Any>(
//            "followupDate" to timestamp
//        )
//
//        firestore.collection("VisitLogBook")
//            .document(visitId!!)
//            .update(data)
//            .addOnSuccessListener {
//                Log.d(TAG, "Timestamp successfully saved for visitId: $visitId")
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error saving timestamp", e)
//            }
//    }
//
//
//}
//
//
//
////package org.brightmindenrichment.street_care.ui.visit.visit_forms
////
////import android.app.DatePickerDialog
////import android.icu.text.SimpleDateFormat
////import android.os.Bundle
////import android.util.Log
////import android.view.LayoutInflater
////import android.view.View
////import android.view.ViewGroup
////import android.widget.TextView
////import androidx.fragment.app.Fragment
////import com.google.firebase.Timestamp
////import com.google.firebase.firestore.FirebaseFirestore
////import com.google.android.material.timepicker.MaterialTimePicker
////import com.google.android.material.timepicker.TimeFormat
////import org.brightmindenrichment.street_care.R
////import java.time.LocalDateTime
////import java.time.ZoneId
////import java.time.ZoneOffset
////import java.util.Calendar
////import java.util.Date
////import java.util.Locale
////import java.util.TimeZone
////
////class VisitFormFragmentEdit11 : Fragment() {
////
////    private lateinit var rootView: View
////
////    private lateinit var dateTextView: TextView
////    private lateinit var timeTextView: TextView
////    private lateinit var timezoneTextView: TextView
////    private lateinit var cancelBtn: TextView
////    private lateinit var updateBtn: TextView
////
////    private var selectedCalendar = Calendar.getInstance()
////    private var selectedTimezone: TimeZone = TimeZone.getDefault()
////
////    private lateinit var firestore: FirebaseFirestore
////    private var visitId: String? = null
////
////    private val TAG = "VisitFormFragmentEdit11"
////
////    private val timezonesList: List<Pair<String, String>> by lazy {
////        TimeZone.getAvailableIDs()
////            .filter { it.contains("/") && !it.contains("Etc") }
////            .map { id ->
////                val tz = TimeZone.getTimeZone(id)
////                val now = System.currentTimeMillis()
////                val abbreviation =
////                    tz.getDisplayName(tz.inDaylightTime(Date(now)), TimeZone.SHORT, Locale.US)
////                val city = id.substringAfter("/")
////                    .replace("_", " ")
////                id to "$city ($abbreviation)"
////            }
////            .sortedBy { it.second }
////    }
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        firestore = FirebaseFirestore.getInstance()
////
////        arguments?.let {
////            visitId = it.getString("visitId")
////            Log.d(TAG, "Received visitId: $visitId")
////        }
////    }
////
////    override fun onCreateView(
////        inflater: LayoutInflater,
////        container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View? {
////        rootView = inflater.inflate(R.layout.fragment_additional10_edit, container, false)
////
////        dateTextView = rootView.findViewById(R.id.date_picker_actions)
////        timeTextView = rootView.findViewById(R.id.time_picker)
////        timezoneTextView = rootView.findViewById(R.id.timezoneText)
////        cancelBtn = rootView.findViewById(R.id.cancel)
////        updateBtn = rootView.findViewById(R.id.update)
////
////        setupInitialValues()
////        setupListeners()
////
////        return rootView
////    }
////
////    private fun setupInitialValues() {
////        val now = Calendar.getInstance()
////        selectedCalendar.time = now.time
////        selectedTimezone = TimeZone.getDefault()
////
////        updateDateText()
////        updateTimeText()
////        updateTimezoneText()
////
////        Log.d(TAG, "Initial Date: ${dateTextView.text}")
////        Log.d(TAG, "Initial Time: ${timeTextView.text}")
////        Log.d(TAG, "Initial Timezone: ${selectedTimezone.id}")
////    }
////
////    private fun updateDateText() {
////        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
////        dateTextView.text = sdf.format(selectedCalendar.time)
////    }
////
////    private fun updateTimeText() {
////        val sdf = SimpleDateFormat("hh:mm a", Locale.US) // 'hh' for 12-hour, 'a' for AM/PM
////        timeTextView.text = sdf.format(selectedCalendar.time)
////    }
////
////
////    private fun updateTimezoneText() {
////        val now = System.currentTimeMillis()
////        val abbreviation = selectedTimezone.getDisplayName(
////            selectedTimezone.inDaylightTime(Date(now)),
////            TimeZone.SHORT,
////            Locale.US
////        )
////        val city = selectedTimezone.id.substringAfter("/").replace("_", " ")
////        timezoneTextView.text = "$city ($abbreviation)"
////    }
////
////    private fun setupListeners() {
////        rootView.findViewById<View>(R.id.date_picker_card).setOnClickListener {
////            val c = selectedCalendar
////            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
////                selectedCalendar.set(Calendar.YEAR, year)
////                selectedCalendar.set(Calendar.MONTH, month)
////                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
////                updateDateText()
////                Log.d(TAG, "Date picked: ${dateTextView.text}")
////            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
////        }
////
////        rootView.findViewById<View>(R.id.time_picker_card).setOnClickListener {
////            val c = selectedCalendar
////
////            val picker = MaterialTimePicker.Builder()
////                .setTimeFormat(TimeFormat.CLOCK_12H)
////                .setHour(c.get(Calendar.HOUR_OF_DAY))
////                .setMinute(c.get(Calendar.MINUTE))
////                .setTitleText("Select Time")
////                .build()
////
////            picker.show(parentFragmentManager, "MATERIAL_TIME_PICKER")
////
////            picker.addOnPositiveButtonClickListener {
////                selectedCalendar.set(Calendar.HOUR_OF_DAY, picker.hour)
////                selectedCalendar.set(Calendar.MINUTE, picker.minute)
////                updateTimeText()
////                Log.d(TAG, "Time picked: ${timeTextView.text}")
////            }
////        }
////
////        rootView.findViewById<View>(R.id.timezonePickerCard).setOnClickListener {
////            val tzArray = timezonesList.map { it.second }.toTypedArray()
////
////            androidx.appcompat.app.AlertDialog.Builder(requireContext())
////                .setTitle("Select Timezone")
////                .setItems(tzArray) { _, which ->
////                    val oldTz = selectedTimezone
////                    val oldTime = selectedCalendar.time
////
////                    selectedTimezone = TimeZone.getTimeZone(timezonesList[which].first)
////
////                    // Adjust selectedCalendar so displayed time stays the same when timezone changes
////                    val oldOffset = oldTz.getOffset(oldTime.time)
////                    val newOffset = selectedTimezone.getOffset(oldTime.time)
////                    val offsetDiff = oldOffset - newOffset
////                    selectedCalendar.time = Date(oldTime.time + offsetDiff)
////
////                    updateTimezoneText()
////                    updateDateText()
////                    updateTimeText()
////
////                    Log.d(TAG, "Timezone selected: ${selectedTimezone.id}")
////                    Log.d(TAG, "Date after TZ change: ${dateTextView.text}")
////                    Log.d(TAG, "Time after TZ change: ${timeTextView.text}")
////                }
////                .show()
////        }
////
////        cancelBtn.setOnClickListener {
////            Log.d(TAG, "Cancel clicked - going back")
////            requireActivity().onBackPressed()
////        }
////
////        updateBtn.setOnClickListener {
////            Log.d(TAG, "Update clicked")
////            saveTimestampToFirebase()
////        }
////    }
////
////
////    private fun saveTimestampToFirebase() {
////        if (visitId.isNullOrEmpty()) {
////            Log.e(TAG, "visitId is null or empty. Cannot save timestamp.")
////            return
////        }
////
////        try {
////            // Create LocalDateTime from selectedCalendar fields
////            val localDateTime = LocalDateTime.of(
////                selectedCalendar.get(Calendar.YEAR),
////                selectedCalendar.get(Calendar.MONTH) + 1,
////                selectedCalendar.get(Calendar.DAY_OF_MONTH),
////                selectedCalendar.get(Calendar.HOUR_OF_DAY),
////                selectedCalendar.get(Calendar.MINUTE)
////            )
////
////            // Get ZoneId from the selected TimeZone
////            val userZoneId = selectedTimezone.toZoneId()
////
////            // ZonedDateTime of the selected local datetime in the selected timezone
////            val userZonedDateTime = localDateTime.atZone(userZoneId)
////
////            // Target timezone UTC-5 (could be America/New_York or fixed offset)
////            // If you want fixed offset ignoring daylight savings:
////            val fixedUtcMinus5Zone = ZoneOffset.ofHours(-5)
////
////            // If you want to use America/New_York (which adjusts for DST):
////            // val targetZone = ZoneId.of("America/New_York")
////
////            // Convert userZonedDateTime instant to UTC-5 zone instant
////            val utcMinus5ZonedDateTime = userZonedDateTime.withZoneSameInstant(fixedUtcMinus5Zone)
////
////            val finalDate = Date.from(utcMinus5ZonedDateTime.toInstant())
////
////            Log.d(TAG, "User selected datetime in timezone: $userZonedDateTime")
////            Log.d(TAG, "Converted datetime in UTC-5: $utcMinus5ZonedDateTime")
////            Log.d(TAG, "Date to save: $finalDate")
////
////            val timestamp = Timestamp(finalDate)
////
////            val data = hashMapOf<String, Any>(
////                "followupDate" to timestamp
////            )
////
////            firestore.collection("VisitLogBook")
////                .document(visitId!!)
////                .update(data)
////                .addOnSuccessListener {
////                    Log.d(TAG, "Timestamp successfully saved for visitId: $visitId")
////                }
////                .addOnFailureListener { e ->
////                    Log.e(TAG, "Error saving timestamp", e)
////                }
////
////        } catch (e: Exception) {
////            Log.e(TAG, "Failed to save timestamp", e)
////        }
////    }
////
////}
////
////
