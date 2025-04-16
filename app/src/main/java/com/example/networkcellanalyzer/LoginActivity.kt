package com.example.loginapp

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.networkcellanalyzer.R
import utils.ApiClient
import com.example.networkcellanalyzer.model.LoginRequest
import com.example.networkcellanalyzer.utils.SessionManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import utils.DeviceInfoUtil
import java.io.IOException
import android.Manifest


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

        // Initialize session manager
        sessionManager = SessionManager(this)

        // Check if user is already logged in
        if (sessionManager.getAuthToken() != null) {
            navigateToWelcome()
            return
        }

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewSignIn = findViewById(R.id.textViewSignIn)

        // Set click listeners
        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
            } else {
                // Call API for login
                loginUser(username, password)
            }
        }

        textViewSignIn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(username: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    // Save auth token and username
                    sessionManager.saveAuthToken(response.body()!!.token)
                    sessionManager.saveUsername(username)

                    // Immediately assign Device ID and MAC
                    val deviceId = DeviceInfoUtil.getDeviceId(this@LoginActivity)
                    val macAddress = DeviceInfoUtil.getMacAddress()
                    sessionManager.saveDeviceId(deviceId)
                    sessionManager.saveMacAddress(macAddress)

                    // Request permissions if not already granted
                    if (ActivityCompat.checkSelfPermission(
                            this@LoginActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(
                            this@LoginActivity,
                            Manifest.permission.READ_PHONE_STATE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@LoginActivity,
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_PHONE_STATE
                            ),
                            REQUEST_PERMISSIONS
                        )
                    } else {
                        assignRealCellId()
                    }

                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToWelcome()
                } else {
                    // For development - also handle testuser/1234 locally
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
                // Handle network error - allow test user as fallback in case backend is unavailable
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

    private fun navigateToWelcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun assignRealCellId() {
        val realCellId = DeviceInfoUtil.getCellId(this)
        sessionManager.saveCellId(realCellId.toString())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            assignRealCellId()
        } else {
            Toast.makeText(this, "Permissions denied. Cannot retrieve real cell ID.", Toast.LENGTH_SHORT).show()
        }
    }
}

/*package com.example.loginapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.networkcellanalyzer.R
import android.content.Intent


class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewSignIn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewSignIn = findViewById(R.id.textViewSignIn)

        // Set click listeners
        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
            } else {
                // dummy values just to check if it works
                if (username == "testuser" && password == "1234") {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, WelcomeActivity::class.java)
                    startActivity(intent)
                    finish()

                }
                else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }

                // Here you would typically validate credentials with your backend
               /* Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                // Navigate to welcome screen after successful login
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()*/

            }
        }

        textViewSignIn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)

        // Navigate to sign up screen
            //Toast.makeText(this, "Navigate to Sign Up screen", Toast.LENGTH_SHORT).show()
            // In a real app, you would use Intent to navigate to the SignUp activity
            // startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}*/