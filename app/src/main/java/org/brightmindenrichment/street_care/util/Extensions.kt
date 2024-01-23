package org.brightmindenrichment.street_care.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import org.brightmindenrichment.data.local.EventsDatabase
import org.brightmindenrichment.street_care.MainActivity
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.notification.ChangedType
import org.brightmindenrichment.street_care.ui.community.model.DatabaseEvent
import org.brightmindenrichment.street_care.util.Constants.EVENTS_NOTIFICATION
import org.brightmindenrichment.street_care.util.Constants.INTENT_TYPE_NOTIFICATION
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class Extensions {
    companion object{

        private fun onAdded(
            scope: CoroutineScope,
            databaseEvent: DatabaseEvent,
            eventsDatabase: EventsDatabase,
            eventTitle: String,
            eventMessage: String,
            appContext: Context,
            eventId: String
        ) {
            scope.launch(IO) {
                Log.d("workManager", "onAdded")
                val notificationTitle = "New Event Added"
                eventsDatabase.eventDao().addEvent(databaseEvent)
                showNotification(
                    notificationTitle,
                    eventTitle,
                    eventMessage,
                    appContext,
                    ChangedType.Add.type,
                    eventId,
                )
            }
        }

        private fun onRemoved(
            scope: CoroutineScope,
            eventsDatabase: EventsDatabase,
            eventTitle: String,
            eventMessage: String,
            appContext: Context,
            eventId: String
        ) {
            scope.launch(IO) {
                eventsDatabase.eventDao().deleteEvent(eventId)
                val notificationTitle = "Event removed"
                showNotification(
                    notificationTitle,
                    eventTitle,
                    eventMessage,
                    appContext,
                    ChangedType.Remove.type,
                    eventId,
                )
            }
        }

        private fun onModified(
            scope: CoroutineScope,
            eventsDatabase: EventsDatabase,
            appContext: Context,
            dc: DocumentChange
        ) {
            scope.launch(IO) {
                // only show the notification when title, location, date, description, status changed.
                val originalEvent = eventsDatabase.eventDao().getEventById(dc.document.id)!!
                var shouldShowNotification = false
                var notificationTitle = "Event Modified"
                val originalEventDate = getDateTimeFromTimestamp(originalEvent.date)
                val newEventDate = getDateTimeFromTimestamp(dc.document.data["date"])
                if(originalEventDate != newEventDate) {
                    Log.d("modify", "originalEvent.timestamp: ${originalEvent.date}")
                    Log.d("modify", "new timestamp: ${dc.document.data["date"]}")
                    originalEvent.date = getDateTimeFromTimestamp(dc.document.data["date"])
                    shouldShowNotification = true
                }
                if(originalEvent.description != dc.document.data["description"]) {
                    Log.d("modify", "originalEvent.description: ${originalEvent.description}")
                    Log.d("modify", "new description: ${dc.document.data["description"]}")
                    originalEvent.description = (dc.document.data["description"] ?: "no description") as String
                    shouldShowNotification = true
                }
                if(originalEvent.interest != dc.document.data["interest"]) {
                    Log.d("modify", "originalEvent.interest: ${originalEvent.interest}")
                    Log.d("modify", "new interest: ${dc.document.data["interest"]}")
                    originalEvent.interest = ((dc.document.data["interest"]?: 0) as Long).toInt()
                }
                if(originalEvent.location != dc.document.data["location"]) {
                    Log.d("modify", "originalEvent.location: ${originalEvent.location}")
                    Log.d("modify", "new location: ${dc.document.data["location"]}")
                    originalEvent.location = (dc.document.data["location"]?: "no location") as String
                    shouldShowNotification = true
                }
                if(originalEvent.status != dc.document.data["status"]) {
                    Log.d("modify", "originalEvent.status: ${originalEvent.status}")
                    Log.d("modify", "new status: ${dc.document.data["status"]}")
                    originalEvent.status = (dc.document.data["status"]?: "no status") as String
                    shouldShowNotification = true
                    when(originalEvent.status?.lowercase()) {
                        "pending" -> notificationTitle = "Event Removed"
                        "approved" -> notificationTitle = "Event Added"
                    }
                }
                if(originalEvent.title != dc.document.data["title"]) {
                    Log.d("modify", "originalEvent.title: ${originalEvent.title}")
                    Log.d("modify", "new title: ${dc.document.data["title"]}")
                    originalEvent.title = (dc.document.data["title"]?: "no title") as String
                    shouldShowNotification = true
                }

                val eventTitle = originalEvent.title ?: "Unknown Title"
                val eventDescription = originalEvent.description!!
                val eventLocation = originalEvent.location!!
                val eventMessage = "${originalEvent.date}/$eventDescription/$eventLocation"
                if(shouldShowNotification) showNotification(
                    notificationTitle,
                    eventTitle,
                    eventMessage,
                    appContext,
                    ChangedType.Modify.type,
                    dc.document.id,
                )
                Log.d("workManager", "Extension, Modified event: ${dc.document.data}")
                eventsDatabase.eventDao().updateUsers(originalEvent)
            }
        }
        suspend fun addSnapshotListenerToCollection(
            collectionRef: CollectionReference,
            eventsDatabase: EventsDatabase,
            appContext: Context,
            scope: CoroutineScope,
            dataStoreManager: DataStoreManager,
            isFromWorker: Boolean
        ): ListenerRegistration {
            val isFirstListener = AtomicBoolean(true)
            val listenerRegistration = collectionRef
                .addSnapshotListener { snapshots, e ->
                    scope.launch(IO) {
                        if (e != null) {
                            Log.w("workManager", "Listen failed.", e)
                            return@launch
                        }

                        Log.d("workManager", "isFirstListener: ${isFirstListener.get()}")

                        if(isFirstListener.get()) {
                            isFirstListener.set(false)
                            return@launch
                        }

                        val isAppOnBackground = dataStoreManager.getIsAppOnBackground().first()
                        Log.d("workManager", "isAppOnBackground: $isAppOnBackground")
                        Log.d("workManager", "isFromWorker: $isFromWorker")

                        if(isFromWorker && !isAppOnBackground) return@launch

                        for (dc in snapshots!!.documentChanges) {
                            val eventTitle = (dc.document.data["title"]?: "Unknown Event").toString()
                            val timestamp = dc.document.data["date"]
                            val eventDescription = (dc.document.data["description"]?: "No Description").toString()
                            val eventLocation = (dc.document.data["location"]?: "No Location").toString()
                            val eventMessage = "${getDateTimeFromTimestamp(timestamp)}/$eventDescription/$eventLocation"
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    val databaseEvent = createDatabaseEvent(dc)
                                    onAdded(
                                        scope,
                                        databaseEvent,
                                        eventsDatabase,
                                        eventTitle,
                                        eventMessage,
                                        appContext,
                                        dc.document.id
                                    )
                                    Log.d("workManager", "New event added: ${dc.document.data}")
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    onModified(
                                        scope,
                                        eventsDatabase,
                                        appContext,
                                        dc
                                    )
                                }
                                DocumentChange.Type.REMOVED -> {
                                    onRemoved(
                                        scope,
                                        eventsDatabase,
                                        eventTitle,
                                        eventMessage,
                                        appContext,
                                        dc.document.id
                                    )
                                    Log.d("workManager", "event removed: ${dc.document.data}")
                                }
                            }
                        }
                    }
                }
            return listenerRegistration
        }


        private fun createDatabaseEvent(dc: DocumentChange): DatabaseEvent {
            return DatabaseEvent(
                id = dc.document.id,
                date = getDateTimeFromTimestamp(dc.document.data["date"]),
                description = dc.document.data["description"] as? String,
                interest = ((dc.document.data["interest"] ?: 0) as Long).toInt(),
                location = dc.document.data["location"] as? String,
                status = dc.document.data["status"] as? String,
                title = dc.document.data["title"] as String,
            )
        }

        fun createDatabaseEvent(document: QueryDocumentSnapshot): DatabaseEvent {
            return DatabaseEvent(
                id = document.id,
                date = getDateTimeFromTimestamp(document.get("date")),
                description = document.get("description")?.toString() ?: "Unknown Description",
                interest = ((document.get("interest") ?: 0) as Long).toInt(),
                location = document.get("location")?.toString() ?: "Unknown Location",
                status = document.get("status")?.toString() ?: "Unknown Status",
                title = document.get("title")?.toString() ?: "Unknown Title",
            )
        }

        private fun createPendingIntentByTaskStackBuilder(
            changedType: String,
            eventId: String,
            eventTitle: String,
            appContext: Context,
        ): PendingIntent {
            // Create an Intent for the activity you want to start.
            val resultIntent = Intent(appContext, MainActivity::class.java)
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            resultIntent.putExtra( "changedType", changedType)
            resultIntent.putExtra( "eventId", eventId)
            resultIntent.putExtra( "eventTitle", eventTitle)
            resultIntent.putExtra( "intentType", INTENT_TYPE_NOTIFICATION)
            // Create the TaskStackBuilder.
            return TaskStackBuilder.create(appContext).run {
                // Add the intent, which inflates the back stack.
                addNextIntentWithParentStack(resultIntent)
                // Get the PendingIntent containing the entire back stack.
                getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }

        }

        private fun createPendingIntent(
            changedType: String,
            eventId: String,
            eventTitle: String,
            context: Context
        ): PendingIntent {
            return NavDeepLinkBuilder(context)
                .setGraph(R.navigation.mobile_navigation)
                .setDestination(R.id.nav_community)
                .setDestination(R.id.communityEventFragment)
                .setComponentName(MainActivity::class.java)
                .setArguments(
                    bundleOf(
                        "changedType" to changedType,
                        "eventId" to eventId,
                        "eventTitle" to eventTitle
                    )
                )
                .createPendingIntent()

            /*
            // Create an Intent for the activity you want to start.
            val resultIntent = Intent(context, MainActivity::class.java)
            // Create the TaskStackBuilder.
            val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
                // Add the intent, which inflates the back stack.
                addNextIntentWithParentStack(resultIntent)
                // Get the PendingIntent containing the entire back stack.
                getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }

             */

        }

        fun showNotification(
            notificationTitle: String,
            eventTitle: String,
            eventMessage: String,
            context: Context,
            changedType: String,
            eventId: String,
        ) {
            Firebase.auth.currentUser?.let {
                // create pendingIntent to redirect to the event fragment when users click the notification
                //val pendingIntent = createPendingIntentByTaskStackBuilder(changedType, eventId, eventTitle, context)
                val pendingIntent = createPendingIntent(changedType, eventId, eventTitle, context)

                // Create a notification channel (required for Android Oreo and above)
                createNotificationChannel(context)
                val eventMessageChunk = eventMessage.split("/")
                val dateAndTime = eventMessageChunk[0]
                val description = eventMessageChunk[1]
                val location = eventMessageChunk[2]

                // Build the notification
                // https://developer.android.com/develop/ui/views/notifications/expanded
                val builder = NotificationCompat.Builder(context,
                    Constants.EVENTS_NOTIFICATION_CHANNEL_ID
                )
                    .setSmallIcon(R.drawable.streetcare_logo) // Set your notification icon
                    .setContentTitle(notificationTitle)
                    .setContentText(eventTitle)
                    .setContentIntent(pendingIntent)
                    //.setGroup(EVENTS_NOTIFICATION)
                    .setStyle(
                        NotificationCompat.InboxStyle()
                            .addLine(eventTitle)
                            .addLine("location: $location")
                            .addLine(dateAndTime)
                            .addLine("description: $description")
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                // Show the notification
                with(NotificationManagerCompat.from(context)) {
                    if(checkPermission(Manifest.permission.POST_NOTIFICATIONS, context)) {
                        notify(NotificationID.getNotificationID(), builder.build()) // You can provide a unique notification ID
                    }
                }
            }

        }

        private fun createNotificationChannel(context: Context) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Events Update Channel"
                val descriptionText = "Firebase Events Collection Channel"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(Constants.EVENTS_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }

                // Register the channel with the system
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun checkPermission(permission: String, context: Context): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }

        private fun showRationaleDialog(
            title: String,
            message: String,
            context: Context,
            requestPermissionLauncher: ActivityResultLauncher<String>
        ) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok") { dialog, _ ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    dialog.dismiss()
                }

            builder.create().show()
        }

        fun askPermission(
            context: Context,
            permission: String,
            activity: Activity,
            requestPermissionLauncher: ActivityResultLauncher<String>,
            title: String,
            message: String,
        ) {
            if (checkPermission(permission, context)) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(activity, permission)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.

                showRationaleDialog(
                    title = title,
                    message = message,
                    context = context,
                    requestPermissionLauncher = requestPermissionLauncher
                )
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(permission)
            }
        }

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