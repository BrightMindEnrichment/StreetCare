package org.brightmindenrichment.street_care.ui.visit

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.ui.visit.data.Status
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

    fun refreshAll(onComplete: () -> Unit) {
        val user = Firebase.auth.currentUser ?: return
        val db = Firebase.firestore

        val allVisits = mutableListOf<VisitLog>()
        totalPeopleCount = 0
        totalItemsDonated = 0
        var completedFetches = 0

        fun checkAndFinish() {
            if (completedFetches == 3) {
                visits.clear()
                visits.addAll(allVisits.sortedByDescending { it.date })
                onComplete()
            }
        }

        val processDocuments: (QuerySnapshot) -> Unit = { result ->
            for (document in result) {
                val visit = VisitLog()
                visit.id = document.id

                try {
                    (document.get("whenVisit") as? com.google.firebase.Timestamp)?.toDate()?.let {
                        visit.date = it
                    }
                    //datetime

                    (document.get("itemQty") as? Long)?.let {
                        totalItemsDonated += it
                    }


                    (document.get("peopleHelped") as? Long)?.let {
                        visit.peopleCount = it
                        totalPeopleCount += it
                    }
                    // numberPeopleHelped

                    (document.get("rating") as? Long)?.let {
                        visit.experience = it.toInt()
                    }
                    // rating

                    visit.whereVisit = (document.get("whereVisit") as? String) ?: run {
                        val loc = document.get("Location") as? Map<*, *>
                        val street = loc?.get("street") as? String ?: ""
                        val city = loc?.get("city") as? String ?: ""
                        val state = loc?.get("state") as? String ?: ""
                        val zip = loc?.get("zipcode") as? String ?: ""
                        listOf(street, city, state, zip).filter { it.isNotBlank() }.joinToString(", ")
                    }
                    // street city state zipcode

                    visit.food_drink = document.get("foodAndDrinks") as? String ?: ""
                    visit.clothes = document.get("clothes") as? String ?: ""
                    visit.hygiene = document.get("hygiene") as? String ?: ""
                    visit.wellness = document.get("wellness") as? String ?: ""
                    visit.lawyerLegal = document.get("legal") as? String ?: ""
                    visit.medicalhelp = document.get("medical") as? String ?: ""
                    visit.socialWorker = document.get("social") as? String ?: ""
                    visit.other = document.get("other") as? String ?: ""
                    // whatGiven
                    // Food and Drink
                    // Clothes
                    // Hygiene Products
                    // Wellness/ Emotional Support
                    // Medical Help
                    // Social Worker /Psychiatrist
                    // Legal/Lawyer

                    // Count "Y" flags
                    if (visit.clothes == "Y") totalItemsDonated++
                    if (visit.food_drink == "Y") totalItemsDonated++
                    if (visit.hygiene == "Y") totalItemsDonated++
                    if (visit.wellness == "Y") totalItemsDonated++
                    if (visit.other == "Y") totalItemsDonated++

                    allVisits.add(visit)

                } catch (e: Exception) {
                    Log.e("VisitLog", "Error parsing document ${document.id}: $e")
                }
            }
        }

        val processDocumentsPublic: (QuerySnapshot) -> Unit = { result ->
            for (document in result) {
                try {
                    (document.get("dateTime") as? com.google.firebase.Timestamp)?.toDate()?.time?.let { time ->
                        allVisits
                            .filter { visit -> visit.date.time == time }
                            .map { visit ->
                            val isPublic = (document.get("isPublic") as? String)?.contentEquals("true") == true
                            if (isPublic) {
                                (document.get("status") as? String)?.let { status ->
                                    visit.status = when(status.lowercase())  {
                                        "pending" -> Status.PENDING
                                        "approved" -> Status.PUBLISHED
                                        "rejected" -> Status.REJECTED
                                        else -> Status.PRIVATE
                                    }
                                }
                            }
                        }
                    }

                } catch (e: Exception) {
                    Log.e("VisitLog", "Error parsing document ${document.id}: $e")
                }
            }
        }

        // First fetch
        db.collection("VisitLogBook")
            .whereEqualTo("uid", user.uid)
            .get()
            .addOnSuccessListener { result ->
                processDocuments(result)
                completedFetches++
                checkAndFinish()
            }
            .addOnFailureListener {
                Log.w("VisitLog", "Failed to fetch VisitLogBook: $it")
                completedFetches++
                checkAndFinish()
            }

        // Second fetch
        db.collection("VisitLogBook_New")
            .whereEqualTo("uid", user.uid)
            .get()
            .addOnSuccessListener { result ->
                processDocuments(result)
                completedFetches++
                checkAndFinish()
            }
            .addOnFailureListener {
                Log.w("VisitLog", "Failed to fetch VisitLogBook_New: $it")
                completedFetches++
                checkAndFinish()
            }

        // Third fetch
        db.collection("visitLogWebProd")
            .whereEqualTo("uid", user.uid)
            .get()
            .addOnSuccessListener { result ->
                processDocumentsPublic(result)
                completedFetches++
                checkAndFinish()
            }
            .addOnFailureListener {
                Log.w("VisitLog", "Failed to fetch visitLogWebProd: $it")
                completedFetches++
                checkAndFinish()
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
        "ratingNotes" to comments,
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