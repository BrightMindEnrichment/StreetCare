package org.brightmindenrichment.street_care.ui.community

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.ui.visit.visit_forms.VisitViewModel
import org.brightmindenrichment.street_care.util.Extensions
import java.util.*

class AddEvent : Fragment() {
    lateinit var edtTitle: EditText
    lateinit var edtDate: EditText
    lateinit var edtTime: EditText
    lateinit var edtDesc: EditText
    lateinit var edtLocation: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_event, container, false)
     }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        edtTitle = view.findViewById<EditText>(R.id.edtTitle)
        edtDate = view.findViewById<EditText>(R.id.edtDate)
        edtTime = view.findViewById<EditText>(R.id.edtTime)
        edtDesc = view.findViewById<EditText>(R.id.edtDesc)
        edtLocation = view.findViewById<EditText>(R.id.edtLocation)
        val myCalendar = Calendar.getInstance()
        val mHour = myCalendar.get(Calendar.HOUR)
        val mMinute = myCalendar.get(Calendar.MINUTE)
        val year = myCalendar.get(Calendar.YEAR)
        val month = myCalendar.get(Calendar.MONTH)
        val day = myCalendar.get(Calendar.DAY_OF_MONTH)
        edtTime.setOnClickListener {
            val timePickerDialog = context?.let { it1 ->
                TimePickerDialog(context,
                    R.style.MyDatePickerDialogTheme,
                    OnTimeSetListener { view, hourOfDay, minute -> edtTime.setText("$hourOfDay:$minute") },
                    mHour,
                    mMinute,
                    false
                )
            }
            timePickerDialog?.show()
        }
        edtDate.setOnClickListener {
            val datePickerDialog = context?.let { it1 ->
                DatePickerDialog(it1, R.style.MyDatePickerDialogTheme,
                    { view, year, monthOfYear, dayOfMonth ->
                        val dat = (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                        edtDate.setText(dat)
                    }, year, month, day
                )
            }
            datePickerDialog?.show()
        }
        btnSubmit.setOnClickListener {
            if (Firebase.auth.currentUser == null) {
                Extensions.showDialog(requireContext(), "Alert ","Please Login before submit the Event", "Ok")
            } else {
                var title = edtTitle.text.toString()
                var date = edtDate.text.toString()
                var time = edtTime.text.toString()
                var desc = edtDesc.text.toString()
                var location = edtLocation.text.toString()
                if (TextUtils.isEmpty(title)) {
                    edtTitle.setError("Required")
                } else if (TextUtils.isEmpty(date)) {
                    edtDate.setError("Required")
                } else if (TextUtils.isEmpty(time)) {
                    edtTime.setError("Required")
                } else if (TextUtils.isEmpty(location)) {
                    edtLocation.setError("Required")
                } else {
                    addEvent(title, desc, date, time, location)
                }
            }
        }
    }
    fun addEvent(title: String, description: String, date: String, time: String, location: String) {
        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return
        // create a map of event data so we can add to firebase
        val eventData = hashMapOf(
            "title" to title,
            "description" to description,
            "date" to date,
            "interest" to 1,
            "time" to time,
            "location" to location,
            "uid" to user.uid,
            "status" to "pending")
        // save to firebase
        val db = Firebase.firestore
        db.collection("events").add(eventData).addOnSuccessListener { documentReference ->
            Log.d("BME", "Saved with id ${documentReference.id}")
            Extensions.showDialog(requireContext(), "Alert","Event registered for Approval", "Ok")
            edtDate.text.clear()
            edtTime.text.clear()
            edtLocation.text.clear()
            edtDesc.text.clear()
            edtTitle.text.clear()
            Toast.makeText(context, "Successfully Registered", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.nav_community)
        }.addOnFailureListener { exception ->
            Log.w("BMR", "Error in addEvent ${exception.toString()}")
            Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show()
        }
    }
}