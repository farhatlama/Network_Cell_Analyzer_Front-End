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

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.networkcellanalyzer.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.example.networkcellanalyzer.utils.SessionManager
import com.google.android.material.navigation.NavigationView

class AboutLiveViewActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_live_view)
        setupBottomNavigation()

        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        drawerLayout.closeDrawer(GravityCompat.START)


        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupNavigationDrawer()
        // Handle close button click
        val closeIcon = findViewById<ImageView>(R.id.closeIcon)
        closeIcon.setOnClickListener {
            finish()

        }
        setupBottomNavigation()

    }

    private fun setupNavigationDrawer() {
        val sessionManager = SessionManager(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_permissions -> {
                    // Handle Permissions navigation
                    Toast.makeText(this, "Opening Permissions", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_logout -> {
                    // Handle Log out action (log the user out and go to login screen)
                    sessionManager.clearSession() // Clear session data
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // Finish current activity (go back to login)
                    true
                }
                else -> false
            }.also {
                drawerLayout.closeDrawers() // Close the drawer after selecting an item
            }
        }





    }
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.navigation_radio

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_radio -> true // Already here
                R.id.navigation_square -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_help -> {
                    startActivity(Intent(this, AboutAppActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }


}


