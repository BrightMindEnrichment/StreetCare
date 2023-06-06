package org.brightmindenrichment.street_care.ui.community.viewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityObject

class CommunityViewModel : ViewModel() {


    private val _activitiesLiveData = MutableLiveData<List<CommunityActivityObject>>()
    // LiveData object to observe changes in the activities list
    val activitiesLiveData : LiveData<List<CommunityActivityObject>> get() = _activitiesLiveData
    init {
        //for testing purpose
        _activitiesLiveData.value = (listOf(
            CommunityActivityObject.Builder()
                .setTime("05/29/2023").setDescription("First test item")
                .build(),
            CommunityActivityObject.Builder()
                .setTime("05/30/2023").setDescription("Sec test item")
                .build(),
            CommunityActivityObject.Builder()
                .setTime("05/29/2023").setDescription("Third test item")
                .build()
        )
        )
    }
    fun updateActivity(activity: List<CommunityActivityObject>) {
        _activitiesLiveData.postValue(activity)
    }

}
//    // List of events
//    private val eventsList = mutableListOf<Event>()
//
//    // LiveData object to observe changes in the events list
//    val eventsLiveData = MutableLiveData<List<Event>>()
//
//    // List of activities
//    // Function to add an event to the list
//    fun addEvent(event: Event) {
//        eventsList.add(event)
//        eventsLiveData.value = eventsList
//    }
//
//    // Function to search events by name
//    fun searchEventsByName(name: String) {
//        val filteredEvents = eventsList.filter { it.name.contains(name, true) }
//        eventsLiveData.value = filteredEvents
//    }
//    // Function to add an activity to the list