package org.brightmindenrichment.street_care.ui.visit.details

import android.util.Log
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
        if (visitLog.food_drink == true) helpTypeList.add("food/drink")
        if (visitLog.clothes == true) helpTypeList.add("clothes")
        if (visitLog.hygiene == true) helpTypeList.add("hygiene")
        if (visitLog.wellness == true) helpTypeList.add("wellness")
        if (visitLog.lawyerLegal == true) helpTypeList.add("lawyer/legal")
        if (visitLog.medicalhelp == true) helpTypeList.add("medical")
        if (visitLog.socialWorker == true) helpTypeList.add("social")
        if (visitLog.other == true) helpTypeList.add("other")

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
                for (collection in collections) {
                    try {
                        val docRef = firestore.collection(collection).document(id)
                        val docSnapshot = docRef.get().await()

                        // Only delete if the document exists
                        if (docSnapshot.exists()) {
                            docRef.delete().await()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        allSuccess = false
                    }
                    Log.d("DeleteVisitLog", "Deleted from $collection")
                }
            }

            _deleteResult.value = allSuccess
        }

    }

}