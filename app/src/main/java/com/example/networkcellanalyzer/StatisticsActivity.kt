package com.example.networkcellanalyzer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.loginapp.AboutAppActivity
import com.example.loginapp.LoginActivity
import com.example.networkcellanalyzer.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sessionManager: SessionManager
    private var startDate: Date? = null
    private var endDate: Date? = null
    private val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        sessionManager = SessionManager(this)
        drawerLayout = findViewById(R.id.drawer_layout)

        // Toolbar setup
        val toolbar = findViewById<Toolbar>(R.id.statistics_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "Statistics"

        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener(this)

        findViewById<ImageView>(R.id.helpIcon).setOnClickListener {
            startActivity(Intent(this, AboutTimeActivity::class.java))
        }

        val btnStart = findViewById<Button>(R.id.btnStartTime)
        val btnEnd = findViewById<Button>(R.id.btnEndTime)
        val btnStats = findViewById<Button>(R.id.btnViewStats)
        val tvError = findViewById<TextView>(R.id.tvError)

        btnStart.setOnClickListener {
            pickDateTime { selected ->
                startDate = selected
                btnStart.text = "Start: ${sdf.format(selected)}"
            }
        }

        btnEnd.setOnClickListener {
            pickDateTime { selected ->
                endDate = selected
                btnEnd.text = "End: ${sdf.format(selected)}"
            }
        }

        btnStats.setOnClickListener {
            when {
                startDate == null || endDate == null -> {
                    tvError.text = "Please select both start and end times."
                    tvError.visibility = TextView.VISIBLE
                }
                endDate!!.before(startDate) -> {
                    tvError.text = "End must be after start."
                    tvError.visibility = TextView.VISIBLE
                }
                endDate!!.time - startDate!!.time < 3 * 60 * 1000 -> {
                    tvError.text = "Range must be at least 3 minutes."
                    tvError.visibility = TextView.VISIBLE
                }
                else -> {
                    tvError.visibility = TextView.GONE
                    val intent = Intent(this, StatisticsResultActivity::class.java)
                    intent.putExtra("start_time", sdf.format(startDate!!))
                    intent.putExtra("end_time", sdf.format(endDate!!))
                    startActivity(intent)
                }
            }
        }

        setupBottomNavigation()
    }

    private fun pickDateTime(onSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                cal.set(year, month, day, hour, minute)
                onSelected(cal.time)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.navigation_radio

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_square -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navigation_help -> {
                    startActivity(Intent(this, AboutAppActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_permissions -> {
                val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                dialog.setTitle("Permissions Required")
                dialog.setMessage(
                    "This app requires the following permissions:\n\n" +
                            "1. Location: To detect the network cell and measure signal strength.\n" +
                            "2. Phone State: To access your phone's status for proper network analysis.\n\n" +
                            "We do not collect any personal data."
                )
                dialog.setPositiveButton("OK") { d, _ -> d.dismiss() }
                dialog.show()
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.nav_logout -> {
                sessionManager.clearSession()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> false
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
