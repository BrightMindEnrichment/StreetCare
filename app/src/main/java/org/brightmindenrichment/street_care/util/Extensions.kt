package org.brightmindenrichment.street_care.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import java.text.SimpleDateFormat
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
    }





}