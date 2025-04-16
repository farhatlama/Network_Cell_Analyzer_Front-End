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
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.example.networkcellanalyzer.databinding.ActivityHomeBinding

import androidx.lifecycle.lifecycleScope

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import utils.ApiClient
import com.example.networkcellanalyzer.model.CellRecordSubmission
import com.example.networkcellanalyzer.model.NetworkData
import com.example.networkcellanalyzer.utils.SessionManager
import utils.NetworkUtil
import java.io.IOException
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var deviceIdOutput: TextView
    private lateinit var macAddressOutput: TextView
    private lateinit var operatorOutput: TextView
    private lateinit var timeStampOutput: TextView
    private lateinit var sinrOutput: TextView
    private lateinit var networkTypeOutput: TextView
    private lateinit var frequencyBandOutput: TextView
    private lateinit var cellIdOutput: TextView

    //backend

    private lateinit var sessionManager: SessionManager
    private val networkData = NetworkData(
        deviceId = "unknown",
        macAddress = "unknown",
        operator = "unknown",
        timestamp = "",
        sinr = 0.0,
        networkType = "unknown",
        frequencyBand = "unknown",
        cellId = "unknown"
    )

    // to refresh
    private lateinit var refreshOverlay: CardView
    private lateinit var refreshProgress: ProgressBar

    //to check for internet connection
    private lateinit var binding: ActivityHomeBinding
    private lateinit var noConnectionView: View

    // for scheduling periodic refresh
    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval = 10000L //refreshing every 10seconda
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
        if (!::sessionManager.isInitialized) {
            sessionManager = SessionManager(this)
        }

        // Get device ID (use stored one or generate)
        var deviceId = sessionManager.getDeviceId()
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            sessionManager.saveDeviceId(deviceId)
        }

        // Get MAC address or use a placeholder
        val macAddress = getMacAddress() ?: "02:00:00:00:00:00"

        // Update UI with stored values first
        deviceIdOutput.text = deviceId
        macAddressOutput.text = macAddress
        timeStampOutput.text = getCurrentTimestamp()

        // Try to fetch data from API
        lifecycleScope.launch {
            try {
                // Get auth token
                val token = sessionManager.getAuthToken() ?: return@launch

                // Format dates for API
                val endDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .format(Date())
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, -1) // Last 24 hours
                val startDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .format(calendar.time)

                // Fetch operator stats
                try {
                    val operatorResponse = ApiClient.apiService.getOperatorStats(
                        startDate, endDate, deviceId, "Bearer $token"
                    )
                    if (operatorResponse.isSuccessful && operatorResponse.body() != null) {
                        // Get operator with highest percentage
                        val operators = operatorResponse.body()!!
                        val highestOperator = operators.entries
                            .maxByOrNull { entry ->
                                entry.value.removeSuffix("%").toDoubleOrNull() ?: 0.0
                            }?.key

                        highestOperator?.let {
                            operatorOutput.text = it
                            networkData.operator = it
                        }
                    }
                } catch (e: Exception) {
                    Log.e("API_ERROR", "Failed to fetch operator stats", e)

                }

                // Fetch signal power stats
                try {
                    val powerResponse = ApiClient.apiService.getSignalPowerPerNetwork(
                        startDate, endDate, deviceId, "Bearer $token"
                    )
                    if (powerResponse.isSuccessful && powerResponse.body() != null) {
                        // Use the strongest network type
                        val networkPowers = powerResponse.body()!!

                        // Find network with best signal
                        val bestNetwork = networkPowers.entries
                            .maxByOrNull { it.value }

                        bestNetwork?.let {
                            networkTypeOutput.text = it.key
                            networkData.networkType = it.key

                            // Update frequency band based on network type
                            frequencyBandOutput.text = when(it.key) {
                                "2G" -> "900/1800 MHz"
                                "3G" -> "2100 MHz"
                                "4G" -> "700/1800/2600 MHz"
                                else -> "Unknown"
                            }
                            networkData.frequencyBand = frequencyBandOutput.text.toString()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("API_ERROR", "Failed to fetch operator stats", e)

                }

                // Fetch SINR stats
                try {
                    val sinrResponse = ApiClient.apiService.getSinrPerNetwork(
                        startDate, endDate, deviceId, "Bearer $token"
                    )
                    if (sinrResponse.isSuccessful && sinrResponse.body() != null) {
                        val sinrValues = sinrResponse.body()!!

                        // Get SINR for current network type
                        val currentSinr = sinrValues[networkData.networkType] ?: 0.0
                        sinrOutput.text = String.format("%.1f dB", currentSinr)
                        networkData.sinr = currentSinr
                    }
                } catch (e: Exception) {
                    Log.e("API_ERROR", "Failed to fetch operator stats", e)

                }

                // Generate a cell ID if not already set
                if (networkData.cellId == "unknown") {
                    val cellIdBase = 12349800
                    val cellIdRandom = cellIdBase + Random.nextInt(200)
                    cellIdOutput.text = cellIdRandom.toString()
                    networkData.cellId = cellIdRandom.toString()
                } else {
                    cellIdOutput.text = networkData.cellId
                }

                // Submit the current reading to the backend
                submitNetworkData()

            } catch (e: IOException) {
                Log.e("API_ERROR", "Failed to fetch operator stats", e)

            }
        }
    }
    private fun getMacAddress(): String? {
        try {
            val networkInterfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                val mac = networkInterface.hardwareAddress
                if (mac != null && mac.isNotEmpty()) {
                    val sb = StringBuilder()
                    for (i in mac.indices) {
                        sb.append(String.format("%02X%s", mac[i], if (i < mac.size - 1) ":" else ""))
                    }
                    return sb.toString()
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return null
    }


    private fun submitNetworkData() {
        lifecycleScope.launch {
            try {
                val token = sessionManager.getAuthToken() ?: return@launch

                // Create submission object
                val submission = CellRecordSubmission(
                    operator = networkData.operator,
                    signal_power = -65.0 + Random.nextDouble(10.0), // Estimated from SINR
                    sinr = networkData.sinr,
                    network_type = networkData.networkType,
                    frequency_band = networkData.frequencyBand,
                    cell_id = networkData.cellId,
                    timestamp = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.US).format(Date()),
                    device_mac = networkData.macAddress,
                    device_id = networkData.deviceId
                )

                // Submit to API
                ApiClient.apiService.submitNetworkData(submission, "Bearer $token")

            } catch (e: Exception) {
                // Silent fail - we don't need to show errors for background submissions
            }
        }
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
import utils.NetworkUtil

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