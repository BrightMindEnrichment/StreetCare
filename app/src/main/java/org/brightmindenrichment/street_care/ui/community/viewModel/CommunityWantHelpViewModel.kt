package org.brightmindenrichment.street_care.ui.community.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityHelp
private const val TAG = "CommunityHelpVM"
class CommunityWantHelpViewModel : ViewModel() {
//    private val _helpListLiveData = MutableLiveData<List<CommunityActivityHelp>>()
//    // LiveData object to observe changes in the activities list
//    val helpListLiveData: LiveData<List<CommunityActivityHelp>> get() = _helpListLiveData
    val helpListLiveData: MutableLiveData<List<CommunityActivityHelp>> by lazy {
        MutableLiveData<List<CommunityActivityHelp>>().also {
            loadList()
        }
    }
    private val db = Firebase.firestore

//    init {
//        // for testing purpose
//        helpListLiveData.value = listOf(
//            CommunityActivityHelp.Builder()
//                .setTime("05/29/2023")
//                .setDescription("First test item")
//                .setTitle("First title")
//                .build(),
//            CommunityActivityHelp.Builder()
//                .setTime("05/30/2023")
//                .setDescription("Sec test item")
//                .setTitle("Second title")
//                .build(),
//            CommunityActivityHelp.Builder()
//                .setTime("05/29/2023")
//                .setDescription("Third test item")
//                .setTitle("Third title")
//                .build()
//        )
//    }

     fun loadList() {
        db.collection("communityHelp")
            .get()
            .addOnSuccessListener { documents ->
                val list = ArrayList<CommunityActivityHelp>()

                for (document in documents) {
                    val myObject = document.toObject(CommunityActivityHelp::class.java)
                    list.add(myObject)
                }

                helpListLiveData.value = list
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun updateActivity(activity: List<CommunityActivityHelp>) {
        helpListLiveData.postValue(activity)
    }
}