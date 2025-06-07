package org.brightmindenrichment.street_care.ui.visit

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
                onComplete()
            }
        }


        val processOldDocuments: (QuerySnapshot) -> Unit = { result ->

            for (document in result) {
                val visit = VisitLog()
                visit.id = document.id

                try {

                    val platformType = document.getString("Type")
                    val isIOS = platformType == null || !platformType.equals("Android", ignoreCase = true)

                    //Q11
                    (document.get("whenVisit") as? com.google.firebase.Timestamp)?.toDate()?.let {
                        visit.date = it
                    }

                    //Q2
                    visit.whereVisit = if (isIOS) {

                        document.getString("whereVisit") ?: ""
                 } else {

                        val loc = document.get("Location") as? Map<*, *>
                        val street = loc?.get("street") as? String ?: ""
                        val city = loc?.get("city") as? String ?: ""
                        val state = loc?.get("state") as? String ?: ""
                        val zip = loc?.get("zipcode") as? String ?: ""
                        listOf(street, city, state, zip).filter { it.isNotBlank() }.joinToString(", ")

                    }


                    // Q3

                    visit.peopleCount = if (isIOS) {
                        // iOS-specific field
                        document.get("peopleHelped") as? Long ?: 0L

                    } else {
                        // Android-specific field
                        document.get("NumberOfPeopleHelped") as? Long ?: 0L
                    }

                    // Q5

                    visit.number_of_items = if (isIOS) {
                        // iOS-specific field
                        document.get("number_of_items_donated") as? Long ?: 0L
                    } else {
                        // Android-specific field
                        document.get("number_of_items_donated") as? Long ?: 0L
                    }


                    // Q6

                    visit.experience = if (isIOS) {
                        (document.get("rating") as? Long ?: 0L).toInt()
                    } else {
                        (document.get("rating") as? Long ?: 0L).toInt()
                    }


                    // Q7

                    var totalHoursSpent = "0"
                    var totalMinutesSpent = "0"

                    if (isIOS) {

                        val hours = document.get("durationHours") as? Long ?: 0L
                        val minutes = document.get("durationMinutes") as? Long ?: 0L

                        totalHoursSpent = hours.toString()
                        totalMinutesSpent = minutes.toString()

                        visit.visitedHours = hours.toInt()
                        visit.visitedMinutes = minutes.toInt()

                        visit.helpTime = "$totalHoursSpent hr, $totalMinutesSpent min"

                    } else {
                        if (document.get("total_hours_spent") != null) {
                            totalHoursSpent = document.get("total_hours_spent").toString()
                            visit.visitedHours = totalHoursSpent.toInt()
                        }

                        if (document.get("total_minutes_spent") != null) {
                            totalMinutesSpent = document.get("total_minutes_spent").toString()
                            visit.visitedMinutes = totalMinutesSpent.toInt()
                        }

                        visit.helpTime = "$totalHoursSpent hr, $totalMinutesSpent min"
                    }

                    // Q8

                    visit.whoJoined = if (isIOS) {
                        (document.get("numberOfHelpers") as? Long ?: 0L).toInt()

                    } else {
                        (document.get("whoJoined") as? Long ?: 0L).toInt()
                    }


                    // Q9

                    visit.stillNeedSupport = if (isIOS) {
                        // iOS-specific field
                        (document.get("peopleNeedFurtherHelp") as? Long ?: 0L).toInt()
                    } else {
                        // Android-specific field
                        (document.get("stillNeedSupport") as? Long ?: 0L).toInt()
                    }

                    // Q11

                    val timestamp = if (isIOS) {
                        document.get("followUpWhenVisit") as? com.google.firebase.Timestamp
                    } else {
                        document.get("followupDate") as? com.google.firebase.Timestamp
                    }

                    visit.followupDate = timestamp?.toDate() ?: Date() // fallback to current time if null


                    //Q12
                    visit.futureNotes = if (isIOS) {
                        document.get("otherNotes")?.toString() ?: "NA"
                    } else {
                        document.get("future_notes")?.toString() ?: "NA"
                    }


                    //Q13

                    visit.visitAgain = if (isIOS) {
                        when (document.get("volunteerAgain") as? Long) {
                            0L -> "No"
                            1L -> "Yes"
                            2L -> "Maybe"
                            else -> "NA"
                        }
                    } else {
                        document.get("visitAgain")?.toString() ?: "NA"
                    }


                    allVisits.add(visit)
                } catch (e: Exception) {
                    Log.e("VisitLog", "Error parsing OLD document ${document.id}: $e")
                }
            }
        }


        val processNewDocuments: (QuerySnapshot) -> Unit = { result ->

            for (document in result) {
                val visit = VisitLog()
                visit.id = document.id

                try {
                    // Q1
                    (document.get("whenVisit") as? com.google.firebase.Timestamp)?.toDate()?.let {
                        visit.date = it
                    }

                    // Q2
                    visit.whereVisit = document.getString("whereVisit") ?: ""

                    // Q3
                    (document.get("peopleHelped") as? Long)?.let {
                        visit.peopleCount = it
                        totalPeopleCount += it
                    }

                    // Q4
                    visit.food_drink = document.get("foodAndDrinks") as? String ?: ""
                    visit.clothes = document.get("clothes") as? String ?: ""
                    visit.hygiene = document.get("hygiene") as? String ?: ""
                    visit.wellness = document.get("wellness") as? String ?: ""
                    visit.lawyerLegal = document.get("legal") as? String ?: ""
                    visit.medicalhelp = document.get("medical") as? String ?: ""
                    visit.socialWorker = document.get("social") as? String ?: ""
                    visit.other = document.get("other") as? String ?: ""

                    (document.get("whatGiven") as? List<*>)?.let { list ->
                        visit.whatGiven = list.filterIsInstance<String>().joinToString(", ")
                    }

                    //Q5

                    (document.get("itemQty") as? Long)?.let {
                        visit.number_of_items = it
                        totalItemsDonated += it
                    }

                    //Q6

                    (document.get("rating") as? Long)?.let {
                        visit.experience = it.toInt()
                    }


                    //Q7

                    document.get("durationHours")?.toString()?.let {
                        visit.visitedHours = it.toInt()
                    }

                    document.get("durationMinutes")?.toString()?.let {
                        visit.visitedMinutes = it.toInt()
                    }

                    visit.helpTime = "${visit.visitedHours} hr, ${visit.visitedMinutes} min"


                    //Q8

                    (document.get("numberOfHelpers") as? Long)?.let {
                        visit.whoJoined = it.toInt()
                    }

                    //Q9
                    (document.get("peopleNeedFurtherHelp") as? Long)?.let {
                        visit.stillNeedSupport = it.toInt()
                    }

                    //Q10
                    (document.get("whatGivenFurther") as? List<*>)?.let { list ->
                        visit.whatGivenFurther = list.filterIsInstance<String>().joinToString(", ")
                    }

                    //Q11
                    (document.get("followUpWhenVisit") as? com.google.firebase.Timestamp)?.toDate()?.let {
                        visit.followupDate = it
                    }

                    //Q12
                    document.get("futureNotes")?.toString()?.let {
                        visit.futureNotes = it
                    }

                    //Q13
                    document.get("volunteerAgain")?.toString()?.let {
                        visit.visitAgain = it
                    }

                    // Count flags
                    if (visit.clothes == "Y") totalItemsDonated++
                    if (visit.food_drink == "Y") totalItemsDonated++
                    if (visit.hygiene == "Y") totalItemsDonated++
                    if (visit.wellness == "Y") totalItemsDonated++
                    if (visit.other == "Y") totalItemsDonated++

                    allVisits.add(visit)

                } catch (e: Exception) {
                    Log.e("VisitLog", "Error parsing NEW document ${document.id}: $e")
                }
            }
        }



        // First fetch
        db.collection("VisitLogBook")
            .whereEqualTo("uid", user.uid)
            .get()
            .addOnSuccessListener { result ->
                processOldDocuments(result)
                completedFetches++
                checkAndFinish()
            }
            .addOnFailureListener {
                Log.w("VisitLog", "Failed to fetch VisitLogBook: $it")
                completedFetches++
                checkAndFinish()
            }

        db.collection("VisitLogBook_New")
            .whereEqualTo("uid", user.uid)
            .get()
            .addOnSuccessListener { result ->
                processNewDocuments(result)
                completedFetches++
                checkAndFinish()
            }
            .addOnFailureListener {
                Log.w("VisitLog", "Failed to fetch VisitLogBook_New: $it")
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