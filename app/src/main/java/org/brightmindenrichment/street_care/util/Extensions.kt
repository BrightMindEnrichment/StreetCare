package org.brightmindenrichment.street_care.util

import android.R
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
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
    }





}