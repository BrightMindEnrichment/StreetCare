package org.brightmindenrichment.street_care.ui.community.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityHelp

class CommunityWantHelpViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val _activitiesLiveData = MutableLiveData<List<CommunityActivityHelp>>()
    // LiveData object to observe changes in the activities list
    val activitiesLiveData: LiveData<List<CommunityActivityHelp>> get() = _activitiesLiveData

    init {
        // for testing purpose
        _activitiesLiveData.value = listOf(
            CommunityActivityHelp.Builder()
                .setTime("05/29/2023")
                .setDescription("First test item")
                .setTitle("First title")
                .build(),
            CommunityActivityHelp.Builder()
                .setTime("05/30/2023")
                .setDescription("Sec test item")
                .setTitle("Second title")
                .build(),
            CommunityActivityHelp.Builder()
                .setTime("05/29/2023")
                .setDescription("Third test item")
                .setTitle("Third title")
                .build()
        )
    }

    fun updateActivity(activity: List<CommunityActivityHelp>) {
        _activitiesLiveData.postValue(activity)
    }
}