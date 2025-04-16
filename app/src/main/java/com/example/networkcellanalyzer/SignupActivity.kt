package com.example.loginapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.networkcellanalyzer.R
import utils.ApiClient
import com.example.networkcellanalyzer.model.LoginRequest
import androidx.lifecycle.lifecycleScope
import com.example.networkcellanalyzer.model.SignupRequest
import kotlinx.coroutines.launch
import com.google.gson.Gson
import java.io.IOException

class SignUpActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonCreateAccount: Button
    private lateinit var textViewLogIn: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount)
        textViewLogIn = findViewById(R.id.textViewLogIn)
        progressBar = findViewById(R.id.progressBar)

        // Set click listeners
        buttonCreateAccount.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT)
                    .show()
            } else {
                registerUser(username, password)
            }
        }

        textViewLogIn.setOnClickListener {
            // Navigate back to login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser(username: String, password: String) {
        progressBar.visibility = View.VISIBLE
        buttonCreateAccount.isEnabled = false

        // Using LoginRequest for registration since the API accepts the same structure
        val request = LoginRequest(username, password)

        lifecycleScope.launch {
            try {
                // Call register endpoint with the same request type
                val response = ApiClient.apiService.signup(SignupRequest)

                progressBar.visibility = View.GONE
                buttonCreateAccount.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(this@SignUpActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Parse error response
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val errorResponse = Gson().fromJson(errorBody, Map::class.java)
                        errorResponse["error"] as? String ?: "Registration failed"
                    } catch (e: Exception) {
                        "Registration failed"
                    }
                    Toast.makeText(this@SignUpActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                progressBar.visibility = View.GONE
                buttonCreateAccount.isEnabled = true
                Toast.makeText(
                    this@SignUpActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}