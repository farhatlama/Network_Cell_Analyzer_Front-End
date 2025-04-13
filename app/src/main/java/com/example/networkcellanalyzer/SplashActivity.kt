package com.example.networkcellanalyzer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.loginapp.LoginActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DURATION = 2000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide the action bar if it's shown
        supportActionBar?.hide()

        // Handler to navigate to the main activity after the splash duration
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Close the splash activity so it's not in the back stack
        }, SPLASH_DURATION)
    }
}