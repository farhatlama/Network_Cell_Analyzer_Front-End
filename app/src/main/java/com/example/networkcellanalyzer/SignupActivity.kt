package com.example.loginapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.networkcellanalyzer.R

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
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
            } else {
                // Dummy action: pretend we "created" the account
                Toast.makeText(this, "Account created for $username", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
         /*   else {
                // Here you would typically register the user with your backend
                Toast.makeText(this, "Account created successfully for: $username", Toast.LENGTH_SHORT).show()

                // After successful registration, navigate to login screen
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }*/
        }

        textViewLogIn.setOnClickListener {
            // Navigate back to login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}