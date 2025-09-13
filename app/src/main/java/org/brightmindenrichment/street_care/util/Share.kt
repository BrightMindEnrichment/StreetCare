package org.brightmindenrichment.street_care.util

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

object Share {
    fun shareEvent(context: Context, eventId: String?) {
        if (eventId.isNullOrBlank()) {
            Toast.makeText(context, "Event ID not found. Cannot share.", Toast.LENGTH_SHORT).show()
            return
        }

        val shareLink = "https://streetcarenow.org/outreachsignup/$eventId"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareLink)
        }

        try {
            context.startActivity(Intent.createChooser(shareIntent, "Share Event"))
        } catch (e: Exception) {
            Log.e("ShareEvent", "Error sharing event: ${e.message}")
            Toast.makeText(context, "No app found to share this event.", Toast.LENGTH_SHORT).show()
        }
    }
}