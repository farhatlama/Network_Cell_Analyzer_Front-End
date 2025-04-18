package com.example.networkcellanalyzer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import android.provider.Settings
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
import utils.DeviceInfoUtil.getCurrentTimestamp
import utils.NetworkUtil

class HomeActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_PERMISSIONS = 1001
        private const val SETTINGS_REQUEST_CODE = 102
    }

    private lateinit var deviceIdOutput: TextView
    private lateinit var macAddressOutput: TextView
    private lateinit var operatorOutput: TextView
    private lateinit var timeStampOutput: TextView
    private lateinit var sinrOutput: TextView
    private lateinit var networkTypeOutput: TextView
    private lateinit var frequencyBandOutput: TextView
    private lateinit var cellIdOutput: TextView

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

    // for scheduling periodic refresh
    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval = 10000L //refreshing every 10seconds
    private var isRefreshScheduled = false

    //to check for internet connection
    private lateinit var binding: ActivityHomeBinding
    private lateinit var noConnectionView: View

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
        sessionManager = SessionManager(this)

        // Initialize refresh components
        refreshOverlay = findViewById(R.id.refresh_overlay)
        refreshProgress = findViewById(R.id.refresh_progress)

        // Hide the refresh overlay initially
        refreshOverlay.visibility = View.GONE

        setupNavigationDrawer()

        // Start the periodic update of the timestamp
        startUpdatingTimestamp()


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

    }


private fun startUpdatingTimestamp() { //this is to make sure that the timestamp is live
    val updateTimestampRunnable = object : Runnable {
        override fun run() {
            // Get current timestamp
            val timestamp = getCurrentTimestamp()
            // Update the TextView
            timeStampOutput.text = timestamp

            // Call this function again after 1 second (1000 milliseconds)
            handler.postDelayed(this, 1000)
        }
    }

    // Start the periodic updates
    handler.post(updateTimestampRunnable)
}


