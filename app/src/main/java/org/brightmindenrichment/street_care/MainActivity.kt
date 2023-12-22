package org.brightmindenrichment.street_care

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import org.brightmindenrichment.street_care.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavView : BottomNavigationView
    companion object {
        private const val TAG = "MainActivity" // Provide a meaningful tag
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()

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
}