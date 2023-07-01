package org.brightmindenrichment.street_care.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import java.text.DateFormatSymbols
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

        const val TYPE_MONTH = 1
        const val TYPE_NEW_DAY = 2
        const val TYPE_DAY = 3
    }





}