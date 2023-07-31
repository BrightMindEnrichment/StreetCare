package org.brightmindenrichment.street_care.ui.community.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityRequest
private const val TAG = "CommunityMyRequestVM"
class CommunityMyRequestViewModel : ViewModel() {
    // LiveData object to observe changes in the activities list
    val requestListLiveData: MutableLiveData<List<CommunityActivityRequest>> by lazy {
        MutableLiveData<List<CommunityActivityRequest>>().also {
            loadRequests()
        }
    }
    private val db = Firebase.firestore
    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    private fun loadRequests() {
        val uid = Firebase.auth.uid
        // Query documents
        val query = db.collection("communityRequest")

        query.whereEqualTo("uid", uid).limit(50)
//        .get()
//        .addOnSuccessListener { result ->
//
//            // Map documents to data list
//            val data = result.documents.mapNotNull {
//                it.toObject(CommunityActivityRequest::class.java)
//
//            }
//
//            // Update LiveData
//            requestListLiveData.postValue(data)
//
//        }.addOnFailureListener { exception ->
//            // Handle error
//            Log.w(TAG, "Error getting documents: ", exception)
//        }
        viewModelScope.launch {
            try {
                val data = query.get().await().documents.mapNotNull {
                    it.toObject(CommunityActivityRequest::class.java)
                }
                requestListLiveData.postValue(data)
            }catch (exception: Exception){
                Log.w(TAG, "Error getting documents: ", exception)
            }

        }
    }
    fun updateActivity(activity: List<CommunityActivityRequest>) {
        requestListLiveData.postValue(activity)
    }
}