package com.example.loginapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.networkcellanalyzer.HomeActivity
import com.example.networkcellanalyzer.R

class WelcomeActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 2000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Handle automatic navigation after delay
        Handler(Looper.getMainLooper()).postDelayed({
            // Navigate to the main content activity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_DELAY)
    }
}
