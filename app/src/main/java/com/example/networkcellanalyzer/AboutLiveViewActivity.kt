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

package com.example.networkcellanalyzer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import com.example.loginapp.HomeActivity
import com.example.loginapp.LoginActivity
import com.example.networkcellanalyzer.utils.SessionManager
import com.google.android.material.navigation.NavigationView

class AboutLiveViewActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_live_view)

        // Initialize the DrawerLayout before using it
        drawerLayout = findViewById(R.id.drawer_layout)

        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Set up navigation components
        setupNavigationDrawer()
        setupBottomNavigation()

        // Handle close button click
        val closeIcon = findViewById<ImageView>(R.id.closeIcon)
        closeIcon.setOnClickListener {
            finish()
        }
    }

    private fun setupNavigationDrawer() {
        val sessionManager = SessionManager(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // Setup ActionBarDrawerToggle
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Close drawer by default
        drawerLayout.closeDrawer(GravityCompat.START)

        // Setup navigation view listeners
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_permissions -> {
                    // Handle Permissions navigation
                    Toast.makeText(this, "Opening Permissions", Toast.LENGTH_SHORT).show()
                    showPermissionsDialog()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_logout -> {
                    // Handle Log out action
                    sessionManager.clearSession()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun showPermissionsDialog() {
        // Create a dialog to show app permissions and their reasons
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Permissions Required")
        dialog.setMessage(
            "This app requires the following permissions:\n\n" +
                    "1. Location: To detect the network cell and measure signal strength.\n" +
                    "2. Phone State: To access your phone's status for proper network analysis.\n\n" +
                    "We do not collect any personal data."
        )
        dialog.setPositiveButton("OK") { _, _ -> }
        dialog.show()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

