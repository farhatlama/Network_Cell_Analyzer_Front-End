package com.example.loginapp

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
}