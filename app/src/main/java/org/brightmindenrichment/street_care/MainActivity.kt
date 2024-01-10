package org.brightmindenrichment.street_care

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import org.brightmindenrichment.street_care.databinding.ActivityMainBinding
import org.brightmindenrichment.street_care.ui.community.data.Event
import org.brightmindenrichment.street_care.util.Extensions.Companion.getDateTimeFromTimestamp

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavView : BottomNavigationView
    private lateinit var listenerRegistration: ListenerRegistration

    private val db = Firebase.firestore

    // Use 'hasInitialized' to avoid receiving notifications when the user first opens the app.
    private var hasInitialized = false
    private val eventsMap: MutableMap<String, Event> = mutableMapOf() // <id : Event>

    companion object {
        private const val TAG = "MainActivity" // Provide a meaningful tag
        private const val EVENTS_NOTIFICATION_CHANNEL_ID = "events_notification_channel_id" // Provide a unique channel ID
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()
        // it will be triggered if there is a change in the "events" collection
        addSnapshotListenerToCollection(db.collection("events"))

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNav)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_visit, R.id.nav_community, R.id.nav_user
            ), drawerLayout
        )

        FirebaseMessaging.getInstance().subscribeToTopic("events")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Successfully subscribed to FCM topic")
                } else {
                    Log.e(TAG, "Error subscribing to FCM topic", task.exception)
                }
            }
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        bottomNavView.setupWithNavController(navController)

        /* FirebaseMessaging.getInstance().token.addOnCompleteListener{ task ->
             if (!task.isSuccessful) {
                 Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                 return@addOnCompleteListener
             }

             // Get new FCM registration token
             val token = task.result

             // Log and toast
             Log.d(TAG, "token: $token")
         }*/
     }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration.remove()
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun showRationaleDialog(
        title: String,
        message: String,
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
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

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.

                showRationaleDialog(
                    title = "Street Care requires notification permission",
                    message = "You won't receive a notification because the permission is denied"
                )
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private fun addSnapshotListenerToCollection(collectionRef: CollectionReference) {
        listenerRegistration = collectionRef
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("firebase", "Listen failed.", e)
                    return@addSnapshotListener
                }
                Log.d("firebase", "hasInitialized: $hasInitialized")
                if(hasInitialized) {
                    for (dc in snapshots!!.documentChanges) {
                        var eventTitle = dc.document.data["title"].toString() ?: "Unknown Event"
                        var timestamp = dc.document.data["date"]
                        var eventDescription = dc.document.data["description"].toString() ?: "No Description"
                        var eventLocation = dc.document.data["location"].toString() ?: "No Location"
                        //Log.d("firebase", "timestamp: $timestamp")
                        var eventMessage = "${getDateTimeFromTimestamp(timestamp)}/$eventDescription/$eventLocation"
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                eventsMap[dc.document.id] = createEvent(dc)
                                val notificationTitle = "New Event Added"
                                showNotification(
                                    notificationTitle,
                                    eventTitle,
                                    eventMessage,
                                    applicationContext,
                                    ChangedType.Add.type,
                                    dc.document.id
                                )
                                Log.d("firebase", "New event added: ${dc.document.data}")
                            }
                            DocumentChange.Type.MODIFIED -> {
                                // only show the notification when title, location, date, description, status changed.
                                val originalEvent = eventsMap[dc.document.id]
                                var shouldShowNotification = false
                                var notificationTitle = "Event Modified"
                                originalEvent?.let {
                                    if(originalEvent.timestamp != getDateTimeFromTimestamp(dc.document.data["date"])) {
                                        Log.d("modify", "originalEvent.timestamp: ${originalEvent.timestamp}")
                                        Log.d("modify", "new timestamp: ${getDateTimeFromTimestamp(dc.document.data["date"])}")
                                        originalEvent.timestamp = getDateTimeFromTimestamp(dc.document.data["date"])
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

                                    eventTitle = originalEvent.title
                                    eventDescription = originalEvent.description!!
                                    eventLocation = originalEvent.location!!
                                    eventMessage = "${originalEvent.timestamp}/$eventDescription/$eventLocation"
                                }

                                if(shouldShowNotification) showNotification(
                                    notificationTitle,
                                    eventTitle,
                                    eventMessage,
                                    applicationContext,
                                    ChangedType.Modify.type,
                                    dc.document.id
                                )
                                Log.d("firebase", "Modified event: ${dc.document.data}")
                            }
                            DocumentChange.Type.REMOVED -> {
                                eventsMap.remove(dc.document.id)
                                val notificationTitle = "Event removed"
                                showNotification(
                                    notificationTitle,
                                    eventTitle,
                                    eventMessage,
                                    applicationContext,
                                    ChangedType.Remove.type,
                                    dc.document.id
                                )
                                Log.d("firebase", "Removed event: ${dc.document.data}")
                            }
                        }
                    }
                }
                else {
                    // initialize the eventsMap
                    for (dc in snapshots!!.documentChanges) {
                        eventsMap[dc.document.id] = createEvent(dc)
                    }
                    Log.d("notification_navigation", "eventsMapSize: ${eventsMap.size}")
                    Log.d("notification_navigation", "eventsMap: $eventsMap")

                }


                hasInitialized = true
            }
    }

    private fun createEvent( dc: DocumentChange): Event {
        val event = Event()
        event.timestamp = getDateTimeFromTimestamp(dc.document.data["date"])
        event.description = dc.document.data["description"] as? String
        event.interest = ((dc.document.data["interest"]?:0) as Long).toInt()
        event.location = dc.document.data["location"] as? String
        event.status = dc.document.data["status"] as? String
        event.title = dc.document.data["title"] as String
        return event
    }

    private fun createPendingIntent(
        changedType: String,
        eventId: String,
        eventTitle: String
    ): PendingIntent {
        val resultPendingIntent = NavDeepLinkBuilder(this)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.nav_community)
            .setDestination(R.id.communityEventFragment)
            .setArguments(bundleOf(
                "changedType" to changedType,
                "eventId" to eventId,
                "eventTitle" to eventTitle
            ))
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
        return resultPendingIntent
    }

    private fun showNotification(
        notificationTitle: String,
        eventTitle: String,
        eventMessage: String,
        context: Context,
        changedType: String,
        eventId: String,
    ) {

        // create pendingIntent to redirect to the event fragment when users click the notification
        val pendingIntent = createPendingIntent(changedType, eventId, eventTitle)

        // Create a notification channel (required for Android Oreo and above)
        createNotificationChannel(context)
        val eventMessageChunk = eventMessage.split("/")
        val dateAndTime = eventMessageChunk[0]
        val description = eventMessageChunk[1]
        val location = eventMessageChunk[2]

        // Build the notification
        // https://developer.android.com/develop/ui/views/notifications/expanded
        val builder = NotificationCompat.Builder(context, EVENTS_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.streetcare_logo) // Set your notification icon
            .setContentTitle(notificationTitle)
            .setContentText(eventTitle)
            .setContentIntent(pendingIntent)
            .setStyle(
                NotificationCompat.InboxStyle()
                    .addLine(eventTitle)
                    .addLine("location: $location")
                    .addLine(dateAndTime)
                    .addLine("description: $description")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            notify(2, builder.build()) // You can provide a unique notification ID
        }
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Events Update Channel"
            val descriptionText = "Firebase Events Collection Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(EVENTS_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}

sealed class ChangedType(val type: String) {
    object Add: ChangedType("add"){}
    object Modify: ChangedType("modify"){}
    object Remove: ChangedType("remove"){}
}