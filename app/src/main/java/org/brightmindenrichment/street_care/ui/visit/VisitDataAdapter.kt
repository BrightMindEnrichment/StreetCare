package org.brightmindenrichment.street_care.ui.visit

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
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
        val usersDocRef = db.collection("users").document(user.uid)
        usersDocRef.get().addOnSuccessListener { document ->
            val status = when (document.getString("Type")) {
                "Chapter Leader", "Street Care Hub Leader" -> "approved"
                else -> "pending"
            }

            val newCollection = db.collection("VisitLogBook_New")
            val oldCollection = db.collection("VisitLogBook")

            val allVisits = mutableListOf<VisitLog>()
            totalPeopleCount = 0
            totalItemsDonated = 0

            Log.d("Migration", "Fetching old docs for UID: ${user.uid}")
            oldCollection.whereEqualTo("uid", user.uid).get()
                .addOnSuccessListener { oldSnapshot ->
                    Log.d("Migration", "Old collection fetched. Count: ${oldSnapshot.size()}")
                    if (oldSnapshot.isEmpty) {
                        // No old data, just load new
                        Log.d("Migration", "No old collection data for UID: ${user.uid}")
                        fetchAndDisplayNewCollection(newCollection, user.uid, allVisits, onComplete)
                    } else {
                        newCollection.whereEqualTo("uid", user.uid).get()
                            .addOnSuccessListener { newSnapshot ->
                                val newIds = newSnapshot.documents.map { it.id }.toSet()

                                val batch = db.batch()
                                val oldVisits = mutableListOf<VisitLog>()

                                // Use your custom logic to convert old records
                                for (document in oldSnapshot) {
                                    if (!newIds.contains(document.id)) {
                                        val visit = convertOldDocument(document)
                                        if (visit != null) {
                                            val docRef = newCollection.document(document.id)

                                            val visitData = hashMapOf(
                                                "whenVisit" to visit.date, //1
                                                "whereVisit" to visit.whereVisit, //2
                                                "locationDescription" to visit.locationDescription, //2
                                                "peopleHelped" to visit.peopleCount, //3
                                                "peopleHelpedDescription" to visit.peopleHelpedDescription, //3
                                                "foodAndDrinks" to visit.food_drink, //4
                                                "clothes" to visit.clothes, //4
                                                "hygiene" to visit.hygiene, //4
                                                "wellness" to visit.wellness, //4
                                                "medical" to visit.medicalhelp, //4
                                                "social" to visit.socialWorker, //4
                                                "legal" to visit.lawyerLegal, //4
                                                "other" to visit.other, //4
                                                "whatGiven" to visit.whattogive, //4
                                                "otherNotes" to visit.otherDetail, //4
                                                "itemQty" to visit.number_of_items, //5
                                                "itemQtyDescription" to visit.itemQtyDescription, //5
                                                "rating" to visit.experience, //6
                                                "ratingNotes" to visit.comments, //6

                                                "durationHours" to visit.visitedHours, //1A
                                                "durationMinutes" to visit.visitedMinutes, //1A
                                                "numberOfHelpers" to visit.whoJoined, //2A
                                                "numberOfHelpersComment" to visit.numberOfHelpersComment, //2A
                                                "peopleNeedFurtherHelp" to visit.stillNeedSupport, //3A
                                                "peopleNeedFurtherHelpComment" to visit.supportTypeNeeded, //3A
                                                "peopleNeedFurtherHelpLocation" to visit.peopleNeedFurtherHelpLocation, //3A
                                                "furtherFoodAndDrinks" to visit.add_food_drink, //4A
                                                "furtherClothes" to visit.add_clothes, //4A
                                                "furtherHygiene" to visit.add_hygine, //4A
                                                "furtherWellness" to visit.add_wellness, //4A
                                                "furtherMedical" to visit.add_medicalhelp, //4A
                                                "furtherSocial" to visit.add_socialWorker, //4A
                                                "furtherLegal" to visit.add_lawyerLegal, //4A
                                                "furtherOther" to visit.add_other, //4A
                                                "furtherOtherNotes" to visit.add_otherDetail, //4A
                                                "whatGivenFurther" to visit.whatrequired, //4A
                                                "followUpWhenVisit" to visit.followupDate, //5A
                                                "futureNotes" to visit.futureNotes, //6A
                                                "volunteerAgain" to visit.visitAgain, //7A

                                                "lastEdited" to visit.lastEditedTime,
                                                "type" to visit.typeofdevice,
                                                "timeStamp" to visit.createdTime,
                                                "uid" to user.uid,
                                                "isPublic" to visit.share,
                                                "isFlagged" to visit.isFlagged,
                                                "flaggedByUser" to visit.flaggedByUser,
                                                "status" to status
                                            )

                                            batch.set(docRef, visitData)
                                            oldVisits.add(visit)
                                            //Delete the records from old Collection
//                                            val oldDocRef = oldCollection.document(document.id)
//                                            batch.delete(oldDocRef)
                                        }
                                    }
                                }

                                // Save and fetch only from new
                                batch.commit().addOnSuccessListener {
                                    fetchAndDisplayNewCollection(
                                        newCollection,
                                        user.uid,
                                        allVisits,
                                        onComplete
                                    )
                                }.addOnFailureListener {
                                    Log.w("VisitLog", "Migration failed: ${it.message}")
                                    fetchAndDisplayNewCollection(
                                        newCollection,
                                        user.uid,
                                        allVisits,
                                        onComplete
                                    )
                                }
                            }
                    }
                }
                .addOnFailureListener {
                    Log.w("VisitLog", "Failed to fetch VisitLogBook: $it")
                    fetchAndDisplayNewCollection(newCollection, user.uid, allVisits, onComplete)
                }
        }
    }

    //Get data from VisitLogBook collection, convert to new data format
    private fun convertOldDocument(document: DocumentSnapshot): VisitLog? {
        return try {
            val visit = VisitLog()
            visit.id = document.id
            Log.d("Migration", "Fetching old docs for document: ${document.id}")
            val platformType = document.getString("Type")
            visit.typeofdevice = platformType ?: "iOS"
            val isIOS = platformType == null || !platformType.equals("Android", ignoreCase = true)
            //1
            (document.get("whenVisit") as? Timestamp)?.toDate()?.let { visit.date = it }
            //2
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
            //2
            visit.locationDescription = document.getString("locationDescription") ?: ""
            //3
            visit.peopleCount = if (isIOS) {
                document.get("peopleHelped") as? Long ?: 0L
            } else {
                document.get("NumberOfPeopleHelped") as? Long ?: 0L
            }
            //3
            visit.peopleHelpedDescription = document.getString("PeopleHelpedDescription") ?: ""
            //4
            val foodDrinkBoolean = when {
                isIOS && document.get("foodAndDrinks") is Boolean -> {
                    document.getBoolean("foodAndDrinks") ?: false
                }
                !isIOS && document.get("FoodAndDrinks") is Boolean -> {
                    document.getBoolean("FoodAndDrinks") ?: false
                }
                document.get("food_drink") is String -> {
                    val value = document.getString("food_drink")
                    value.equals("Y", ignoreCase = true)
                }
                else -> false
            }
            visit.food_drink = foodDrinkBoolean
            //4
            visit.clothes = when {
                document.get("clothes") is Boolean -> document.getBoolean("clothes") ?: false
                document.get("Clothes") is Boolean -> document.getBoolean("Clothes") ?: false
                document.get("clothes") is String -> {
                    val str = document.getString("clothes")
                    str.equals("Y", ignoreCase = true)
                }
                else -> false
            }
            //4
            visit.hygiene = when {
                document.get("hygine") is Boolean -> document.getBoolean("hygine") ?: false
                document.get("Hygiene") is Boolean -> document.getBoolean("Hygiene") ?: false
                document.get("hygine") is String -> {
                    val str = document.getString("hygine")
                    str.equals("Y", ignoreCase = true)
                }
                else -> false
            }
            //4
            visit.wellness = when {
                document.get("wellness") is Boolean -> document.getBoolean("wellness") ?: false
                document.get("Wellness") is Boolean -> document.getBoolean("Wellness") ?: false
                document.get("wellness") is String -> {
                    val str = document.getString("wellness")
                    str.equals("Y", ignoreCase = true)
                }
                else -> false
            }
            //4
            visit.medicalhelp = when {
                document.get("Medical") is Boolean -> document.getBoolean("Medical") ?: false
                document.get("medicalhelp") is String -> {
                    val str = document.getString("medicalhelp")
                    str.equals("Y", ignoreCase = true)
                }
                else -> false
            }
            //4
            visit.socialWorker = when {
                document.get("SocialWorker") is Boolean -> document.getBoolean("SocialWorker") ?: false
                document.get("social") is String -> {
                    val str = document.getString("social")
                    str.equals("Y", ignoreCase = true)
                }
                else -> false
            }
            //4
            visit.lawyerLegal = when {
                document.get("LawyerLegal") is Boolean -> document.getBoolean("LawyerLegal") ?: false
                document.get("lawyerLegal") is String -> {
                    val str = document.getString("lawyerLegal")
                    str.equals("Y", ignoreCase = true)
                }
                else -> false
            }
            //4
            visit.other = when {
                document.get("other") is Boolean -> document.getBoolean("other") ?: false
                document.get("OtherChecked") is Boolean -> document.getBoolean("OtherChecked") ?: false
                document.get("other") is String -> {
                    val str = document.getString("other")
                    str.equals("Y", ignoreCase = true)
                }
                else -> false
            }
            //4
            val otherDetailValue = listOf("otherNotes", "OtherDescription")
                .asSequence()
                .mapNotNull { key -> document.getString(key) }
                .firstOrNull { it.isNotBlank() }
                ?: ""
            visit.otherDetail = otherDetailValue
            //4
            if (visit.food_drink) visit.whattogive.add("Food and Drink")
            if (visit.clothes) visit.whattogive.add("Clothes")
            if (visit.hygiene) visit.whattogive.add("Hygiene Products")
            if (visit.wellness) visit.whattogive.add("Wellness/ Emotional Support")
            if (visit.medicalhelp) visit.whattogive.add("Medical Help")
            if (visit.socialWorker) visit.whattogive.add("Social Worker/ Psychiatrist")
            if (visit.lawyerLegal) visit.whattogive.add("Legal/ Lawyer")
            if (visit.other) {
                if (visit.otherDetail.isNotBlank()) {
                    visit.whattogive.add(visit.otherDetail)
                }
            }
            //5
            visit.number_of_items = if (isIOS) {
                document.get("itemQty") as? Long ?: 0L
            } else {
                document.get("number_of_items_donated") as? Long ?: 0L
            }
            //5
            visit.itemQtyDescription = document.getString("notes") ?: ""
            //6
            visit.experience = (document.get("rating") as? Number)?.toInt() ?: 0
            //6
            visit.comments = if (isIOS) {
                document.getString("ratingNotes") ?: ""
            } else {
                document.getString("comments") ?: ""
            }
            //1A
            if (isIOS) {
                val hours = (document.get("durationHours") as? Number)?.toInt() ?: 0
                val minutes = (document.get("durationMinutes") as? Number)?.toInt() ?: 0
                visit.visitedHours = hours
                visit.visitedMinutes = minutes
                visit.helpTime = "$hours hr, $minutes min"
            } else {
                val hours = (document.get("total_hours_spent") as? Number)?.toInt() ?: 0
                val minutes = (document.get("total_minutes_spent") as? Number)?.toInt() ?: 0
                visit.visitedHours = hours
                visit.visitedMinutes = minutes
                visit.helpTime = "$hours hr, $minutes min"
            }
            //2A
            visit.whoJoined = if(isIOS) {
                (document.get("numberOfHelpers") as? Long ?: 0L).toInt()
            }
            else {
                (document.get("whoJoined") as? Long ?: 0L).toInt()
            }
            //2A
            visit.numberOfHelpersComment = document.getString("numberOfHelpersComment") ?: ""
            //3A
            visit.stillNeedSupport = if (isIOS) {
                (document.get("peopleNeedFurtherHelp") as? Long ?: 0L).toInt()
            } else {
                (document.get("stillNeedSupport") as? Long ?: 0L).toInt()
            }
            //3A
            visit.supportTypeNeeded = document.getString("supportTypeNeeded") ?: ""
            //3A
            visit.peopleNeedFurtherHelpLocation = document.getString("peopleNeedFurtherHelpLocation") ?: ""
            //4A
            visit.add_food_drink = document.getBoolean("furtherFoodAndDrinks") ?: false
            visit.add_clothes = document.getBoolean("furtherClothes") ?: false
            visit.add_hygine = if(isIOS) {
                document.getBoolean("furtherHygine") ?: false
            } else {
                document.getBoolean("furtherHygiene") ?: false
            }
            visit.add_wellness = document.getBoolean("furtherWellness") ?: false
            visit.add_medicalhelp = if(isIOS) {
                document.getBoolean("furthermedical") ?: false
            } else {
                document.getBoolean("furtherMedical") ?: false
            }
            visit.add_socialWorker = if(isIOS) {
                document.getBoolean("furthersocialworker") ?: false
            } else {
                document.getBoolean("furtherSocial") ?: false
            }
            visit.add_lawyerLegal = if(isIOS) {
                document.getBoolean("furtherlegal") ?: false
            } else {
                document.getBoolean("furtherLegal") ?: false
            }
            visit.add_other = document.getBoolean("furtherOther") ?: false
            visit.add_otherDetail = document.getString("furtherOtherNotes") ?: ""
            if (visit.add_food_drink) visit.whatrequired.add("Food and Drink")
            if (visit.add_clothes) visit.whatrequired.add("Clothes")
            if (visit.add_hygine) visit.whatrequired.add("Hygiene Products")
            if (visit.add_wellness) visit.whatrequired.add("Wellness/ Emotional Support")
            if (visit.add_medicalhelp) visit.whatrequired.add("Medical Help")
            if (visit.add_socialWorker) visit.whatrequired.add("Social Worker/ Psychiatrist")
            if (visit.add_lawyerLegal) visit.whatrequired.add("Legal/ Lawyer")
            if (visit.add_other) {
                if (visit.add_otherDetail.isNotBlank()) {
                    visit.whatrequired.add(visit.add_otherDetail)
                }
            }
            //5A
            val timestamp = if (isIOS) {
                document.get("followUpWhenVisit") as? Timestamp
            } else {
                document.get("followupDate") as? Timestamp
            }
            visit.followupDate = timestamp?.toDate() ?: Date()
            //6A
            visit.futureNotes = if (isIOS) {
                document.get("futureNotes")?.toString() ?: "NA"
            } else {
                document.get("future_notes")?.toString() ?: "NA"
            }
            //7A
            visit.visitAgain = if (isIOS) {
                when (val value = document.get("volunteerAgain")) {
                    is Long -> when (value) {
                        0L -> "No"
                        1L -> "Yes"
                        2L -> "Maybe"
                        else -> "NA"
                    }
                    is String -> value
                    else -> ""
                }
            } else {
                when (val value = document.get("visitAgain")) {
                    is Long -> when (value) {
                        0L -> "No"
                        1L -> "Yes"
                        2L -> "Maybe"
                        else -> "NA"
                    }
                    is String -> value
                    else -> ""
                }
            }
            visit.lastEditedTime = document.getTimestamp("timestamp")?.toDate() ?: Date()
            visit.createdTime = document.getTimestamp("timestamp")?.toDate() ?: Date()
            visit.share = document.getBoolean("share") ?: false
            visit.isFlagged = false
            visit.flaggedByUser = ""

            visit
        } catch (e: Exception) {
            Log.e("VisitLog", "Conversion error for ${document.id}: $e")
            null
        }
    }


    private fun fetchAndDisplayNewCollection(
        collection: CollectionReference,
        uid: String,
        allVisits: MutableList<VisitLog>,
        onComplete: () -> Unit
    ) {
        collection.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val visit = VisitLog()
                    visit.id = document.id

                    try {
                        //Q1
                        (document.get("whenVisit") as? Timestamp)?.toDate()?.let {
                            visit.date = it
                        }
                        //Q2
                        visit.whereVisit = document.get("whereVisit").toString()
                        visit.locationDescription = document.get("locationDescription").toString()
                        //Q3
                        (document.get("peopleHelped") as? Number)?.toLong()?.let {
                            visit.peopleCount = it
                            totalPeopleCount += it
                        }
                        visit.peopleHelpedDescription = document.get("peopleHelpedDescription").toString()
                        //Q4
                        visit.food_drink = document.get("foodAndDrinks") == true
                        visit.clothes = document.get("clothes")  == true
                        visit.hygiene = document.get("hygiene")  == true
                        visit.wellness = document.get("wellness")  == true
                        visit.lawyerLegal = document.get("legal")  == true
                        visit.medicalhelp = document.get("medical")  == true
                        visit.socialWorker = document.get("social")  == true
                        visit.other = document.get("other")  == true
                        visit.otherDetail = document.get("otherNotes").toString()
                        (document.get("whatGiven") as? List<*>)?.let { list ->
                            visit.whatrequired = ArrayList(list.filterIsInstance<String>().filter { it.isNotBlank() })
                        }
                        if (visit.clothes) totalItemsDonated++
                        if (visit.food_drink) totalItemsDonated++
                        if (visit.hygiene) totalItemsDonated++
                        if (visit.wellness) totalItemsDonated++
                        if (visit.lawyerLegal) totalItemsDonated++
                        if (visit.medicalhelp) totalItemsDonated++
                        if (visit.socialWorker) totalItemsDonated++
                        if (visit.otherDetail.isNotBlank()) totalItemsDonated++
                        //Q5
                        (document.get("itemQty") as? Number)?.toLong()?.let {
                            totalItemsDonated += it
                        }
                        visit.itemQtyDescription = document.get("itemQtyDescription").toString()
                        //Q6
                        visit.experience = (document.get("rating") as? Number)?.toInt() ?: 0
                        visit.comments = document.get("ratingNotes").toString()
                        //Additional Q1
                        visit.visitedHours = (document.get("durationHours") as? Number)?.toInt() ?: 0
                        visit.visitedMinutes = (document.get("durationMinutes") as? Number)?.toInt() ?: 0
                        //Additional Q2
                        visit.whoJoined = (document.get("numberOfHelpers") as? Number)?.toInt() ?: 0
                        visit.numberOfHelpersComment = document.get("numberOfHelpersComment").toString()
                        //Additional Q3
                        visit.stillNeedSupport = (document.get("peopleNeedFurtherHelp") as? Number)?.toInt() ?: 0
                        visit.supportTypeNeeded = document.get("peopleNeedFurtherHelpComment").toString()
                        visit.peopleNeedFurtherHelpLocation = document.get("peopleNeedFurtherHelpLocation").toString()
                        //Additional Q4
                        visit.add_food_drink = document.getBoolean("furtherFoodAndDrinks") == true
                        visit.add_clothes = document.getBoolean("furtherClothes")  == true
                        visit.add_hygine = document.getBoolean("furtherHygiene")  == true
                        visit.add_wellness = document.getBoolean("furtherWellness")  == true
                        visit.add_lawyerLegal = document.getBoolean("furtherLegal")  == true
                        visit.add_medicalhelp = document.getBoolean("furtherMedical")  == true
                        visit.add_socialWorker = document.getBoolean("furtherSocial")  == true
                        visit.add_other = document.getBoolean("furtherOther")  == true
                        visit.add_otherDetail = document.get("furtherOtherNotes").toString()
                        (document.get("whatGivenFurther") as? List<*>)?.let { list ->
                            visit.whatrequired = ArrayList(list.filterIsInstance<String>().filter { it.isNotBlank() })
                        }
                        //Additional Q5
                        visit.followupDate = (document.get("followUpWhenVisit") as? Timestamp)?.toDate() ?: Date()
                        //Additional Q6
                        visit.futureNotes = document.get("futureNotes").toString()
                        //Additional Q7
                        visit.visitAgain = document.get("volunteerAgain").toString()

                        visit.lastEditedTime = (document.get("lastEdited") as? Timestamp)?.toDate() ?: Date()
                        visit.typeofdevice = document.get("type").toString()
                        visit.share = document.get("isPublic") as Boolean
                        visit.isFlagged = document.get("isFlagged") as Boolean
                        visit.flaggedByUser = document.get("flaggedByUser").toString()
                        visit.status = document.get("status").toString()

                        allVisits.add(visit)

                    } catch (e: Exception) {
                        Log.e("VisitLog", "Error parsing document in fetch ${document.id}: $e")
                    }
                }

                visits.clear()
                visits.addAll(allVisits.sortedByDescending { it.date })
                onComplete()
            }
            .addOnFailureListener {
                Log.w("VisitLog", "Failed to fetch VisitLogBook_New: $it")
                onComplete()
            }
    }

    fun getPublicVisitLog(onComplete: () -> Unit) {
        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return
        Log.d("BME", user.uid)
        val db = Firebase.firestore
        db.collection("VisitLogBook_New").whereEqualTo("isPublic", true).get()
            .addOnSuccessListener { result ->
                // we are going to reload the whole list, remove anything already cached
                this.visits.clear()
                for (document in result) {
                    val visit = VisitLog()
                    visit.location = document.get("whereVisit").toString()
                    visit.userId = document.get("uid").toString()
                    if(document.get("whenVisit")!=null)
                    {
                        val d = document.get("whenVisit") as Timestamp
                        visit.date = d.toDate()
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
        Log.w("BMR", "Error in addEvent ${exception}")
        onComplete()
    }
}
// end class