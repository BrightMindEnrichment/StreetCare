package org.brightmindenrichment.street_care.ui.visit.repository


import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import java.util.*


class VisitLogRepositoryImp : VisitLogRepository {

    var visits: MutableList<VisitLog> = mutableListOf()
    val visit = VisitLog()

    override fun saveVisitLog(visitLog: VisitLog) {
        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return
        Log.d("BME", user.uid)
        // create a map of event data so we can add to firebase
        val visitData = hashMapOf(
            "whereVisit" to visitLog.location,
            "whenVisit" to visitLog.date,
            "numberOfHelpers" to visitLog.peopleCount,
            "names(opt)" to visitLog.names,
            "food_drink" to visitLog.food_drink,
            "clothes" to visitLog.clothes,
            "hygine" to visitLog.hygine,
            "wellness" to visitLog.wellness,
            "other" to visitLog.other,
            "otherDetail" to visitLog.otherDetail,
            "comments" to visitLog.comments,
            "rating" to visitLog.experience,
            "visitAgain" to visitLog.visitAgain,
            "peopleHelped" to visitLog.peopleHelped,
            "outreach" to visitLog.outreach,
            "uid" to user.uid
        )
        // save to firebase
        val db = Firebase.firestore
        db.collection("VisitLogBook").add(visitData).addOnSuccessListener { documentReference ->
            Log.d("BME", "Saved with id ${documentReference.id}")
            //onComplete()
        }.addOnFailureListener { exception ->
            Log.w("BMR", "Error in Save VisitLog ${exception.toString()}")
            //onComplete()
        }
    }


    override fun loadVisitLogs(onComplete: () -> Unit) {
        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return
        //val user = Firebase.auth.currentUser
        Log.d("BME", user.uid)
        val db = Firebase.firestore
        db.collection("VisitLogBook").whereEqualTo("uid", user.uid).get()
            .addOnSuccessListener { result ->
                // we are going to reload the whole list, remove anything already cached
                this.visits.clear()
                for (document in result) {
                    val visit = VisitLog()
                    visit.location = document.get("whereVisit").toString()
                    visit.visitAgain = document.get("volunteerAgain").toString()
                    visit.peopleCount = document.get("numberOfHelpers") as Long
                    visit.experience = document.get("rating").toString()
                    visit.comments = document.get("comments").toString()
                    visit.date = document.get("whenVisit") as Date
                    visit.names = document.get("names(opt)").toString()
                    if (document.get("date") != null) {
                        val dt = document.get("date") as com.google.firebase.Timestamp
                        if (dt != null) {
                            visit.date = dt.toDate()
                        }
                    }
                    this.visits.add(visit)
                }
                onComplete()
            }.addOnFailureListener { exception ->
                Log.w("BMR", "Error in Add VisitLog ${exception.toString()}")
                //onComplete()
                db.collection("VisitLogBook").whereEqualTo("share", true).get()
                    .addOnSuccessListener { result ->

                        // we are going to reload the whole list, remove anything already cached
                        this.visits.clear()

                        for (document in result) {
                            val visit = VisitLog()

                            visit.location = document.get("location").toString()
                            //visit.hours = document.get("hoursSpentOnOutreach") as Long
                            visit.visitAgain = document.get("willPerformOutreachAgain").toString()
                            visit.peopleCount = document.get("helpers") as Long
                            visit.experience = document.get("rating").toString()
                            visit.comments = document.get("comments").toString()

                            if (document.get("date") != null) {
                                val dt = document.get("date") as com.google.firebase.Timestamp
                                if (dt != null) {
                                    visit.date = dt.toDate()
                                }
                            }

                            this.visits.add(visit)
                        }

                        onComplete()

                    }
            }
    }// end of class
}

