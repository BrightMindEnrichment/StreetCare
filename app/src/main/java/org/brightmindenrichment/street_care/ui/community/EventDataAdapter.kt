package org.brightmindenrichment.street_care.ui.community

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.sql.Timestamp
import java.util.*


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

    var events: MutableList<Event> = mutableListOf()

    val size: Int
        get() {
            return events.size
        }

    fun getEventAtPosition(position: Int): Event? {

        if ((position >= 0) && (position < events.size)) {
            return events[position]
        }

        return null
    }

  /*  fun setLikedEvent(eventId: String, doesLike: Boolean, onComplete: () -> Unit) {

        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return

        val db = Firebase.firestore
        if (doesLike) {  // add a record if liked

            // create a map of the data to add to firebase
            val likedData = hashMapOf(
                "uid" to user.uid,
                "eventId" to eventId
            )

            db.collection("likedEvents").document().set(likedData).addOnSuccessListener {
                Log.d("BME", "saved liked")
                onComplete()
            }
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
*/

    fun refresh(onComplete: () -> Unit) {
        // make sure somebody is logged in
        // val user = Firebase.auth.currentUser ?: return
        val db = Firebase.firestore
        db.collection("events").whereEqualTo("status", "Approved").get()
            .addOnSuccessListener { result ->
                this.events.clear()
                for (document in result) {
                    var event = Event()
                    event.eventId = document.id
                    event.title = document.get("title").toString()
                    event.description = document.get("description").toString()
                    event.uid = document.get("uid").toString()
                    event.location = document.get("location").toString()
                    event.time = document.get("time").toString()
                    event.date = document.get("date").toString()
                    this.events.add(event)
                }

             //   refreshedLiked {
                   onComplete()
              //  }
            }.addOnFailureListener { exceptioon ->
                onComplete()
            }
    }


   /* private fun refreshedLiked(onComplete: () -> Unit) {

        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return

        val db = Firebase.firestore

        db.collection("likedEvents").whereEqualTo("uid", user.uid).get()
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
            }

    }*/



} // end class