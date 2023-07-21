package org.brightmindenrichment.street_care.ui.community

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentCommunityPostHelpBinding
import org.brightmindenrichment.street_care.databinding.FragmentCommunityPostRequestBinding
import org.brightmindenrichment.street_care.util.Extensions
import java.util.Calendar
import java.util.Date

private const val FLAG = "Help"
class CommunityPostHelpFragment : Fragment() {
    private lateinit var inputTitle: EditText
    private lateinit var contactInfo: EditText
    private lateinit var inputDescription: EditText
    private lateinit var inputLocation: EditText
    private var _binding: FragmentCommunityPostHelpBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCommunityPostHelpBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnSubmit = binding.btnPost
        val btnDiscard = binding.btnDiscard
        inputTitle = binding.titleText
        inputDescription = binding.requestDetailText
        inputLocation = binding.locationText
        contactInfo = binding.contactText
//        edtTime = binding.edtTime

        val myCalendar = Calendar.getInstance()
        val mHour = myCalendar.get(Calendar.HOUR)
        val mMinute = myCalendar.get(Calendar.MINUTE)
        val year = myCalendar.get(Calendar.YEAR)
        val month = myCalendar.get(Calendar.MONTH)
        val day = myCalendar.get(Calendar.DAY_OF_MONTH)

//        edtTime.setOnClickListener {
//            val timePickerDialog = context?.let { it1 ->
//                TimePickerDialog(context,
//                    R.style.MyDatePickerDialogTheme,
//                    { view, hourOfDay, minute ->
//                        edtTime.setText(
//                            "$hourOfDay:$minute"
//                        )
//                    },
//                    mHour,
//                    mMinute,
//                    false
//                )
//            }
//            timePickerDialog?.show()
//        }
//        contactInfo.setOnClickListener {
//            val datePickerDialog = context?.let { it1 ->
//                DatePickerDialog(it1, R.style.MyDatePickerDialogTheme,
//                    { view, year, monthOfYear, dayOfMonth ->
//                        val dat = (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
//                        contactInfo.setText(dat)
//                    }, year, month, day
//                )
//            }
//            datePickerDialog?.show()
//        }
        btnSubmit.setOnClickListener {
            if (Firebase.auth.currentUser == null) {
                context?.let { it1 -> Extensions.showDialog(it1, "Alert","Please Login before submit the Event", "Ok") }
            } else {
                var title = inputTitle.text.toString()
                var contact = contactInfo.text.toString()
                var desc = inputDescription.text.toString()
                var location = inputLocation.text.toString()
                if (TextUtils.isEmpty(title)) {
                    inputTitle.error = "Required"
                } else if (TextUtils.isEmpty(desc)) {
                    inputDescription.error = "Required"
                } else if (TextUtils.isEmpty(location)) {
                    inputLocation.error = "Required"
                } else {
//                    addEvent(title, desc, contact, location)
                }
            }
        }

        btnDiscard.setOnClickListener {
            clearFields()
        }
    }


    private fun addEvent(title: String, description: String, time: String, location: String) {
        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return
        // create a map of event data so we can add to firebase
        val eventData = hashMapOf(
            "title" to title,
            "description" to description,
            "date" to Date(),
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
            clearFields()
            Toast.makeText(context, "Successfully Registered", Toast.LENGTH_LONG).show()
            navBack()
        }.addOnFailureListener { exception ->
            Log.w("BMR", "Error in addEvent ${exception.toString()}")
            Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearFields() {
        contactInfo.text.clear()
        inputLocation.text.clear()
        inputDescription.text.clear()
        inputTitle.text.clear()
    }

    private fun navBack(){
        val bundle = Bundle()
        bundle.putString("name", FLAG)
        findNavController().navigate(R.id.communityHelpFragment,bundle)
    }
}