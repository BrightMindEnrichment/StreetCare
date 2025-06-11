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
import org.brightmindenrichment.street_care.util.Extensions
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

    private val _helpTypeFurther = MutableLiveData<String>()
    val helpTypeFurther: LiveData<String> = _helpTypeFurther

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult

    fun setVisitLog(log: VisitLog) {
        _visitLog.value = log
        formatDate(log.date)
        formatDateTime(log.date)
        setHelpType(log)
        setHelpTypeFurther(log)
    }

    private fun formatDate(date: Date?) {
        val sdf = SimpleDateFormat("dd MMM yyyy")
        _formattedDate.value = sdf.format(date)
    }

    private fun formatDateTime(date: Date?) {
        val sdfDate = SimpleDateFormat("dd MMM yyyy")
        val sdfTime = SimpleDateFormat("HH:mm a z")

        val formattedDate = sdfDate.format(date)
        val formattedTime = sdfTime.format(date)

        // Combine both with "at" in between
        _formattedDateTime.value = "$formattedDate at $formattedTime"
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


    private fun setHelpTypeFurther(visitLog: VisitLog) {
        val helpTypeFurtherList = mutableListOf<String>()
        if (visitLog.add_food_drink == true) helpTypeFurtherList.add("food/drink")
        if (visitLog.add_clothes == true) helpTypeFurtherList.add("clothes")
        if (visitLog.add_hygine == true) helpTypeFurtherList.add("hygiene")
        if (visitLog.add_wellness == true) helpTypeFurtherList.add("wellness")
        if (visitLog.add_lawyerLegal == true) helpTypeFurtherList.add("lawyer/legal")
        if (visitLog.add_medicalhelp == true) helpTypeFurtherList.add("medical")
        if (visitLog.add_socialWorker == true) helpTypeFurtherList.add("social")
        if (visitLog.add_other == true) helpTypeFurtherList.add("other")

        _helpTypeFurther.value = helpTypeFurtherList.joinToString(", ")
    }


    fun refreshVisitLog(visitId: String) {
        FirebaseFirestore.getInstance().collection("VisitLogBook_New")
            .document(visitId)
            .get()
            .addOnSuccessListener { document ->
                document.toObject(VisitLog::class.java)?.let { updated ->
                    Log.d("VisitLogDetailsVM", "Fetched fresh data: $updated")
                    setVisitLog(updated)  // ⬅️ Important to call this so formatted fields get updated too
                } ?: Log.e("VisitLogDetailsVM", "No data found")
            }
            .addOnFailureListener {
                Log.e("VisitLogDetailsVM", "Error getting document", it)
            }
    }



    fun updateWhenVisit(newDate: Date?) {
        val currentLog = _visitLog.value
        if (currentLog != null && newDate != null) {
            val updated = currentLog.copy(date = newDate)
            _visitLog.value = updated
            _formattedDateTime.value = Extensions.dateToString(newDate, "dd MMM yyyy 'at' hh:mm a z")
        }
    }



    fun updateWhereVisit(whereVisit: String) {
        val currentLog = _visitLog.value
        if (currentLog != null) {
            _visitLog.value = currentLog.copy(whereVisit = whereVisit.toString())
        }
    }


    fun updatePeopleHelped(newCount: Int) {
        val currentLog = _visitLog.value
        if (currentLog != null) {
            _visitLog.value = currentLog.copy(peopleCount = newCount.toLong())
        }
    }

    fun updateWhatGiven(whatGiven: String) {
        val currentLog = _visitLog.value
        if (currentLog != null) {
            _visitLog.value = currentLog.copy(whatGiven = whatGiven.toString())
        }
    }

    fun updateItemsDonated(numberOfItems: Int) {
        val currentLog = _visitLog.value
        if (currentLog != null) {
            _visitLog.value = currentLog.copy(number_of_items = numberOfItems.toLong())
        }
    }

    fun updateRating(rating: Int) {
        val currentLog = _visitLog.value
        if (currentLog != null) {
            _visitLog.value = currentLog.copy(experience = rating)
        }
    }

    fun updateHelpTime(helpTime: String) {
        val currentLog = _visitLog.value
        if (currentLog != null) {
            _visitLog.value = currentLog.copy(helpTime = helpTime.toString())
        }
    }

    fun updateWhoJoined(whoJoined: Int) {
        val currentLog = _visitLog.value
        if (currentLog != null) {
            _visitLog.value = currentLog.copy(whoJoined = whoJoined.toInt())
        }
    }


    fun updateStillNeedSupport(stillNeedSupport: Int) {
        val currentLog = _visitLog.value
        if (currentLog != null) {
            _visitLog.value = currentLog.copy(stillNeedSupport = stillNeedSupport)
        }
    }


    fun updateWhatGivenFurther(whatGivenFurther: String) {
        val currentLog = _visitLog.value
        if (currentLog != null) {
            _visitLog.value = currentLog.copy(whatGivenFurther = whatGivenFurther.toString())
        }
    }


    fun updateFollowupDate(newDate: Date?) {
        val currentLog = _visitLog.value
        if (currentLog != null && newDate != null) {
            val updated = currentLog.copy(followupDate = newDate)
            _visitLog.value = updated

        }
    }

    fun updateFutureNotes(futureNotes: String) {
        val currentLog = _visitLog.value
        if (currentLog != null) {
            _visitLog.value = currentLog.copy(futureNotes = futureNotes.toString())
        }
    }

    fun updateVisitAgain(visitAgain: String) {
        val currentLog = _visitLog.value
        if (currentLog != null) {
            _visitLog.value = currentLog.copy(visitAgain = visitAgain.toString())
        }
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