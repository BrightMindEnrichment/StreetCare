package org.brightmindenrichment.street_care

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.brightmindenrichment.street_care.ui.user.UserRepository
import org.brightmindenrichment.street_care.ui.user.UserSingleton

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        // the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        lifecycleScope.launch(Dispatchers.IO) {
            try {
                UserSingleton.userModel = UserRepository().fetchUserData()
                Log.d("ContentValues", "UserSingleton.userModel: ${UserSingleton.userModel}")
            } catch (e: Exception) {
                Log.e("ContentValues", "Error: ${e.message}")
            } finally {
//                withContext(Dispatchers.Main) { // Not needed as main thread called on looper below
                    navigateToUI()
//                }
            }
        }
    }

    private fun navigateToUI() {
        // we used the postDelayed(Runnable, time) method
        // to send a message with a delayed time.
        //Normal Handler is deprecated , so we have to change the code little bit
        Handler(Looper.getMainLooper()).post {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}