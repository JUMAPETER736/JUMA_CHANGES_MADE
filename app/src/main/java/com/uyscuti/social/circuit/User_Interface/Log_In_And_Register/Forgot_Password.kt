package com.uyscuti.social.circuit.User_Interface.Log_In_And_Register

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.uyscuti.social.circuit.R

class Forgot_Password : AppCompatActivity() {

    private lateinit var forgotPasswordEmail: EditText
    private lateinit var btnContinue: AppCompatButton


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password) // Ensure XML file is named correctly

        // Initialize UI elements
        forgotPasswordEmail = findViewById(R.id.forgot_password_email)
        btnContinue = findViewById(R.id.btnContinue)


        // Set up button click listener
        btnContinue.setOnClickListener {
            val email = forgotPasswordEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter your email",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!isValidEmail(email)) {
                Toast.makeText(
                    this,
                    "Please enter a valid email address",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Proceed with password reset logic
                sendPasswordResetEmail(email)
            }
        }
    }

    // Function to check if the entered email is valid
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun sendPasswordResetEmail(email: String) {
        // Simulate sending a password reset request
        Toast.makeText(
            this,
            "Password reset link sent to $email", Toast.LENGTH_SHORT
        ).show()

        // Delay the transition to Verification_Code activity
        Handler(Looper.getMainLooper()).postDelayed({
            // Redirect to verification code page
            val intent = Intent(this, Verification_Code::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}