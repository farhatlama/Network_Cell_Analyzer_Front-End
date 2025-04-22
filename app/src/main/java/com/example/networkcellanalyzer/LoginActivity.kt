package com.example.loginapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.networkcellanalyzer.R
import com.example.loginapp.WelcomeActivity
import com.example.networkcellanalyzer.model.LoginRequest
import com.example.networkcellanalyzer.utils.SessionManager
import kotlinx.coroutines.launch
import utils.ApiClient
import utils.DeviceInfoUtil
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewSignIn: TextView
    private lateinit var sessionManager: SessionManager

    companion object {
        private const val REQUEST_PERMISSIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        // If the user if already logged in
        if (sessionManager.getAuthToken() != null) {
            navigateToWelcome()
            return
        }

        // Init UI
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewSignIn = findViewById(R.id.textViewSignIn)

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(username, password)
            }
        }

        textViewSignIn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun loginUser(username: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    sessionManager.saveAuthToken(response.body()!!.token)
                    sessionManager.saveUsername(username)

                    // Save Device ID once
                    var deviceId = sessionManager.getDeviceId()
                    if (deviceId == null) {
                        deviceId = DeviceInfoUtil.getDeviceId(this@LoginActivity)
                        sessionManager.saveDeviceId(deviceId)
                    }

                    handlePermissionsOrProceed()
                } else {
                    // Allow test fallback
                    if (username == "testuser" && password == "1234") {
                        sessionManager.saveUsername(username)
                        sessionManager.saveAuthToken("dummy-token-for-testing")
                        Toast.makeText(this@LoginActivity, "Login successful (test mode)!", Toast.LENGTH_SHORT).show()
                        navigateToWelcome()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                // Offline fallback
                if (username == "testuser" && password == "1234") {
                    sessionManager.saveUsername(username)
                    sessionManager.saveAuthToken("dummy-token-for-testing")
                    Toast.makeText(this@LoginActivity, "Login successful (offline mode)!", Toast.LENGTH_SHORT).show()
                    navigateToWelcome()
                } else {
                    Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handlePermissionsOrProceed() {
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
                ),
                REQUEST_PERMISSIONS
            )
        } else {
            assignRealCellId()
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            navigateToWelcome()
        }
    }

    private fun assignRealCellId() {
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        ) {
            val realCellId = DeviceInfoUtil.getCellId(this)
            sessionManager.saveCellId(realCellId.toString())
        } else {
            Toast.makeText(this, "Permission denied: Cannot access Cell ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToWelcome() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}
