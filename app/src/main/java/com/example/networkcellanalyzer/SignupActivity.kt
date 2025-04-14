package com.example.loginapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.networkcellanalyzer.R
import com.example.networkcellanalyzer.api.RetrofitClient
import com.example.networkcellanalyzer.api.models.RegisterRequest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class SignUpActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonCreateAccount: Button
    private lateinit var textViewLogIn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount)
        textViewLogIn = findViewById(R.id.textViewLogIn)

        // Set click listeners
        buttonCreateAccount.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT)
                    .show()
            } else {
                RegisterUser(username, password)
            }
        }
         /*   else {
                // Here you would typically register the user with your backend
                Toast.makeText(this, "Account created successfully for: $username", Toast.LENGTH_SHORT).show()

                // After successful registration, navigate to login screen
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }*/


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

        val registerRequest = RegisterRequest(username, password)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.registerUser(registerRequest)

                withContext(Dispatchers.Main) {
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
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {

                    buttonCreateAccount.isEnabled = true
                    Toast.makeText(
                        this@SignUpActivity,
                        "Network error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
}