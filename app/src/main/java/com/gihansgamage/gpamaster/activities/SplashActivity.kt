package com.gihansgamage.gpamaster.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.gihansgamage.gpamaster.utils.PrefManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Handle the splash screen transition
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // 2. Initialize PrefManager to check user status
        val pref = PrefManager(this)

        // 3. Determine the destination based on whether it's the first run
        val intent = if (pref.isFirstTime()) {
            Intent(this, SetupActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }

        startActivity(intent)

        // 4. Remove SplashActivity from the back stack
        finish()
    }
}