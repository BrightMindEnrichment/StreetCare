package org.brightmindenrichment.street_care

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.isNotEmpty().let {
            // Check if the message contains data payload.
            if (it) {
                // Handle the data payload here.
                handleEventNotification(remoteMessage.data)
            } else {
                // Handle the notification payload.
                remoteMessage.notification?.let {
                    showNotification(it.body ?: "This is a test message", applicationContext)
                }
            }
        }
    }

    private fun handleEventNotification(data: Map<String, String>) {
        val eventType = data["eventType"] ?: "Unknown Event"
        val eventMessage = data["eventMessage"] ?: "New event added!"

        // You can customize the notification content based on your data payload.
        showNotification("New Event: $eventType", applicationContext)
    }


    private fun showNotification(message: String, context: Context) {
        // Create a notification channel (required for Android Oreo and above)
        createNotificationChannel(context)

        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.streetcare_logo) // Set your notification icon
            .setContentTitle("FCM Notification")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build()) // You can provide a unique notification ID
        }
    }

    override fun onNewToken(token: String) {
        // Handle the new FCM token here
        // This method will be called whenever the token is refreshed or generated for the first time
        Log.d("FCM Token", "token: $token")
        // You can also save the token locally or send it to your server
    }


    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "FCM Channel"
            val descriptionText = "Firebase Cloud Messaging Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "fcm_channel_id" // Provide a unique channel ID
    }
}

