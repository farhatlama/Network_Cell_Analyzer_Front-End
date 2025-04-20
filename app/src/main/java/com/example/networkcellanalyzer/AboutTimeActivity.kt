package com.example.networkcellanalyzer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class AboutTimeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_time)

        val toolbar = findViewById<Toolbar>(R.id.aboutTimeToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Close button action
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
