package org.brightmindenrichment.street_care.ui.visit

import android.content.ContentValues
import android.util.Log
import androidx.core.text.isDigitsOnly
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
    private var totalPeopleCount: Long = 0
    private var totalItemsDonated: Long = 0
    val size: Int
        get() {
            return visits.size
        }
    val getTotalPeopleCount: Long
        get() {
            return totalPeopleCount
        }
    val getTotalItemsDonated: Long
        get() {
            return totalItemsDonated
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
        db.collection("interimPersonalVisitLog").whereEqualTo("uid", user.uid).get()
            .addOnSuccessListener { result ->
                // we are going to reload the whole list, remove anything already cached
                this.visits.clear()
                for (document in result) {
                    var visit = VisitLog()

                    visit.location = document.get("whereVisit").toString()
                    visit.date = document.getTimestamp("whenVisit")!!.toDate()
                    visit.peopleCount = document.get("numberOfHelpers") as Long

                    var temp=document.get("rating").toString()
                    
                    if (temp.isNotBlank() and temp.isDigitsOnly()){
                        visit.experience = Integer.parseInt(temp)
                    }else{
                        visit.experience = 0
                    }

                    visit.comments = document.get("comments").toString()
                    visit.names = document.get("names(opt)") .toString()
                    visit.clothes = document.get("clothes") .toString()
                    visit.food_drink = document.get("food_drink") .toString()
                    visit.hygine = document.get("hygine") .toString()
                    visit.wellness = document.get("wellness") .toString()
                    visit.other = document.get("other") .toString()

                    if (document.get("time") != null) {
                        val dt = document.get("time") as com.google.firebase.Timestamp
                        if (dt != null) {
                            visit.date = dt.toDate()
                        }
                    }
                    if(document.get("whenVisit")!=null)
                    {
                        val d = document.get("whenVisit") as com.google.firebase.Timestamp
                        if (d != null) {
                            visit.date = d.toDate()
                        }
                    }
                    if (document.get("number_of_items_donated") != null) {
                        val num = document.get("number_of_items_donated")
                        totalItemsDonated += num as Long
                    }
                    if (document.get("numberOfHelpers") != null) {
                        val pcount = document.get("numberOfHelpers")
                        totalPeopleCount += pcount as Long
                    }
                    if (document.get("NumberOfPeopleHelped") != null) {
                        val pcount1 = document.get("NumberOfPeopleHelped")
                        totalPeopleCount += pcount1 as Long
                    }

                    //totalPeopleCount += visit.peopleCount
                    // need to cchek in the array list
                    if(visit.clothes=="Y") totalItemsDonated++
                    if(visit.food_drink=="Y") totalItemsDonated++
                    if(visit.hygine=="Y") totalItemsDonated++
                    if(visit.wellness=="Y") totalItemsDonated++
                    if(visit.other=="Y") totalItemsDonated++
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
                    if(document.get("whenVisit")!=null)
                    {
                        val d = document.get("whenVisit") as com.google.firebase.Timestamp
                        if (d != null) {
                            visit.date = d.toDate()
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
    }
 // end class