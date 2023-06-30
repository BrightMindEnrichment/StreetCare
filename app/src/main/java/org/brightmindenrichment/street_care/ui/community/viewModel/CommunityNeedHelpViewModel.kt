package org.brightmindenrichment.street_care.ui.community.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityRequest

class CommunityNeedHelpViewModel : ViewModel() {
    private val _requestListLiveData = MutableLiveData<List<CommunityActivityRequest>>()
    // LiveData object to observe changes in the activities list
    val requestListLiveData: LiveData<List<CommunityActivityRequest>> get() = _requestListLiveData

    init {
        // for testing purpose
        _requestListLiveData.value = listOf(
            CommunityActivityRequest.Builder()
                .setTime("05/29/2023")
                .setDescription("First test item")
                .setTitle("First title")
                .build(),
            CommunityActivityRequest.Builder()
                .setTime("05/30/2023")
                .setDescription("Sec test item")
                .setTitle("Second title")
                .build(),
            CommunityActivityRequest.Builder()
                .setTime("05/29/2023")
                .setDescription("Third test item")
                .setTitle("Third title")
                .build()
        )
    }

    fun updateActivity(activity: List<CommunityActivityRequest>) {
        _requestListLiveData.postValue(activity)
    }
}