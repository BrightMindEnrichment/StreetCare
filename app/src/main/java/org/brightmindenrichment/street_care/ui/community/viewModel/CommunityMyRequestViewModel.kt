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
    private val query = db.collection("communityRequest")

    private fun loadRequests() {
        val uid = Firebase.auth.uid
        // Query documents


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
                val data = query.get().await().documents.mapNotNull { data ->
                    data.toObject(CommunityActivityRequest::class.java).also {
                        it?.let {
                            it.setDocId(data.id)
                        }
                    }
                }
                requestListLiveData.postValue(data)
            }catch (exception: Exception){
                Log.w(TAG, "Error getting documents: ", exception)
            }

        }
    }
    private fun updateActivity(activity: List<CommunityActivityRequest>) {
        requestListLiveData.postValue(activity)
    }


    fun deleteItem(request: CommunityActivityRequest) {
        viewModelScope.launch {
            try {
                val id = request.getDocId()
                if(id == null){
                    Log.w(TAG,"The Object has no Id")
                }else{
                    query.document(id).delete().await()
                    deleteLiveData(request)
                }
            }catch(exception: Exception){
                Log.w(TAG, "Error getting documents: ", exception)
            }
        }
    }

    private fun deleteLiveData(request: CommunityActivityRequest) {
        val list = requestListLiveData.value ?: return
        val newList = list.filter {
            it!=request
        }
        updateActivity(newList)
    }
}