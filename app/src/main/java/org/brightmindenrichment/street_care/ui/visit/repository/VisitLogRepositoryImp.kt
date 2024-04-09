package org.brightmindenrichment.street_care.ui.visit.repository


import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import java.time.LocalDate
import java.util.Calendar
import java.util.Date


//class VisitLogRepositoryImp : VisitLogRepository {
//
//    var visits: MutableList<VisitLog> = mutableListOf()
//    val visit = VisitLog()
//
//    override fun saveVisitLog(visitLog: VisitLog) {
//        // make sure somebody is logged in
//        val user = Firebase.auth.currentUser ?: return
//        Log.d("BME", user.uid)
//        // create a map of event data so we can add to firebase
//        val visitData = hashMapOf(
//            "whereVisit" to visitLog.location,
//            "whenVisit" to visitLog.date,
//            "whenVisitTime" to visitLog.whenVisitTime,
//            "numberOfHelpers" to visitLog.peopleCount,
//            "names(opt)" to visitLog.names,
////            "food_drink" to visitLog.food_drink,
////            "clothes" to visitLog.clothes,
////            "hygine" to visitLog.hygine,
////            "wellness" to visitLog.wellness,
//            //"other" to visitLog.other,
//            "otherDetail" to visitLog.otherDetail,
//            "comments" to visitLog.comments,
//            "rating" to visitLog.experience,
//            "visitAgain" to visitLog.visitAgain,
//            "peopleHelped" to visitLog.peopleHelped,
//            "outreach" to visitLog.outreach,
//            "share" to visitLog.share,
//            "uid" to user.uid,
//
//            "FollowUpDate" to visitLog.followupDate,
//            "HelpNames" to visitLog.addnames,
//            "HelpAddress" to visitLog.address,
////            "HelpFoodDrink" to visitLog.add_food_drink,
////            "HelpClothes" to visitLog.add_clothes,
////            "HelpHygine" to visitLog.add_hygine,
////            "HelpWellness" to visitLog.add_wellness,
////            "HelpMedical" to visitLog.add_medicalhelp,
////            "HelpSocial" to visitLog.add_socialWorker,
////            "HelpLegal" to visitLog.add_lawyerLegal,
//            "HelpOther" to visitLog.add_other,
//            "HelpTime" to visitLog.helpTime,
//            "HelpOtherDetail" to visitLog.add_otherDetail,
//            "AdditionalVolunteerNotes" to visitLog.add_volunteerDetail,
//            "number_of_items_donated" to visitLog.number_of_items,
//            "WhatGiven" to visitLog.whattogive,
//            "WhatRequired" to visitLog.whatrequired
//        )
//        // save to firebase
//        val db = Firebase.firestore
//        db.collection("VisitLogBook").add(visitData).addOnSuccessListener { documentReference ->
//            Log.d("BME", "Saved with id ${documentReference.id}")
//            //onComplete()
//        }.addOnFailureListener { exception ->
//            Log.w("BMR", "Error in Save VisitLog ${exception.toString()}")
//            //onComplete()
//        }
//    }
//
//
//    override fun loadVisitLogs(onComplete: () -> Unit) {
//        // make sure somebody is logged in
//        val user = Firebase.auth.currentUser ?: return
//        //val user = Firebase.auth.currentUser
//        Log.d("BME", user.uid)
//        val db = Firebase.firestore
//        db.collection("VisitLogBook").whereEqualTo("uid", user.uid).get()
//            .addOnSuccessListener { result ->
//                // we are going to reload the whole list, remove anything already cached
//                this.visits.clear()
//                for (document in result) {
//                    val visit = VisitLog()
//                    visit.location = document.get("whereVisit").toString()
//                    visit.visitAgain = document.get("volunteerAgain").toString()
//                    visit.peopleCount = document.get("numberOfHelpers") as Long
//                    visit.experience = document.get("rating").toString()
//                    visit.comments = document.get("comments").toString()
//                    visit.date = document.get("whenVisit") as Date
//                    visit.names = document.get("names(opt)").toString()
//                    if (document.get("date") != null) {
//                        val dt = document.get("date") as com.google.firebase.Timestamp
//                        if (dt != null) {
//                            visit.date = dt.toDate()
//                        }
//                    }
//                    this.visits.add(visit)
//                }
//                onComplete()
//            }.addOnFailureListener { exception ->
//                Log.w("BMR", "Error in Add VisitLog ${exception.toString()}")
//                //onComplete()
//                db.collection("VisitLogBook").whereEqualTo("share", true).get()
//                    .addOnSuccessListener { result ->
//
//                        // we are going to reload the whole list, remove anything already cached
//                        this.visits.clear()
//
//                        for (document in result) {
//                            val visit = VisitLog()
//
//                            visit.location = document.get("location").toString()
//                            //visit.hours = document.get("hoursSpentOnOutreach") as Long
//                            visit.visitAgain = document.get("willPerformOutreachAgain").toString()
//                            visit.peopleCount = document.get("helpers") as Long
//                            visit.experience = document.get("rating").toString()
//                            visit.comments = document.get("comments").toString()
//
//                            if (document.get("date") != null) {
//                                val dt = document.get("date") as com.google.firebase.Timestamp
//                                if (dt != null) {
//                                    visit.date = dt.toDate()
//                                }
//                            }
//
//                            this.visits.add(visit)
//                        }
//
//                        onComplete()
//
//                    }
//            }
//    }// end of class
//}

