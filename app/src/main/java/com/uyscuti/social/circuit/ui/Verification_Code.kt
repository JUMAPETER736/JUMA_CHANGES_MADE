package com.uyscuti.social.circuit.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.uyscuti.social.circuit.R
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.api.response.login.ForgotPasswordRequest
import com.uyscuti.social.network.api.response.login.VerifyOTPRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class Verification_Code : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvInstructions: TextView
    private lateinit var otp1: EditText
    private lateinit var otp2: EditText
    private lateinit var otp3: EditText
    private lateinit var otp4: EditText
    private lateinit var otp5: EditText
    private lateinit var otp6: EditText
    private lateinit var tvCountdownTimer: TextView
    private lateinit var btnContinue: AppCompatButton
    private lateinit var btnResendCode: TextView

    private var timerValue = 120 // 2 minutes in seconds
    private var timerRunning = false
    private val timerHandler = android.os.Handler()
    private var dialog: Dialog? = null

    // ADD THESE VARIABLES
    private var resetToken: String? = null
    private var email: String? = null

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    //: Changed to 6 OTP fields (backend sends 6-digit OTP)
    val otpFields = arrayOf(R.id.otp1, R.id.otp2, R.id.otp3, R.id.otp4, R.id.otp5, R.id.otp6)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_code)

        // GET DATA FROM INTENT
        resetToken = intent.getStringExtra("RESET_TOKEN")
        email = intent.getStringExtra("EMAIL")

        if (resetToken.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid reset token", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        tvTitle = findViewById(R.id.tvTitle)
        tvInstructions = findViewById(R.id.tvInstructions)
        otp1 = findViewById(R.id.otp1)
        otp2 = findViewById(R.id.otp2)
        otp3 = findViewById(R.id.otp3)
        otp4 = findViewById(R.id.otp4)
        otp5 = findViewById(R.id.otp5)
        otp6 = findViewById(R.id.otp6)
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

    //  Function to handle OTP field auto-move focus (6 fields)
    private fun setOTPFocusListener() {
        otp1.addTextChangedListener(createTextWatcher(otp1))
        otp2.addTextChangedListener(createTextWatcher(otp2))
        otp3.addTextChangedListener(createTextWatcher(otp3))
        otp4.addTextChangedListener(createTextWatcher(otp4))
        otp5.addTextChangedListener(createTextWatcher(otp5))
        otp6.addTextChangedListener(createTextWatcher(otp6))
    }

    // : Helper function to create TextWatcher
    private fun createTextWatcher(currentField: EditText): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 1) {
                    moveToNextField(currentField)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    //  Function to move to the next OTP field (6 fields)
    private fun moveToNextField(currentField: EditText) {
        val currentIndex = when (currentField.id) {
            R.id.otp1 -> 0
            R.id.otp2 -> 1
            R.id.otp3 -> 2
            R.id.otp4 -> 3
            R.id.otp5 -> 4
            R.id.otp6 -> 5
            else -> -1
        }

        if (currentIndex != -1 && currentIndex < 5) {
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

    // ✅ UPDATED: Handle continue button click with API call
    private fun onContinueClicked() {
        val otp = getOTP()
        if (otp.length == 6) { // ✅ Changed from 4 to 6
            verifyOTP(otp)
        } else {
            Toast.makeText(this, "Please enter a valid 6-digit verification code", Toast.LENGTH_SHORT).show()
        }
    }

    // : Verify OTP with backend
    private fun verifyOTP(otp: String) {
        showLoadingDialog()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = VerifyOTPRequest(resetToken!!, otp)
                val response = retrofitInstance.apiService.verifyOTP(request)

                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()

                    if (response.isSuccessful && response.body()?.success == true) {
                        val verifiedToken = response.body()?.data?.resetToken ?: ""

                        Toast.makeText(
                            this@Verification_Code,
                            "OTP verified successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to Create New Password screen
                        val intent = Intent(this@Verification_Code, Create_New_Password::class.java)
                        intent.putExtra("RESET_TOKEN", verifiedToken)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@Verification_Code,
                            response.body()?.message ?: "Invalid or expired OTP",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: HttpException) {
                Log.e("VerifyOTP", "HTTP Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@Verification_Code,
                        "HTTP error. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                Log.e("VerifyOTP", "IO Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@Verification_Code,
                        "Network error. Check your connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("VerifyOTP", "Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@Verification_Code,
                        "An error occurred. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    //  Get OTP value from all 6 input fields
    private fun getOTP(): String {
        return otp1.text.toString() +
                otp2.text.toString() +
                otp3.text.toString() +
                otp4.text.toString() +
                otp5.text.toString() +
                otp6.text.toString()
    }

    // Handle resend code with API call
    private fun onResendCodeClicked() {
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
            return
        }

        showLoadingDialog()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = ForgotPasswordRequest(email!!)
                val response = retrofitInstance.apiService.forgotPassword(request)

                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()

                    if (response.isSuccessful && response.body()?.success == true) {
                        resetToken = response.body()?.data?.resetToken ?: ""

                        Toast.makeText(
                            this@Verification_Code,
                            "New OTP sent to your email",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Reset the timer and restart
                        timerValue = 120
                        startCountdownTimer()

                        // Clear OTP fields
                        clearOTPFields()
                    } else {
                        Toast.makeText(
                            this@Verification_Code,
                            "Failed to resend OTP. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ResendOTP", "Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@Verification_Code,
                        "Error resending OTP. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    //  Clear all OTP fields
    private fun clearOTPFields() {
        otp1.text.clear()
        otp2.text.clear()
        otp3.text.clear()
        otp4.text.clear()
        otp5.text.clear()
        otp6.text.clear()
        otp1.requestFocus()
    }

    // : Loading dialog functions
    private fun showLoadingDialog() {
        if (dialog == null) {
            dialog = Dialog(this)
            dialog?.setContentView(R.layout.loading_dialog)
            dialog?.setCancelable(false)
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog?.show()
    }

    private fun dismissLoadingDialog() {
        dialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerHandler.removeCallbacksAndMessages(null)
        dialog?.dismiss()
        dialog = null
    }
}