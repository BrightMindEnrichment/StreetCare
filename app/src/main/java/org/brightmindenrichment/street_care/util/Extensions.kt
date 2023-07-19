package org.brightmindenrichment.street_care.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class Extensions {
    companion object{
        fun floatToLong (value : Float) : Long{
            return value.toLong()
        }

        fun dateToString(date: Date?, format: String): String {
            val dateFormat = SimpleDateFormat(format, Locale.US)
            return dateFormat.format(date)
        }


        fun showDialog(context : Context, title: String, message : String, textPositivebtn : String) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(textPositivebtn, DialogInterface.OnClickListener { dialog, _ ->
                    dialog.cancel()
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