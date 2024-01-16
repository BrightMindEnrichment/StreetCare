package org.brightmindenrichment.street_care.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class Extensions {
    companion object{

        fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
        fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
        fun convertTimestampToDate(timestamp: Timestamp): Date {
            return timestamp.toDate()
        }

        fun convertTimestampToMilliSec(timestamp: Timestamp): Long {
            return timestamp.toDate().time
        }

        fun getDayInMilliSec(days: Int): Long {
            return days.toLong() * 24 * 60 * 60 * 1000
        }

        fun getDateTimeFromTimestamp(s: Any?): String {
            if(s == null) return "Unknown date and time"
            val timestamp = s as? Timestamp ?: return "Unknown date and time"
            val netDate = timestamp.toDate()
            //Log.d("firebase", "timestamp: ${timestamp.toDate()}")
            return try {
                // Jan/10/2023 at 15:08 CST
                val sdf =
                    android.icu.text.SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm zzz", Locale.US)
                //val netDate = Date(timestamp.toString().toLong() * 1000)
                sdf.format(netDate)
            } catch (e: Exception) {
                e.toString()
            }
        }

        fun floatToLong (value : Float) : Long{
            return value.toLong()
        }

        fun dateToString(date: Date?, format: String): String {
            val dateFormat = SimpleDateFormat(format, Locale.US)
            return dateFormat.format(date)
        }


        fun  showDialog(
            context: Context,
            title: String,
            message: String,
            textPositive: String,
            Cancel: String
        ){
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(textPositive, DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                }
                )
            val alert = builder.create()
            alert.show()
        }

        fun dateParser(date: String): LocalDateTime? {
            if(date.length>12) {
                val secondsStartIndex = date.indexOf("seconds=") + 8
                val secondsEndIndex = date.indexOf(",", secondsStartIndex)
                val secondsString = date.substring(secondsStartIndex, secondsEndIndex)
                val seconds = secondsString.toLong()

// Extract the nanoseconds value
                val nanosecondsStartIndex = date.indexOf("nanoseconds=") + 12
                val nanosecondsEndIndex = date.indexOf(")", nanosecondsStartIndex)
                val nanosecondsString = date.substring(nanosecondsStartIndex, nanosecondsEndIndex)
                val nanoseconds = nanosecondsString.toLong()
                val instant = Instant.ofEpochSecond(seconds, nanoseconds.toLong())
// Convert the Instant to a LocalDateTime in the system default time zone
                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            }
            return null
        }

        const val TYPE_MONTH = 1
        const val TYPE_NEW_DAY = 2
        const val TYPE_DAY = 3
    }





}