package org.brightmindenrichment.street_care.ui.visit.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import java.text.SimpleDateFormat
import java.util.Date

class VisitLogDetailsViewModel : ViewModel() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _visitLog = MutableLiveData<VisitLog>()
    val visitLog: LiveData<VisitLog> = _visitLog

    private val _formattedDate = MutableLiveData<String>()
    val formattedDate: LiveData<String> = _formattedDate

    private val _helpType = MutableLiveData<String>()
    val helpType: LiveData<String> = _helpType

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult

    fun setVisitLog(log: VisitLog) {
        _visitLog.value = log
        formatDate(log.date)
        setHelpType(log)
    }

    private fun formatDate(date: Date) {
        val sdf = SimpleDateFormat("dd MMM yyyy")
        _formattedDate.value = sdf.format(date)
    }

    private fun setHelpType(visitLog: VisitLog) {
        val helpTypeList = mutableListOf<String>()
        if (visitLog.food_drink == "Y") helpTypeList.add("food/drink")
        if (visitLog.clothes == "Y") helpTypeList.add("clothes")
        if (visitLog.hygine == "Y") helpTypeList.add("hygiene")
        if (visitLog.wellness == "Y") helpTypeList.add("wellness")
        if (visitLog.lawyerLegal == "Y") helpTypeList.add("lawyer/legal")
        if (visitLog.medicalhelp == "Y") helpTypeList.add("medical")
        if (visitLog.socialWorker == "Y") helpTypeList.add("social")
        if (visitLog.other == "Y") helpTypeList.add("other")

        _helpType.value = helpTypeList.joinToString(", ")
    }

    fun deleteVisitLog() {
        viewModelScope.launch {
            try {
                _visitLog.value?.id?.let { id ->
                    firestore.collection("VisitLogBook").document(id).delete().await()
                    _deleteResult.value = true
                }
            } catch (e: Exception) {
                _deleteResult.value = false
            }
        }
    }
}