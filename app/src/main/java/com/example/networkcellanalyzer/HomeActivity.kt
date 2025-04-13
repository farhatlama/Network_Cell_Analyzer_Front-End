package com.example.networkcellanalyzer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.loginapp.AboutAppActivity
import com.example.loginapp.AboutLiveViewActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.LayoutInflater
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import com.example.networkcellanalyzer.databinding.ActivityHomeBinding

import androidx.lifecycle.lifecycleScope

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var deviceIdOutput: TextView
    private lateinit var macAddressOutput: TextView
    private lateinit var operatorOutput: TextView
    private lateinit var timeStampOutput: TextView
    private lateinit var sinrOutput: TextView
    private lateinit var networkTypeOutput: TextView
    private lateinit var frequencyBandOutput: TextView
    private lateinit var cellIdOutput: TextView

    // to refresh
    private lateinit var refreshOverlay: CardView
    private lateinit var refreshProgress: ProgressBar

    //to check for internet connection
    private lateinit var binding: ActivityHomeBinding
    private lateinit var noConnectionView: View

    // for scheduling periodic refresh
    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval = 12000L //refreshing every 12seconds, the 2 seconds are added as a safety margin
    private var isRefreshScheduled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Initialize the No Connection view
        setupNoConnectionView()

        // Initialize views
        deviceIdOutput = findViewById(R.id.deviceIdOutput)
        macAddressOutput = findViewById(R.id.macAddressOutput)
        operatorOutput = findViewById(R.id.operatorOutput)
        timeStampOutput = findViewById(R.id.timeStampOutput)
        sinrOutput = findViewById(R.id.sinrOutput)
        networkTypeOutput = findViewById(R.id.networkTypeOutput)
        frequencyBandOutput = findViewById(R.id.frequencyBandOutput)
        cellIdOutput = findViewById(R.id.cellIdOutput)

        // Initialize refresh components
        refreshOverlay = findViewById(R.id.refresh_overlay)
        refreshProgress = findViewById(R.id.refresh_progress)

        // Hide the refresh overlay initially
        refreshOverlay.visibility = View.GONE

        val helpIcon = findViewById<ImageView>(R.id.helpIcon)
        helpIcon.setOnClickListener {
            // Navigate to About Live View screen when clicking the top-right ? button
            val intent = Intent(this, AboutLiveViewActivity::class.java)
            startActivity(intent)
        }

        // Setup bottom navigation
        setupBottomNavigation()

        // Initial check for internet connection
        checkConnectionAndUpdateUI()

        // Set up periodic connectivity checks
        startPeriodicConnectivityChecks()

        // Load initial data if connected
        if (NetworkUtil.isInternetAvailable(this)) {
            loadDeviceData()
            startPeriodicRefresh()
        }
    }

    private fun setupNoConnectionView() {
        // Inflate the no connection view
        noConnectionView = layoutInflater.inflate(R.layout.layout_no_connection, null)

        // Set up retry button click listener
        noConnectionView.findViewById<View>(R.id.buttonRetry).setOnClickListener {
            checkConnectionAndUpdateUI()
        }

        // Set up bottom navigation in the no connection view
        val bottomNav = noConnectionView.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_radio -> true
                R.id.navigation_square -> true
                R.id.navigation_help -> {
                    val intent = Intent(this, AboutAppActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_radio -> {
                    // Handle radio button click
                    true
                }
                R.id.navigation_square -> {
                    // Handle square button click
                    true
                }
                R.id.navigation_help -> {
                    // Navigate to About App screen when clicking the bottom ? button
                    val intent = Intent(this, AboutAppActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun startPeriodicRefresh() {
        if (isRefreshScheduled) return // avoid rescheduling
        isRefreshScheduled = true
        // Schedule the refresh task
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (NetworkUtil.isInternetAvailable(this@HomeActivity)) {
                    refreshData()
                }
                // Schedule the next refresh
                handler.postDelayed(this, refreshInterval)
            }
        }, refreshInterval)
    }

    private fun refreshData() {
        // Show the refresh overlay
        refreshOverlay.visibility = View.VISIBLE

        // Simulate network request delay (1.5 seconds)
        handler.postDelayed({
            // Fetch new data
            loadDeviceData()

            // Hide the refresh overlay after update
            refreshOverlay.visibility = View.GONE
        }, 1500)
    }

    private fun checkConnectionAndUpdateUI() {
        if (!NetworkUtil.isInternetAvailable(this)) {
            // Show no connection view
            showNoConnectionView()
        } else {
            // Hide no connection view and show main content
            hideNoConnectionView()

            // Load data if we just got connected
            loadDeviceData()
            if (!::refreshOverlay.isInitialized || refreshOverlay.visibility != View.VISIBLE) {
                startPeriodicRefresh()
            }
        }
    }

    private fun showNoConnectionView() {
        // Add the no connection view to the root layout if not already added
        if (noConnectionView.parent == null) {
            (binding.root as ViewGroup).addView(noConnectionView)
        }

        // Hide main content
        binding.scrollContent.visibility = View.GONE
        noConnectionView.visibility = View.VISIBLE

        // Stop refresh timer when disconnected
        handler.removeCallbacksAndMessages(null)
    }

    private fun hideNoConnectionView() {
        // Remove the no connection view if it's added
        if (noConnectionView.parent != null) {
            (binding.root as ViewGroup).removeView(noConnectionView)
        }

        // Show main content
        binding.scrollContent.visibility = View.VISIBLE
    }

    private fun startPeriodicConnectivityChecks() {
        lifecycleScope.launch {
            while (true) {
                delay(5000) // Check every 5 seconds
                checkConnectionAndUpdateUI()
            }
        }
    }

    private fun loadDeviceData() {
        // In a real app, this would come from a backend API
        // This is just simulated data for demonstration
        deviceIdOutput.text = "1234567890"
        macAddressOutput.text = "00:1A:2B:3C:4D:5E"
        operatorOutput.text = "T-Mobile"
        timeStampOutput.text = getCurrentTimestamp()
        val sinrValue = 17.0 + Random.nextDouble(3.0)
        sinrOutput.text = String.format("%.1f dB", sinrValue)
        networkTypeOutput.text = "5G"
        frequencyBandOutput.text = "n41 (2.5 GHz)"
        val cellIdBase = 12349800
        val cellIdRandom = cellIdBase + Random.nextInt(200)
        cellIdOutput.text = cellIdRandom.toString()
    }

    private fun getCurrentTimestamp(): String {
        val now = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(now)
    }

    override fun onPause() {
        super.onPause()
        // Remove callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null)
        isRefreshScheduled = false
    }

    override fun onResume() {
        super.onResume()
        // Check connection status when resuming
        checkConnectionAndUpdateUI()
    }
}



/*package com.example.networkcellanalyzer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.loginapp.AboutAppActivity
import com.example.loginapp.AboutLiveViewActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.networkcellanalyzer.R
import android.view.LayoutInflater
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import com.example.networkcellanalyzer.databinding.ActivityHomeBinding
import com.example.networkcellanalyzer.NetworkUtil

import androidx.lifecycle.lifecycleScope

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var deviceIdOutput: TextView
    private lateinit var macAddressOutput: TextView
    private lateinit var operatorOutput: TextView
    private lateinit var timeStampOutput: TextView
    private lateinit var sinrOutput: TextView
    private lateinit var networkTypeOutput: TextView
    private lateinit var frequencyBandOutput: TextView
    private lateinit var cellIdOutput: TextView

    // to refresh

    private lateinit var refreshOverlay: CardView
    private lateinit var refreshProgress: ProgressBar

    //to check for internet connection
    private lateinit var binding: ActivityHomeBinding
    private lateinit var noConnectionView: View
    // for scheduling periodic refresh
    private val handler = Handler(Looper.getMainLooper())

    private val refreshInterval = 12000L //refreshing every 12seconds, the 2 seconds are added as a safety margin



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noConnectionView = layoutInflater.inflate(R.layout.layout_no_connection, null)
        // Set up retry button click listener
        noConnectionView.findViewById<View>(R.id.buttonRetry).setOnClickListener {
            checkConnectionAndUpdateUI()
        }
        // Initial check for internet connection
        checkConnectionAndUpdateUI()

        // Set up periodic connectivity checks
        startPeriodicConnectivityChecks()

        setContentView(R.layout.activity_home)

        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Initialize views
        deviceIdOutput = findViewById(R.id.deviceIdOutput)
        macAddressOutput = findViewById(R.id.macAddressOutput)
        operatorOutput = findViewById(R.id.operatorOutput)
        timeStampOutput = findViewById(R.id.timeStampOutput)
        sinrOutput = findViewById(R.id.sinrOutput)
        networkTypeOutput = findViewById(R.id.networkTypeOutput)
        frequencyBandOutput = findViewById(R.id.frequencyBandOutput)
        cellIdOutput = findViewById(R.id.cellIdOutput)

        // Initialize refresh components
        refreshOverlay = findViewById(R.id.refresh_overlay)
        refreshProgress = findViewById(R.id.refresh_progress)

        // Hide the refresh overlay initially
        refreshOverlay.visibility = View.GONE

        val helpIcon = findViewById<ImageView>(R.id.helpIcon)
        helpIcon.setOnClickListener {
            // Navigate to About Live View screen when clicking the top-right ? button
            val intent = Intent(this, AboutLiveViewActivity::class.java)
            startActivity(intent)
        }

        // Setup bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_radio -> {
                    // Handle radio button click
                    true
                }
                R.id.navigation_square -> {
                    // Handle square button click
                    true
                }
                R.id.navigation_help -> {
                    // Navigate to About App screen when clicking the bottom ? button
                    val intent = Intent(this, AboutAppActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }


        // Load data from backend (simulated here)
        loadDeviceData()

        startPeriodicRefresh()

    }
    private fun startPeriodicRefresh() {
        // Schedule the refresh task
        handler.postDelayed(object : Runnable {
            override fun run() {
                refreshData()
                // Schedule the next refresh
                handler.postDelayed(this, refreshInterval)
            }
        }, refreshInterval)
    }
    private fun refreshData() {
        // Show the refresh overlay
        refreshOverlay.visibility = View.VISIBLE

        // Simulate network request delay (1.5 seconds)
        handler.postDelayed({
            // Fetch new data
            loadDeviceData()

            // Hide the refresh overlay after update
            refreshOverlay.visibility = View.GONE
        }, 1500)
    }

    private fun checkConnectionAndUpdateUI() {
        if (!NetworkUtil.isInternetAvailable(this)) {
            // Show no connection view
            showNoConnectionView()
        } else {
            // Hide no connection view and show main content
            hideNoConnectionView()
        }
    }

    private fun showNoConnectionView() {
        // Add the no connection view to the root layout if not already added
        if (noConnectionView.parent == null) {
            binding.root.addView(noConnectionView)
        }
        // Hide main content if needed
        binding.scrollContent.visibility = View.GONE
        noConnectionView.visibility = View.VISIBLE
    }

    private fun hideNoConnectionView() {
        // Remove the no connection view if it's added
        if (noConnectionView.parent != null) {
            binding.root.removeView(noConnectionView)
        }
        // Show main content
        binding.scrollContent.visibility = View.VISIBLE

    }

    private fun startPeriodicConnectivityChecks() {
        lifecycleScope.launch {
            while (true) {
                delay(5000) // Check every 5 seconds
                checkConnectionAndUpdateUI()
            }
        }
    }
    private fun loadDeviceData() {
        // In a real app, this would come from a backend API
        // This is just simulated data for demonstration
        deviceIdOutput.text = "1234567890"
        macAddressOutput.text = "00:1A:2B:3C:4D:5E"
        operatorOutput.text = "T-Mobile"
        timeStampOutput.text = getCurrentTimestamp()
        val sinrValue = 17.0 + Random.nextDouble(3.0)
        sinrOutput.text = String.format("%.1f dB", sinrValue)
        networkTypeOutput.text = "5G"
        frequencyBandOutput.text = "n41 (2.5 GHz)"
        val cellIdBase = 12349800
        val cellIdRandom = cellIdBase + Random.nextInt(200)
        cellIdOutput.text = cellIdRandom.toString()
    }
    private fun getCurrentTimestamp(): String {
        val now = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(now)
    }

    override fun onPause() {
        super.onPause()
        // Remove callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        // Restart periodic refresh when activity resumes
        startPeriodicRefresh()
    }
}

 */