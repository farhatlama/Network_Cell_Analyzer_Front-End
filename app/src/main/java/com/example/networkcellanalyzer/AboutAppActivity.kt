package com.example.loginapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.loginapp.HomeActivity
import com.example.loginapp.LoginActivity
import com.example.networkcellanalyzer.R
import com.example.networkcellanalyzer.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class AboutAppActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)

        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Setup Drawer and Navigation
        setupNavigationDrawer()
      //  setupBottomNavigation()

        // Handle close button click
        val closeIcon = findViewById<ImageView>(R.id.closeIcon)
        closeIcon?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

   /* private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView?.let {
            it.selectedItemId = R.id.navigation_help

            it.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_radio -> {
                        // Navigate without finish()

                        true
                    }
                    R.id.navigation_square -> {
                        startActivity(Intent(this, HomeActivity::class.java))
                        // Don't call finish() here
                        true
                    }
                    R.id.navigation_help -> true // Already here
                    else -> false
                }
            }
        }
    }
*/
    private fun setupNavigationDrawer() {
        val sessionManager = SessionManager(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // Setup drawer toggle
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView?.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_permissions -> {
                    // Handle Permissions navigation
                    Toast.makeText(this, "Opening Permissions", Toast.LENGTH_SHORT).show()
                    showPermissionsDialog()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_logout -> {
                    // Handle Log out action (log the user out and go to login screen)
                    sessionManager.clearSession() // Clear session data
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // Finish current activity (go back to login)
                    drawerLayout.closeDrawer(GravityCompat.START)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}