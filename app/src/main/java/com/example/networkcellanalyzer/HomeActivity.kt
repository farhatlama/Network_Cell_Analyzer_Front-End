package com.example.networkcellanalyzer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.networkcellanalyzer.databinding.ActivityHomeBinding

import androidx.lifecycle.lifecycleScope
import com.example.loginapp.AboutAppActivity
import com.example.loginapp.LoginActivity

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import utils.ApiClient
import com.example.networkcellanalyzer.model.CellRecordSubmission
import com.example.networkcellanalyzer.model.NetworkData
import com.example.networkcellanalyzer.utils.SessionManager
import com.google.android.material.navigation.NavigationView
import utils.DeviceInfoUtil
import utils.NetworkUtil

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
        cellId = "unknown",
        signalPower = 0.0
    )

    // to refresh
    private lateinit var refreshOverlay: CardView
    private lateinit var refreshProgress: ProgressBar

    //to check for internet connection
    private lateinit var binding: ActivityHomeBinding
    private lateinit var noConnectionView: View

    // for scheduling periodic refresh
    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval = 10000L //refreshing every 10seconds
    private var isRefreshScheduled = false

    // For Navigation Drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupBottomNavigation()

        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

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

        setupNavigationDrawer()

        val helpIcon = findViewById<ImageView>(R.id.helpIcon)
        helpIcon.setOnClickListener {
            // Navigate to About Live View screen when clicking the top-right ? button
            val intent = Intent(this, AboutLiveViewActivity::class.java)
            startActivity(intent)
        }

        // Initial check for internet connection
        checkConnectionAndUpdateUI()

        // Set up periodic connectivity checks
        startPeriodicConnectivityChecks()

        checkAndRequestPermissions()

        // Load initial data if connected
        if (NetworkUtil.isInternetAvailable(this)) {
            startPeriodicRefresh()
        }

    }

    val PERMISSION_REQUEST_CODE = 1001
    private fun checkAndRequestPermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            // Permissions not granted, request them
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE),
                PERMISSION_REQUEST_CODE
            )

            // Show message explaining why permissions are needed again
            Toast.makeText(this, "Permissions needed to display network information", Toast.LENGTH_SHORT).show()
        } else {
            // Permissions already granted, load data
            loadDeviceData()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions were granted, load data
                loadDeviceData()
            } else {
                // Permissions denied
                Toast.makeText(this, "Cannot display network information without permissions", Toast.LENGTH_LONG).show()

                // Show limited information
                deviceIdOutput.text = DeviceInfoUtil.getDeviceId(this)
                macAddressOutput.text = DeviceInfoUtil.getMacAddress()
                timeStampOutput.text = DeviceInfoUtil.getCurrentTimestamp()

                // Show placeholder for permission-required data
                cellIdOutput.text = "Permission required"
                operatorOutput.text = "Permission required"
                // etc.
            }
        }
    }
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Highlight the active icon
        bottomNavigationView.selectedItemId = R.id.navigation_square

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_radio -> {
                    // Instead of finish(), use this pattern:
                    // startActivity(Intent(this, YourRadioActivity::class.java))
                    true
                }
                R.id.navigation_square -> {
                    // Already on this page, do nothing
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

    private fun setupNavigationDrawer() {
        val sessionManager = SessionManager(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

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
                    // Handle Log out action (log the user out and go to login screen)
                    sessionManager.clearSession() // Clear session data
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // Finish current activity (go back to login)
                    drawerLayout.closeDrawers()
                    true
                }

                else -> false
            }
        }
    }

    private fun showPermissionsDialog() {
        // Create a dialog to show app permissions and their reasons
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
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

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /*@RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadDeviceData()
            } else {
                // Permission denied - handle this case
                Toast.makeText(this, "Location permission required for network data collection", Toast.LENGTH_LONG).show()
            }
        }
    }
    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) */
    private fun loadDeviceData() {
        // Get data from DeviceInfoUtil and session
        val deviceId = sessionManager.getDeviceId() ?: DeviceInfoUtil.getDeviceId(this)
        val macAddress = sessionManager.getMacAddress() ?: DeviceInfoUtil.getMacAddress()
        val ipAddress = sessionManager.getIpAddress() ?: DeviceInfoUtil.getIPAddress()
        val timestamp = DeviceInfoUtil.getCurrentTimestamp()

        // Update basic device info UI
        deviceIdOutput.text = deviceId
        macAddressOutput.text = macAddress
        timeStampOutput.text = timestamp

        // Update network data object with basic info
        networkData.deviceId = deviceId
        networkData.macAddress = macAddress
        networkData.timestamp = timestamp

        // In loadDeviceData()
        try {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            // Check if telephony features are available
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                Log.d("Device", "Telephony features available")
            } else {
                Log.e("Device", "No telephony features on this device!")
            }

            // Check if we can access cell info
            val cellInfoList = telephonyManager.allCellInfo
            if (cellInfoList == null || cellInfoList.isEmpty()) {
                Log.e("Device", "No cell info available!")
            } else {
                Log.d("Device", "Cell info available: ${cellInfoList.size} items")
            }
        } catch (e: Exception) {
            Log.e("Device", "Error accessing telephony service", e)
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Get cell ID using DeviceInfoUtil
            val cellId = DeviceInfoUtil.getCellId(this)
            if (cellId != -1) {
                cellIdOutput.text = cellId.toString()
                networkData.cellId = cellId.toString()
            } else {
                cellIdOutput.text = "Unknown"
                networkData.cellId = "Unknown"
            }

            // Get operator name
            val operatorName = DeviceInfoUtil.getOperatorName(this)
            operatorOutput.text = operatorName
            networkData.operator = operatorName

            // Get network type
            val networkType = DeviceInfoUtil.getNetworkType(this)
            networkTypeOutput.text = networkType
            networkData.networkType = networkType

            // Get frequency band (now properly extracted from device)
            val frequencyBand = DeviceInfoUtil.getFrequencyBand(this)
            frequencyBandOutput.text = frequencyBand
            networkData.frequencyBand = frequencyBand

            // Get signal metrics
            val (sinr, signalStrength) = DeviceInfoUtil.getSignalMetrics(this)
            sinrOutput.text = String.format("%.1f dB", sinr)
            networkData.sinr = sinr
            networkData.signalPower = signalStrength

            // Log device info
            Log.d("DeviceInfo", "Device ID: $deviceId, MAC: $macAddress, IP: $ipAddress")

        } else {
            // Handle permission not granted
            cellIdOutput.text = "Permission required"
            operatorOutput.text = "Permission required"
            networkTypeOutput.text = "Unknown"
            frequencyBandOutput.text = "Unknown"
            sinrOutput.text = "N/A"
        }

        // Submit the collected data to backend
        submitNetworkData()
    }

    private fun submitNetworkData() {
        lifecycleScope.launch {
            try {
                val token = sessionManager.getAuthToken() ?: return@launch

                // Create submission object
                val submission = CellRecordSubmission(
                    operator = networkData.operator,
                    signal_power = networkData.signalPower,
                    sinr = networkData.sinr,
                    network_type = networkData.networkType,
                    frequency_band = networkData.frequencyBand,
                    cell_id = networkData.cellId,
                    timestamp = DeviceInfoUtil.getFormattedTimestampForApi(),
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
