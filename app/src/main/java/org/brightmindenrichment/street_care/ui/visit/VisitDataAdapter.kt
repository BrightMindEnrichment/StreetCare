package org.brightmindenrichment.street_care.ui.visit

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
//    fun refresh(onComplete: () -> Unit) {
//        // make sure somebody is logged in
//        val user = Firebase.auth.currentUser ?: return
//        Log.d("BME", user.uid)
//        val db = Firebase.firestore
//        db.collection("VisitLogBook").whereEqualTo("uid", user.uid).get()
//            .addOnSuccessListener { result ->
//                // we are going to reload the whole list, remove anything already cached
//                this.visits.clear()
//                totalPeopleCount = 0
//                totalItemsDonated = 0
//                try {
//                    for (document in result) {
//                        var visit = VisitLog()
//                        visit.id = document.id
//
//                        if (document.get("whenVisit") != null) {
//                            val d = document.get("whenVisit") as com.google.firebase.Timestamp
//                            visit.date = d.toDate()
//                        }
//                        if (document.get("itemQty") != null) {
//                            val num = document.get("itemQty")
//                            totalItemsDonated += num as Long
//                        }
//                        if (document.get("peopleHelped") != null) {
//                            val pcount = document.get("peopleHelped")
//                            totalPeopleCount += pcount as Long
//                            visit.peopleCount = totalPeopleCount
//                        }
//
//                        if (document.get("rating") != null) {
//                            val rating = document.get("rating") as? Long
//                            visit.experience = rating?.toInt() ?: 0
//                        }
//
//                        val location = document.get("Location") as? Map<*, *>
//                        val street = location?.get("street") as? String ?: ""
//                        val city = location?.get("city") as? String ?: ""
//                        val state = location?.get("state") as? String ?: ""
//                        val zipcode = location?.get("zipcode") as? String ?: ""
//
//                        visit.whereVisit = listOf(street, city, state, zipcode)
//                            .filter { it.isNotBlank() }
//                            .joinToString(", ")
//
//                        if(document.get("whereVisit")!=null) {
//                            visit.whereVisit = document.get("whereVisit") as String
//                        }
//
//                        if (document.get("foodAndDrinks") != null) {
//                            visit.food_drink = document.get("foodAndDrinks") as String
//                        }
//                        if (document.get("clothes") != null) {
//                            visit.clothes = document.get("clothes") as String
//                        }
//                        if (document.get("hygiene") != null) {
//                            visit.hygiene = document.get("hygiene") as String
//                        }
//                        if (document.get("wellness") != null) {
//                            visit.wellness = document.get("wellness") as String
//                        }
//
//                        if (document.get("legal") != null) {
//                            visit.lawyerLegal = document.get("legal") as String
//                        }
//
//                        if (document.get("medical") != null) {
//                            visit.medicalhelp = document.get("medical") as String
//                        }
//
//                        if (document.get("social") != null) {
//                            visit.socialWorker = document.get("social") as String
//                        }
//
//                        if (document.get("other") != null) {
//                            visit.other = document.get("other") as String
//                        }
//
//                        // need to cchek in the array list
//                        if (visit.clothes == "Y") totalItemsDonated++
//                        if (visit.food_drink == "Y") totalItemsDonated++
//                        if (visit.hygiene == "Y") totalItemsDonated++
//                        if (visit.wellness == "Y") totalItemsDonated++
//                        if (visit.other == "Y") totalItemsDonated++
//
//                        this.visits.add(visit)
//                    }
//                } catch (e: Exception) {
//                    Log.e(TAG, e.toString())
//                }
//                this.visits.sortByDescending { it.date }
//                onComplete()
//            }.addOnFailureListener { exception ->
//                Log.w("BMR", "Error in addEvent ${exception.toString()}")
//                onComplete()
//            }
//    }

    fun refreshAll(onComplete: () -> Unit) {
        val user = Firebase.auth.currentUser ?: return
        val db = Firebase.firestore

        val allVisits = mutableListOf<VisitLog>()
        totalPeopleCount = 0
        totalItemsDonated = 0
        var completedFetches = 0

        fun checkAndFinish() {
            if (completedFetches == 2) {
                visits.clear()
                visits.addAll(allVisits.sortedByDescending { it.date })
//                totalPeopleCount = totalPeopleCount
//                totalItemsDonated = totalItemsDonated
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

                    (document.get("itemQty") as? Long)?.let {
                        totalItemsDonated += it
                    }

                    (document.get("peopleHelped") as? Long)?.let {
                        visit.peopleCount = it
                        totalPeopleCount += it
                    }

                    (document.get("rating") as? Long)?.let {
                        visit.experience = it.toInt()
                    }

                    visit.whereVisit = (document.get("whereVisit") as? String) ?: run {
                        val loc = document.get("Location") as? Map<*, *>
                        val street = loc?.get("street") as? String ?: ""
                        val city = loc?.get("city") as? String ?: ""
                        val state = loc?.get("state") as? String ?: ""
                        val zip = loc?.get("zipcode") as? String ?: ""
                        listOf(street, city, state, zip).filter { it.isNotBlank() }.joinToString(", ")
                    }

                    visit.food_drink = document.get("foodAndDrinks") as? String ?: ""
                    visit.clothes = document.get("clothes") as? String ?: ""
                    visit.hygiene = document.get("hygiene") as? String ?: ""
                    visit.wellness = document.get("wellness") as? String ?: ""
                    visit.lawyerLegal = document.get("legal") as? String ?: ""
                    visit.medicalhelp = document.get("medical") as? String ?: ""
                    visit.socialWorker = document.get("social") as? String ?: ""
                    visit.other = document.get("other") as? String ?: ""

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
    }


    fun refresh2(onComplete: () -> Unit) {
        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return
        Log.d("BME", user.uid)
        val db = Firebase.firestore
        db.collection("VisitLogBook_New").whereEqualTo("uid", user.uid).get()
            .addOnSuccessListener { result ->
                // we are going to reload the whole list, remove anything already cached
                this.visits.clear()
                totalPeopleCount = 0
                totalItemsDonated = 0
                try {
                    for (document in result) {
                        var visit = VisitLog()
                        visit.id = document.id

                        if (document.get("whenVisit") != null) {
                            val d = document.get("whenVisit") as com.google.firebase.Timestamp
                            visit.date = d.toDate()
                        }
                        if (document.get("itemQty") != null) {
                            val num = document.get("itemQty")
                            totalItemsDonated += num as Long
                        }

                        if (document.get("peopleHelped") != null) {
                            val pcount = document.get("peopleHelped")
                            totalPeopleCount += pcount as Long
                            visit.peopleCount = totalPeopleCount
                        }

                        if (document.get("rating") != null) {
                            val rating = document.get("rating") as? Long
                            visit.experience = rating?.toInt() ?: 0
                        }

                        if(document.get("whereVisit")!=null) {
                            visit.whereVisit = document.get("whereVisit") as String
                        }

                        if (document.get("foodAndDrinks") != null) {
                            visit.food_drink = document.get("foodAndDrinks") as String
                        }
                        if (document.get("clothes") != null) {
                            visit.clothes = document.get("clothes") as String
                        }
                        if (document.get("hygiene") != null) {
                            visit.hygiene = document.get("hygiene") as String
                        }
                        if (document.get("wellness") != null) {
                            visit.wellness = document.get("wellness") as String
                        }

                        if (document.get("legal") != null) {
                            visit.lawyerLegal = document.get("legal") as String
                        }

                        if (document.get("medical") != null) {
                            visit.medicalhelp = document.get("medical") as String
                        }

                        if (document.get("social") != null) {
                            visit.socialWorker = document.get("social") as String
                        }

                        if (document.get("other") != null) {
                            visit.other = document.get("other") as String
                        }

                        // need to cchek in the array list
                        if (visit.clothes == "Y") totalItemsDonated++
                        if (visit.food_drink == "Y") totalItemsDonated++
                        if (visit.hygiene == "Y") totalItemsDonated++
                        if (visit.wellness == "Y") totalItemsDonated++
                        if (visit.other == "Y") totalItemsDonated++

                        this.visits.add(visit)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
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