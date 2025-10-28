package com.uyscuti.social.circuit.User_Interface.Log_In_And_Register

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.uyscuti.social.circuit.R

class Password_Reset_Success : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.password_reset_success) // Ensure this matches your XML filename

        // Find the Log In button
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Set click listener
        btnLogin.setOnClickListener {
            // Navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Optional: Close current activity
        }
    }
}
