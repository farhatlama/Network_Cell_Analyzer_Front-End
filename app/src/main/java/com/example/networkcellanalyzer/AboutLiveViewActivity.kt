/*package com.example.networkcellanalyzer

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.loginapp.AboutAppActivity

import com.example.networkcellanalyzer.HomeActivity
import com.example.networkcellanalyzer.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class AboutLiveViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_live_view)

        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Handle close button click
        val closeIcon = findViewById<ImageView>(R.id.closeIcon)
        closeIcon.setOnClickListener {
            finish()
        }

        // Set up bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.navigation_help // highlight the current section

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_radio -> {
                    // Just stay here or do nothing (you can show a Toast or dialog if needed)
                    true
                }
                R.id.navigation_square -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish() // optional: close current activity
                    true
                }
                R.id.navigation_help -> {
                    val intent = Intent(this, AboutAppActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    */

package com.example.loginapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import com.example.networkcellanalyzer.R

class AboutLiveViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_live_view)

        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Handle close button click
        val closeIcon = findViewById<ImageView>(R.id.closeIcon)
        closeIcon.setOnClickListener {
            finish()
        }
    }
}

