package org.brightmindenrichment.street_care.util

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import org.brightmindenrichment.street_care.R

object Share {
    fun shareEvent(context: Context, eventId: String?) {
        if (eventId.isNullOrBlank()) {
            Toast.makeText(context, context.getString(R.string.share_event_id_missing), Toast.LENGTH_SHORT).show()
            return
        }

        val shareLink = "https://streetcarenow.org/outreachsignup/$eventId"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareLink)
        }

        try {
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_event_title)))
        } catch (e: Exception) {
            Log.e("ShareEvent", "Error sharing event: ${e.message}")
            Toast.makeText(context, context.getString(R.string.share_event_error), Toast.LENGTH_SHORT).show()
        }
    }
}