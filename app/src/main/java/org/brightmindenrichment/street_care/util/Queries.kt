package org.brightmindenrichment.street_care.util

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import org.brightmindenrichment.street_care.util.Extensions.Companion.getDayInMilliSec
import java.util.Date


object Queries {
    /*
    val defaultQuery = Firebase.firestore
        .collection("events")
        .orderBy("date", Query.Direction.DESCENDING)
     */

    val defaultQuery = Firebase.firestore
        .collection("outreachEvents")
        .orderBy("eventDate", Query.Direction.DESCENDING)

    fun getHelpRequestDefaultQuery(
        order: Query.Direction = Query.Direction.ASCENDING
    ): Query {
        return Firebase.firestore
            .collection("helpRequests")
            .orderBy("title", order)
    }

    fun getPastEventsQuery(
        order: Query.Direction = Query.Direction.DESCENDING
    ): Query {
        val targetDay = Timestamp(Date(System.currentTimeMillis()))
        return Firebase.firestore
            .collection("outreachEvents")
            .whereLessThan("eventDate", targetDay)
            .orderBy("eventDate", order)
    }


    fun getUpcomingEventsQuery(
        order: Query.Direction = Query.Direction.ASCENDING
    ): Query {
        val targetDay = Timestamp(Date(System.currentTimeMillis()))
        return Firebase.firestore
            .collection("outreachEvents")
            .whereGreaterThanOrEqualTo("eventDate", targetDay)
            .orderBy("eventDate", order)
    }

    fun getHelpRequestEventsQuery(
        order: Query.Direction = Query.Direction.ASCENDING,
        helpRequestId: String,
    ): Query {
        val targetDay = Timestamp(Date(System.currentTimeMillis()))
        return Firebase.firestore
            .collection("outreachEvents")
            .whereGreaterThanOrEqualTo("eventDate", targetDay)
            .whereArrayContains("helpRequest", helpRequestId)
            .orderBy("eventDate", order)
    }

    fun getLikedEventsQuery(order: Query.Direction = Query.Direction.ASCENDING): Query {
        val user = Firebase.auth.currentUser
        val userId= user?.uid.toString()
        return Firebase.firestore
            .collection("outreachEvents")
            .whereArrayContains("participants",userId)
            .orderBy("eventDate", order)
    }


    fun getQueryToFilterEventsAfterTargetDate(
        targetDate: Timestamp,
        isPastEvents: Boolean,
        order: Query.Direction = Query.Direction.DESCENDING,
    ): Query {
        val currDay = Timestamp(Date(System.currentTimeMillis()))
        return if(isPastEvents) {
            Firebase.firestore
                .collection("outreachEvents")
                .whereLessThan("eventDate", currDay)
                .whereGreaterThanOrEqualTo("eventDate", targetDate)
                .orderBy("eventDate", order)
        }
        else {
            Firebase.firestore
                .collection("outreachEvents")
                .whereGreaterThanOrEqualTo("eventDate", currDay)
                .whereGreaterThanOrEqualTo("eventDate", targetDate)
                .orderBy("eventDate", order)
        }
    }

    fun getQueryToFilterEventsBeforeTargetDate(
        targetDate: Timestamp,
        isPastEvents: Boolean,
        order: Query.Direction = Query.Direction.DESCENDING
    ): Query {
        val currDay = Timestamp(Date(System.currentTimeMillis()))
        return if(isPastEvents) {
            Firebase.firestore
                .collection("outreachEvents")
                .whereLessThan("eventDate", currDay)
                .whereLessThan("eventDate", targetDate)
                .orderBy("eventDate", order)
        }
        else {
            Firebase.firestore
                .collection("outreachEvents")
                .whereGreaterThanOrEqualTo("eventDate", currDay)
                .whereLessThan("eventDate", targetDate)
                .orderBy("eventDate", order)
        }
    }

}