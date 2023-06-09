package org.brightmindenrichment.street_care.ui.community.data

import android.content.ContentValues
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.brightmindenrichment.street_care.util.Extensions
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


/**
// example addEvent
//eventDataAdapter.addEvent("Food for Androids", "Feed all the android of the world.", Date()) {
//    Log.d("BME", "Event added")
//}

//example setLiked
//eventDataAdapter.setLikedEvent("2r9Z5TKnQYFC96iMAB1i", true) {
//    Log.d("BME", "done")
//}

// example refresh
//eventDataAdapter.refresh {
//    for (event in this.eventDataAdapter.events) {
//        Log.d("BME", "${event.title} ${event.date} ${event.liked}")
//    }
//}
 * */


class EventDataAdapter {

    //var events: MutableList<Event> = mutableListOf()
    var communityData: MutableList<CommunityData> = mutableListOf()
    val storage = Firebase.storage
    val size: Int
        get() {
            return communityData.size
        }

    fun getEventAtPosition(position: Int): CommunityData? {

        if ((position >= 0) && (position < communityData.size)) {
            return communityData[position]
        }

        return null
    }

   fun setLikedEvent(eventId: String, doesLike: Boolean, onComplete: () -> Unit) {

        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return

        val db = Firebase.firestore
        if (doesLike) {  // add a record if liked

            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(user?.uid ?: "??")
            var profileImageUrl : String
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userData = document.data
                        if (userData != null) {
                            profileImageUrl = userData["profileImageUrl"].toString()
                            val likedData = hashMapOf(
                                "uid" to user.uid,
                                "eventId" to eventId,
                                "profileImageUrl" to profileImageUrl
                            )

                            db.collection("likedEvents").document().set(likedData).addOnSuccessListener {
                                Log.d("BME", "saved liked")
                                onComplete()
                            }
                        }
                    } else {
                        Log.d(ContentValues.TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    onComplete()
                    Log.d(ContentValues.TAG, "get failed with ", exception)
                }
            // create a map of the data to add to firebase

        } else {
            // delete record of the like of this event for this user
            db.collection("likedEvents").whereEqualTo("uid", user.uid)
                .whereEqualTo("eventId", eventId).get().addOnSuccessListener { result ->
                for (document in result) {
                    db.collection("likedEvents").document(document.id).delete()
                }
                onComplete()
            }
                .addOnFailureListener { exception ->
                    Log.w("BME", "Failed to save liked event ${exception.toString()}")
                    onComplete()
                }
        }
    }


    fun refresh(onComplete: () -> Unit) {
        // make sure somebody is logged in
        // val user = Firebase.auth.currentUser ?: return
        var prevMonth: String? = null
        var prevDay: String? = null
        val db = Firebase.firestore
        val query = db.collection("events").orderBy("date", Query.Direction.DESCENDING)
        query.get().addOnSuccessListener { result ->
                this.communityData.clear()
                for (document in result) {
                    var event = Event()
                    event.eventId = document.id
                    event.title = document.get("title")?.toString() ?: "Unknown"
                    event.description = document.get("description")?.toString() ?: "Unknown"
                    event.uid = document.get("uid").toString()
                    event.location = document.get("location")?.toString() ?: "Unknown"
                    event.time = document.get("time")?.toString() ?: "Unknown"

                    Log.d("Event date", "Event date"+event.date.toString())
                    val date:String = document.get("date")?.toString()  ?: "Unknown"
                    if(date != "Unknown"){
                        val secondsStartIndex = date.indexOf("seconds=") + 8
                        val secondsEndIndex = date.indexOf(",", secondsStartIndex)
                        val secondsString = date.substring(secondsStartIndex, secondsEndIndex)
                        val seconds = secondsString.toLong()

// Extract the nanoseconds value
                        val nanosecondsStartIndex = date.indexOf("nanoseconds=") + 12
                        val nanosecondsEndIndex = date.indexOf(")", nanosecondsStartIndex)
                        val nanosecondsString = date.substring(nanosecondsStartIndex, nanosecondsEndIndex)
                        val nanoseconds = nanosecondsString.toLong()
                        val instant = Instant.ofEpochSecond(seconds, nanoseconds.toLong())
// Convert the Instant to a LocalDateTime in the system default time zone
                        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
// Extract the month from the LocalDateTime
                        val month = localDateTime.month
                        val dayOfMonth = localDateTime.dayOfMonth.toString()
                        val dayOfWeek = localDateTime.dayOfWeek.toString()
                        val year = localDateTime.year
// Get the month name as a string
                        val monthName = month.toString()
// Extract the month and date
                        event.day = dayOfWeek.substring(0,3)
                        event.date = dayOfMonth
                        event.year = "$monthName $year"
                        if(prevMonth!=null){
                            if(!month.toString().equals(prevMonth)){
                                prevMonth = month.toString()
                                event.layoutType = Extensions.TYPE_NEW_DAY
                                var eventYear = EventYear()
                                eventYear.year = "$monthName $year"
                                var community = CommunityData(eventYear, Extensions.TYPE_MONTH)
                                this.communityData.add(community)
                            }
                            else{
                                if(dayOfMonth != prevDay){
                                    prevDay = dayOfMonth
                                    event.layoutType = Extensions.TYPE_NEW_DAY
                                }
                                else{
                                    event.layoutType = Extensions.TYPE_DAY
                                }
                            }
                        }
                        else{
                            prevMonth = month.toString()
                            prevDay = dayOfMonth
                            event.layoutType = Extensions.TYPE_NEW_DAY
                            var eventYear = EventYear()
                            eventYear.year = "$monthName $year"
                            var community = CommunityData(eventYear,Extensions.TYPE_MONTH)
                            this.communityData.add(community)
                        }
                        //this.events.add(event)
                        var communityEvent = CommunityData(event,event.layoutType!!)
                        this.communityData.add(communityEvent)

                    }

                }
                refreshedLiked{
                    onComplete()
                }

            }.addOnFailureListener { exception ->
                onComplete()
            }
    }


   private fun refreshedLiked(onComplete: () -> Unit) {

        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return

        val db = Firebase.firestore

        /*db.collection("likedEvents").whereEqualTo("uid", user.uid).get()
            .addOnSuccessListener { results ->

                for (document in results) {
                    for (event in this.events) {
                        if (event.eventId == document.get("eventId").toString()) {
                            event.liked = true
                        }
                    }
                }

                onComplete()
            }
            .addOnFailureListener { exceptioon ->
                onComplete()
            }*/
       db.collection("likedEvents").get()
           .addOnSuccessListener { results ->

               for (document in results) {
                   for(community in this.communityData){
                       community.event?.let{ event->
                           if(event.eventId == document.get("eventId").toString()){
                               if (user.uid==document.get("uid").toString()) {
                                   event.liked = true
                                   event.interest = event.interest?.minus(1)
                               }
                               else{
                                   if(event.itemList.size<3)
                                       event.addValue(document.get("profileImageUrl").toString())
                               }
                               event.addInterest()
                           }
                       }
                   }
               }
               onComplete()
           }
           .addOnFailureListener { exceptioon ->
               onComplete()
           }

    }

} // end class