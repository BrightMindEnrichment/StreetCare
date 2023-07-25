package org.brightmindenrichment.street_care.ui.visit

import android.content.ContentValues
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import java.util.*

class VisitDataAdapter {

    var visits: MutableList<VisitLog> = mutableListOf()
    val size: Int
        get() {
            return visits.size
        }

    /***
     * Returns a visit log for a given position.
     *
     * If the position is not valid, null is returned.
     * */
    fun getVisitAtPosition(position: Int): VisitLog? {
        if ((position >= 0) && (position < visits.size)) {
            return visits[position]
        }
        return null
    }
    fun refresh(onComplete: () -> Unit) {
        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return
        Log.d("BME", user.uid)
        val db = Firebase.firestore
        db.collection("VisitLogBook").whereEqualTo("uid", user.uid).get()
            .addOnSuccessListener { result ->
                // we are going to reload the whole list, remove anything already cached
                this.visits.clear()
                for (document in result) {
                    var visit = VisitLog()
                    visit.location = document.get("whereVisit").toString()
                    visit.date = document.getTimestamp("whenVisit")!!.toDate()
                    visit.peopleCount = document.get("numberOfHelpers") as Long
                    visit.experience = document.get("rating").toString()
                    visit.comments = document.get("comments").toString()
                    visit.names = document.get("names(opt)") .toString()

                    if (document.get("whenVisit") != null) {
                        val dt = document.get("whenVisit") as com.google.firebase.Timestamp
                        if (dt != null) {
                            visit.date = dt.toDate()
                        }
                    }
                    this.visits.add(visit)
                }
               this.visits.sortByDescending { it.date }
                onComplete()
            }.addOnFailureListener { exception ->
                Log.w("BMR", "Error in addEvent ${exception.toString()}")
                onComplete()
            }
    }

    fun getPublicVisitLog(onComplete: () -> Unit) {
        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return
        Log.d("BME", user.uid)
        val db = Firebase.firestore
        db.collection("VisitLogBook").whereEqualTo("share", true).get()
            .addOnSuccessListener { result ->
                // we are going to reload the whole list, remove anything already cached
                this.visits.clear()
                for (document in result) {
                    var visit = VisitLog()
                    visit.location = document.get("whereVisit").toString()
                    visit.whenVisit = document.get("whenVisit").toString()
                    visit.userId = document.get("uid").toString()
                    if (document.get("date") != null) {
                        val dt = document.get("date") as com.google.firebase.Timestamp
                        if (dt != null) {
                            visit.date = dt.toDate()
                        }
                    }
                    if (visit.userId != user.uid) {
                        this.visits.add(visit)
                    }
                }
                this.visits.sortByDescending { it.date }
                onComplete()
            }
    }
}
/*
    fun addVisit(location: String, hours: Long, visitAgain: String, peopleCount: Long, experience: String, comments: String, date: Date, onComplete: () -> Unit) {

        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return

        // create a map of event data so we can add to firebase
        val visitData = hashMapOf(
            "location" to location,
            "hoursSpentOnOutreach" to hours,
            "willPerformOutreachAgain" to visitAgain,
            "helpers" to peopleCount,
            "rating" to experience,
            "comments" to comments,
            "uid" to user.uid
        )

        // save to firebase
        val db = Firebase.firestore
        db.collection("surveys").add(visitData).addOnSuccessListener { documentReference ->
            Log.d("BME", "Saved with id ${documentReference.id}")
            onComplete()
        } .addOnFailureListener { exception ->
            Log.w("BMR", "Error in addEvent ${exception.toString()}")
            onComplete()
        }
    }*/
 // end class