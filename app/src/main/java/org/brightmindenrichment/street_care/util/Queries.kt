package org.brightmindenrichment.street_care.util

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.util.Extensions.Companion.getDayInMilliSec
import java.util.Date

object Queries {
    /*
    val defaultQuery = Firebase.firestore
        .collection("events")
        .orderBy("date", Query.Direction.DESCENDING)
     */

    val defaultQuery = Firebase.firestore
        .collection("outreachEventsAndroid")
        .orderBy("eventDate", Query.Direction.DESCENDING)

    fun getPastEventsQuery(
        order: Query.Direction = Query.Direction.DESCENDING
    ): Query {
        val targetDay = Timestamp(Date(System.currentTimeMillis()))
        return Firebase.firestore
            .collection("outreachEventsAndroid")
            .whereLessThan("eventDate", targetDay)
            .orderBy("eventDate", order)
    }

    fun getUpcomingEventsQuery(
        order: Query.Direction = Query.Direction.ASCENDING
    ): Query {
        val targetDay = Timestamp(Date(System.currentTimeMillis()))
        return Firebase.firestore
            .collection("outreachEventsAndroid")
            .whereGreaterThanOrEqualTo("eventDate", targetDay)
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
                .collection("outreachEventsAndroid")
                .whereLessThan("eventDate", currDay)
                .whereGreaterThanOrEqualTo("eventDate", targetDate)
                .orderBy("eventDate", order)
        }
        else {
            Firebase.firestore
                .collection("outreachEventsAndroid")
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
                .collection("outreachEventsAndroid")
                .whereLessThan("eventDate", currDay)
                .whereLessThan("eventDate", targetDate)
                .orderBy("eventDate", order)
        }
        else {
            Firebase.firestore
                .collection("outreachEventsAndroid")
                .whereGreaterThanOrEqualTo("eventDate", currDay)
                .whereLessThan("eventDate", targetDate)
                .orderBy("eventDate", order)
        }
    }

}