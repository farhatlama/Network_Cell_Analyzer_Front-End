package com.example.networkcellanalyzer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.loginapp.AboutAppActivity
import com.example.loginapp.LoginActivity
import com.example.networkcellanalyzer.utils.SessionManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter


class GraphActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var returnBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        // Setup top bar
        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navigation_view)
        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        bottomNav = findViewById(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_radio -> {
                    startActivity(Intent(this, StatisticsActivity::class.java))
                    true
                }
                R.id.navigation_square -> {
                    finish()
                    true
                }
                R.id.navigation_help -> {
                    startActivity(Intent(this, AboutAppActivity::class.java))
                    true
                }
                else -> false
            }
        }

        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)
        returnBtn = findViewById(R.id.returnBtn)

        returnBtn.setOnClickListener {
            finish()
        }

        loadChart()
    }

    private fun loadChart() {
        val title = intent.getStringExtra("title") ?: ""
        val chartType = intent.getStringExtra("chart_type") ?: ""
        val labels = intent.getStringArrayListExtra("labels") ?: arrayListOf()
        val values = intent.getFloatArrayExtra("values") ?: floatArrayOf()

        supportActionBar?.title = title

        if (chartType == "pie") {
            pieChart.visibility = PieChart.VISIBLE
            barChart.visibility = BarChart.GONE

            val entries = ArrayList<PieEntry>()
            for (i in labels.indices) {
                entries.add(PieEntry(values[i], labels[i]))
            }

            val dataSet = PieDataSet(entries, "")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            val data = PieData(dataSet)

            pieChart.data = data
            pieChart.description = Description().apply { text = "" }
            pieChart.animateY(1000)
            pieChart.invalidate()

        } else {
            barChart.visibility = BarChart.VISIBLE
            pieChart.visibility = PieChart.GONE

            val entries = ArrayList<BarEntry>()
            for (i in labels.indices) {
                entries.add(BarEntry(i.toFloat(), values[i]))
            }

            val dataSet = BarDataSet(entries, "Values")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            val data = BarData(dataSet)

            barChart.data = data
            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            barChart.xAxis.granularity = 1f
            barChart.xAxis.isGranularityEnabled = true
            barChart.description = Description().apply { text = "" }
            barChart.animateY(1000)
            barChart.invalidate()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                SessionManager(this).clearSession()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                return true
            }
            R.id.nav_permissions -> {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
                return true
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
