package org.brightmindenrichment.street_care.ui.visit.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import java.text.SimpleDateFormat
import java.util.Date

class VisitLogDetailsViewModel : ViewModel() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _visitLog = MutableLiveData<VisitLog>()
    val visitLog: LiveData<VisitLog> = _visitLog

    private val _formattedDateTime = MutableLiveData<String>()
    val formattedDateTime: LiveData<String> = _formattedDateTime

    private val _formattedDate = MutableLiveData<String>()
    val formattedDate: LiveData<String> = _formattedDate

    private val _helpType = MutableLiveData<String>()
    val helpType: LiveData<String> = _helpType

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult

    fun setVisitLog(log: VisitLog) {
        _visitLog.value = log
        formatDate(log.date)
        formatDateTime(log.date)
        setHelpType(log)
    }

    private fun formatDate(date: Date) {
        val sdf = SimpleDateFormat("dd MMM yyyy")
        _formattedDate.value = sdf.format(date)
    }

    private fun formatDateTime(date: Date) {
        val sdfDate = SimpleDateFormat("dd MMM yyyy")
        val sdfTime = SimpleDateFormat("HH:mm a z")

        val formattedDate = sdfDate.format(date)
        val formattedTime = sdfTime.format(date)

        // Combine both with "at" in between
        _formattedDateTime.value = "$formattedDate at $formattedTime"
    }

    private fun setHelpType(visitLog: VisitLog) {
        val helpTypeList = mutableListOf<String>()
        if (visitLog.food_drink == "Y") helpTypeList.add("food/drink")
        if (visitLog.clothes == "Y") helpTypeList.add("clothes")
        if (visitLog.hygiene == "Y") helpTypeList.add("hygiene")
        if (visitLog.wellness == "Y") helpTypeList.add("wellness")
        if (visitLog.lawyerLegal == "Y") helpTypeList.add("lawyer/legal")
        if (visitLog.medicalhelp == "Y") helpTypeList.add("medical")
        if (visitLog.socialWorker == "Y") helpTypeList.add("social")
        if (visitLog.other == "Y") helpTypeList.add("other")

        _helpType.value = helpTypeList.joinToString(", ")
    }

    fun deleteVisitLog() {
        viewModelScope.launch {
            val id = _visitLog.value?.id
            if (id == null) {
                _deleteResult.value = false
                return@launch
            }

            val collections = listOf("VisitLogBook", "VisitLogBook_New")
            var allSuccess = true

            withContext(Dispatchers.IO) {
                collections.forEach { collection ->
                    try {
                        firestore.collection(collection).document(id).delete().await()
                    } catch (e: Exception) {
                        allSuccess = false
                    }
                }
            }

            _deleteResult.value = allSuccess
        }
    }

}