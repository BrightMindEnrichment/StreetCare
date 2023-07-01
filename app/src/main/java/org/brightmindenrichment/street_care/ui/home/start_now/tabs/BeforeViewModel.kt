package org.brightmindenrichment.street_care.ui.home.start_now.tabs

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import org.brightmindenrichment.street_care.ui.home.data.BeforePageData


class BeforeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val docRef = db.collection("page_content").document("start_now_before")

    val _beforePageData: MutableLiveData<BeforePageData> by lazy {
        MutableLiveData<BeforePageData>().also {
               loadBeforePageData()
        }
    }
    val beforePageLiveData : LiveData<BeforePageData>   = _beforePageData
    private fun loadBeforePageData() {
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null) {
                 _beforePageData.value = BeforePageData (documentSnapshot.data?.get("para1") as String)
                _beforePageData.value?.addBeforePageContent("para2", documentSnapshot.data?.get("para2") as String)
                _beforePageData.value?.addBeforePageContent("para3", documentSnapshot.data?.get("para3") as String)
                _beforePageData.value?.addBeforePageContent("para4", documentSnapshot.data?.get("para4") as String)
                _beforePageData.value?.addBeforePageContent("para5", documentSnapshot.data?.get("para5") as String)
            }
        } .addOnFailureListener { exception ->
            Log.w("BME", "Failed to fetch data from Database ${exception.toString()}")
        }

    }

}