class VisitLogRepositoryImp : VisitLogRepository {

    var visits: MutableList<VisitLog> = mutableListOf()
    val visit = VisitLog()

    override fun saveVisitLog(visitLog: VisitLog) {
        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return
        Log.d("BME", user.uid)

        //Adding code to fix Date and Time issue in whenVisit and andWhenVisitTime
        //var tempDate: Date = visitLog.date
        //visitLog.date
        //val splitTemp = temp.split(" ")
        //val fixedDate = splitTemp[0] +" "+ splitTemp[1] +" "+ splitTemp[2] +" "+ visitLog.whenVisitTime.toString().uppercase() +" "+ splitTemp[4] +" "+ splitTemp[5]
        //visitLog.fixedDate = fixedDate

        // create a map of event data so we can add to firebase
        val visitData = hashMapOf(

            "whenVisit" to visitLog.date,
            "whenVisitTime" to visitLog.whenVisitTime,
            "NumberOfPeopleHelped" to visitLog.peopleCount,
            "PeopleHelpedDescription" to visitLog.names,
            "rating" to visitLog.experience,
            "share" to visitLog.share,
            "uid" to user.uid,
            "number_of_items_donated" to visitLog.number_of_items,
            "WhatGiven" to visitLog.whattogive,
            "Location" to visitLog.locationmap,
            "Type" to visitLog.typeofdevice
        )
        // save to firebase
        val db = Firebase.firestore
        db.collection("interimPersonalVisitLog").add(visitData).addOnSuccessListener { documentReference ->
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
        db.collection("interimPersonalVisitLog").whereEqualTo("uid", user.uid).get()
            .addOnSuccessListener { result ->
                // we are going to reload the whole list, remove anything already cached
                this.visits.clear()
                for (document in result) {
                    val visit = VisitLog()
//                    visit.location = document.get("whereVisit").toString()
//                    visit.visitAgain = document.get("volunteerAgain").toString()
//                    visit.peopleCount = document.get("numberOfHelpers") as Long
//                    visit.experience = document.get("rating").toString()
//                    visit.comments = document.get("comments").toString()
//                    visit.date = document.get("whenVisit") as Date
//                    visit.names = document.get("names(opt)").toString()

                    visit.date = document.get("time") as Date

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
                    this.visits.add(visit)
                }
                onComplete()
            }.addOnFailureListener { exception ->
                Log.w("BMR", "Error in Add VisitLog ${exception.toString()}")
                //onComplete()
                db.collection("interimPersonalVisitLog").whereEqualTo("share", true).get()
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
                            if(document.get("whenVisit")!=null)
                            {
                                val d = document.get("whenVisit") as com.google.firebase.Timestamp
                                if (d != null) {
                                    visit.date = d.toDate()
                                }
                            }

                            this.visits.add(visit)
                        }

                        onComplete()

                    }
            }
    }// end of class
}



