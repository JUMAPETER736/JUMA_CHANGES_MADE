package com.uyscuti.social.circuit.User_Interface.Log_In_And_Register

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.uyscuti.social.circuit.R


class Verification_Code : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvInstructions: TextView
    private lateinit var otp1: EditText
    private lateinit var otp2: EditText
    private lateinit var otp3: EditText
    private lateinit var otp4: EditText
    private lateinit var tvCountdownTimer: TextView
    private lateinit var btnContinue: AppCompatButton
    private lateinit var btnResendCode: TextView

    private var timerValue = 120 // 2 minutes in seconds
    private var timerRunning = false
    private val timerHandler = android.os.Handler()

    val otpFields = arrayOf(R.id.otp1, R.id.otp2, R.id.otp3, R.id.otp4)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verification_code)

        // Initialize views
        tvTitle = findViewById(R.id.tvTitle)
        tvInstructions = findViewById(R.id.tvInstructions)
        otp1 = findViewById(R.id.otp1)
        otp2 = findViewById(R.id.otp2)
        otp3 = findViewById(R.id.otp3)
        otp4 = findViewById(R.id.otp4)
        tvCountdownTimer = findViewById(R.id.tvCountdownTimer)
        btnContinue = findViewById(R.id.btnContinue)
        btnResendCode = findViewById(R.id.btnResendCode)



        // Set up listeners
        btnContinue.setOnClickListener { onContinueClicked() }
        btnResendCode.setOnClickListener { onResendCodeClicked() }

        // Start the countdown timer
        startCountdownTimer()

        // Set OTP fields to move focus automatically
        setOTPFocusListener()
    }

    // Function to handle OTP field auto-move focus
    private fun setOTPFocusListener() {
        otp1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                moveToNextField(otp1)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        otp2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                moveToNextField(otp2)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        otp3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                moveToNextField(otp3)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        otp4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                moveToNextField(otp4)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Function to move to the next OTP field after entering a digit
    private fun moveToNextField(currentField: EditText) {
        val currentIndex = when (currentField.id) {
            R.id.otp1 -> 0
            R.id.otp2 -> 1
            R.id.otp3 -> 2
            R.id.otp4 -> 3
            else -> -1
        }

        if (currentIndex != -1 && currentIndex < 3) {
            val nextField = findViewById<EditText>(otpFields[currentIndex + 1])
            nextField.requestFocus()
        }
    }

    // Start countdown timer
    private fun startCountdownTimer() {
        if (!timerRunning) {
            timerRunning = true
            timerHandler.post(object : Runnable {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun run() {
                    if (timerValue > 0) {
                        val minutes = timerValue / 60
                        val seconds = timerValue % 60
                        tvCountdownTimer.text = String.format("Time remaining: %02d:%02d", minutes, seconds)
                        timerValue--
                        timerHandler.postDelayed(this, 1000)
                    } else {
                        timerRunning = false
                        tvCountdownTimer.text = "Time's up!"
                    }
                }
            })
        }
    }

    // Handle continue button click
    private fun onContinueClicked() {
        val otp = getOTP()
        if (otp.length == 4) {
            // Handle verification logic here (e.g., send OTP to server)
            Toast.makeText(this, "Verification successful", Toast.LENGTH_SHORT).show()

            // Open Create New Password
            val intent = Intent(this, Create_New_Password::class.java)
            startActivity(intent)
        }

        else {
            Toast.makeText(this, "Please enter a valid 4-digit verification code", Toast.LENGTH_SHORT).show()
        }
    }

    // Get OTP value from all input fields
    private fun getOTP(): String {
        return otp1.text.toString() + otp2.text.toString() + otp3.text.toString() + otp4.text.toString()
    }

    // Handle resend code click
    private fun onResendCodeClicked() {
        // Implement resend OTP logic here
        Toast.makeText(this, "Resending verification code...", Toast.LENGTH_SHORT).show()

        // Reset the timer and restart
        timerValue = 120
        startCountdownTimer()
    }


}