private fun checkAndRequestPermissions() { //requesting permissions from the user
        val locationPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val phoneStatePermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        )

        // If both permissions are granted, load data
        if (locationPermission == PackageManager.PERMISSION_GRANTED &&
            phoneStatePermission == PackageManager.PERMISSION_GRANTED) {
            loadDeviceData()
            startUpdatingTimestamp()
            return
        }

        // Create a list of permissions to request
        val permissionsToRequest = mutableListOf<String>()

        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (phoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE)
        }

        // Request permissions
        ActivityCompat.requestPermissions(
            this,
            permissionsToRequest.toTypedArray(),
            REQUEST_PERMISSIONS
        )
    }

    // Handle permission result in one place
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, load device data and continue the live time
                loadDeviceData()
                startUpdatingTimestamp()
            } else {
                startUpdatingTimestamp()
                // Load basic data without permissions
                val deviceId = sessionManager.getDeviceId() ?: DeviceInfoUtil.getDeviceId(this)
                val macAddress = sessionManager.getMacAddress() ?: DeviceInfoUtil.getMacAddress()
                val timestamp = DeviceInfoUtil.getCurrentTimestamp()

                cellIdOutput.text = "Permission required"
                operatorOutput.text = "Permission required"
                networkTypeOutput.text = "Permission required"
                frequencyBandOutput.text = "Permission required"
                sinrOutput.text = "N/A"

                deviceIdOutput.text = deviceId
                macAddressOutput.text = macAddress
                timeStampOutput.text = timestamp

                networkData.deviceId = deviceId
                networkData.macAddress = macAddress
                networkData.timestamp = timestamp

                // Check if user denied with "Don't ask again"
                val anyPermanentlyDenied = permissions.indices.any { i ->
                    grantResults[i] != PackageManager.PERMISSION_GRANTED &&
                            !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])
                }

                if (anyPermanentlyDenied) {
                    // Show settings dialog
                    showSettingsDialog()
                } else {
                    // Show permission explanation dialog
                    Toast.makeText(
                        this,
                        "Permissions are required for complete network information",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }



        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_REQUEST_CODE) {
            // Check permissions again after returning from settings
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED) {
                startUpdatingTimestamp()
                loadDeviceData()
            } else {
                showSettingsDialog()

                // Still not granted
                deviceIdOutput.text = DeviceInfoUtil.getDeviceId(this)
                macAddressOutput.text = DeviceInfoUtil.getMacAddress()
                cellIdOutput.text = "Permission required"
                operatorOutput.text = "Permission required"
                networkTypeOutput.text = "Permission required"
                frequencyBandOutput.text = "Permission required"
                sinrOutput.text = "N/A"
            }
        }
    }
    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Location and Phone permissions are required to analyze network cells. Please enable them in app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, SETTINGS_REQUEST_CODE)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
        // show no connection
        noConnectionView = layoutInflater.inflate(R.layout.layout_no_connection, null)

        // retry button
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
        if (isRefreshScheduled) return // to not reschedule
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
        // Show the refresh
        refreshOverlay.visibility = View.VISIBLE

        handler.postDelayed({
             // remove refresh
            refreshOverlay.visibility = View.GONE
        }, 1500)
    }

    private fun checkConnectionAndUpdateUI() {
        if (!NetworkUtil.isInternetAvailable(this)) {
            // Show no connection view
            stopPeriodicRefresh()
            showNoConnectionView()
        } else {
            // Hide no connection view and show main content
            startUpdatingTimestamp()
            hideNoConnectionView()

            if (!::refreshOverlay.isInitialized || refreshOverlay.visibility != View.VISIBLE) {
                startPeriodicRefresh()
            }
        }
    }

    private fun stopPeriodicRefresh() {
        // Stop periodic refresh
        handler.removeCallbacksAndMessages(null)
        isRefreshScheduled = false
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
        // Remove the no connection view if added
        if (noConnectionView.parent != null) {
            (binding.root as ViewGroup).removeView(noConnectionView)
        }

        // Show main content
        binding.scrollContent.visibility = View.VISIBLE
    }

    private fun startPeriodicConnectivityChecks() {
        lifecycleScope.launch {
            while (true) {
                delay(5000) // Checks every 5 seconds
                checkConnectionAndUpdateUI()
            }
        }
    }

    private fun setupNavigationDrawer() {
        val sessionManager = SessionManager(this)
        // setting up the navigation drawer
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
                    // Handle Log out action (logs the user out and goes to login screen)
                    sessionManager.clearSession() // Clearing session data
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // goes back to login
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


    private fun loadDeviceData() {
        // Get data from DeviceInfoUtil and session
        val deviceId = sessionManager.getDeviceId() ?: DeviceInfoUtil.getDeviceId(this)
        val macAddress = sessionManager.getMacAddress() ?: DeviceInfoUtil.getMacAddress()
        val ipAddress = sessionManager.getIpAddress() ?: DeviceInfoUtil.getIPAddress()
        val timestamp = DeviceInfoUtil.getCurrentTimestamp()

        val networkType = DeviceInfoUtil.getNetworkType(this)
        networkTypeOutput.text = networkType
        networkData.networkType = networkType
        // Update basic device info UI
        deviceIdOutput.text = deviceId
        macAddressOutput.text = macAddress
        timeStampOutput.text = timestamp

        // Update network data object with basic info
        networkData.deviceId = deviceId
        networkData.macAddress = macAddress
        networkData.timestamp = timestamp


        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Get cell ID using DeviceInfoUtil
            val cellId = DeviceInfoUtil.getCellId(this)

            if (cellId != -1) {
                cellIdOutput.text = cellId.toString()
                networkData.cellId = cellId.toString()
            } else {
                cellIdOutput.text = "Permission required"
                networkData.cellId = "Permission required"
            }


            // Get operator name
            val operatorName = DeviceInfoUtil.getOperatorName(this)
            operatorOutput.text = operatorName
            networkData.operator = operatorName

            // Get network type
            val networkType = DeviceInfoUtil.getNetworkType(this)
            networkTypeOutput.text = networkType
            networkData.networkType = networkType

            // Get frequency band
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
            networkTypeOutput.text = "Permission required"
            frequencyBandOutput.text = "Permission required"
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

            }
        }
    }


    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        isRefreshScheduled = false
    }

    override fun onResume() {
        super.onResume()
        // Check connection status when resuming
        checkConnectionAndUpdateUI()
    }

}
