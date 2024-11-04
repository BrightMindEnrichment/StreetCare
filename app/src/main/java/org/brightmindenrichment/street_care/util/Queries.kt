package org.brightmindenrichment.street_care.util

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import org.brightmindenrichment.street_care.ui.community.data.Event
import org.brightmindenrichment.street_care.util.Extensions.Companion.getDayInMilliSec
import java.util.Date


object Queries {
    /*
    val defaultQuery = Firebase.firestore
        .collection("events")
        .orderBy("date", Query.Direction.DESCENDING)
     */

    val defaultQuery = Firebase.firestore
        .collection("outreachEventsDev")
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
            .collection("outreachEventsDev")
            .whereLessThan("eventDate", targetDay)
            .orderBy("eventDate", order)
    }


    fun getUpcomingEventsQuery(
        order: Query.Direction = Query.Direction.ASCENDING
    ): Query {
        val targetDay = Timestamp(Date(System.currentTimeMillis()))
        return Firebase.firestore
            .collection("outreachEventsDev")
            .whereGreaterThanOrEqualTo("eventDate", targetDay)
            .orderBy("eventDate", order)
    }

    fun getHelpRequestEventsQuery(
        order: Query.Direction = Query.Direction.ASCENDING,
        helpRequestId: String,
    ): Query {
        val targetDay = Timestamp(Date(System.currentTimeMillis()))
        return Firebase.firestore
            .collection("outreachEventsDev")
            .whereGreaterThanOrEqualTo("eventDate", targetDay)
            .whereArrayContains("helpRequest", helpRequestId)
            .orderBy("eventDate", order)
    }

    fun getLikedEventsQuery(order: Query.Direction = Query.Direction.ASCENDING): Query {
        val user = Firebase.auth.currentUser
        val userId= user?.uid.toString()
        return Firebase.firestore
            .collection("outreachEventsDev")
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
                .collection("outreachEventsDev")
                .whereLessThan("eventDate", currDay)
                .whereGreaterThanOrEqualTo("eventDate", targetDate)
                .orderBy("eventDate", order)
        }
        else {
            Firebase.firestore
                .collection("outreachEventsDev")
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
                .collection("outreachEventsDev")
                .whereLessThan("eventDate", currDay)
                .whereLessThan("eventDate", targetDate)
                .orderBy("eventDate", order)
        }
        else {
            Firebase.firestore
                .collection("outreachEventsDev")
                .whereGreaterThanOrEqualTo("eventDate", currDay)
                .whereLessThan("eventDate", targetDate)
                .orderBy("eventDate", order)
        }
    }

    fun getQueryToFilterPastEventsByType(skill: String, isPastEvents: Boolean, order: Query.Direction = Query.Direction.DESCENDING) : Query{
        val targetDay = Timestamp(Date(System.currentTimeMillis()))

        return Firebase.firestore
                .collection("outreachEventsDev")
                .whereLessThan("eventDate", targetDay)
                .whereArrayContains("skills",skill)

    }

    fun getQueryToFilterFutureEventsByType(skill: String, isPastEvents: Boolean, order: Query.Direction = Query.Direction.ASCENDING) : Query{
        val targetDay = Timestamp(Date(System.currentTimeMillis()))
        return Firebase.firestore
                .collection("outreachEventsDev")
                .whereGreaterThanOrEqualTo("eventDate", targetDay)
                .whereArrayContains("skills",skill)
                .orderBy("eventDate", order)
    }

    fun getQueryToFilterHelpRequestsByType(skill: String, helpRequestId: String, order: Query.Direction = Query.Direction.ASCENDING): Query{
        val targetDay = Timestamp(Date(System.currentTimeMillis()))
        return Firebase.firestore
            .collection("helpRequests")
            .whereArrayContains("skills", skill)
            .orderBy("title", Query.Direction.ASCENDING)
    }

}