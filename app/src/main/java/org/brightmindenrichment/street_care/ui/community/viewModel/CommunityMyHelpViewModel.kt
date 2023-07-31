package org.brightmindenrichment.street_care.ui.community.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityHelp

private const val TAG = "CommunityMyHelpVM"
class CommunityMyHelpViewModel : ViewModel() {
    val helpListLiveData : MutableLiveData<List<CommunityActivityHelp>> by lazy {
        MutableLiveData<List<CommunityActivityHelp>>().also {
            loadMyHelp()
        }
    }
    // LiveData object to observe changes in the activities list
    private val db = Firebase.firestore
//
    fun updateActivity(activity: List<CommunityActivityHelp>) {
        helpListLiveData.postValue(activity)
    }
    private fun loadMyHelp() {
        val uid = Firebase.auth.uid
        // Query documents
        val query = db.collection("communityHelp")

        query.whereEqualTo("uid", uid).limit(50)
            .get()
            .addOnSuccessListener { result ->

                // Map documents to data list
                val data = result.documents.mapNotNull {
                    it.toObject(CommunityActivityHelp::class.java)
                }
                // Update LiveData
                helpListLiveData.postValue(data)

            }.addOnFailureListener { exception ->
                // Handle error
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }
    fun deleteItem(item: CommunityActivityHelp){
        val list = helpListLiveData.value ?: return
        val updatedList = list.filter { it != item }
        helpListLiveData.value = updatedList
    }
}