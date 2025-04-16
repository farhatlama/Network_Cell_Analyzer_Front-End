package com.example.networkcellanalyzer

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.networkcellanalyzer.R
import com.google.android.material.navigation.NavigationView

class NavigationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        val toolbar: Toolbar = findViewById(R.id.toolbar)

        val navView: NavigationView = findViewById(R.id.nav_view)
        setSupportActionBar(toolbar)
        // Set up the navigation drawer toggle

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set up navigation item click listener
        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks
        when (item.itemId) {
            R.id.nav_permissions -> {
                // Handle permissions action
                Toast.makeText(this, "Permissions clicked", Toast.LENGTH_SHORT).show()
                // Implement your permissions logic here
            }
            R.id.nav_logout -> {
                // Handle log out action
                Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show()
                // Implement your logout logic here
            }
        }

        // Close the drawer after an item is selected
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        // Close drawer on back press if it's open, otherwise perform normal back action
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}