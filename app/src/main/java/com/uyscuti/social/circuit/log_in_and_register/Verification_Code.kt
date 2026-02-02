package com.uyscuti.social.circuit.log_in_and_register

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Verification_Code : AppCompatActivity() {

    private val TAG = "Verification_Code"

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

    private var timerValue = 300
    private var timerRunning = false
    private val timerHandler = Handler()
    private var dialog: Dialog? = null

    private var resetToken: String? = null
    private var email: String? = null

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    val otpFields = arrayOf(R.id.otp1, R.id.otp2, R.id.otp3, R.id.otp4, R.id.otp5, R.id.otp6)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_code)


        Log.d(TAG, "onCreate: Verification Code activity started")

        // GET DATA FROM INTENT
        resetToken = intent.getStringExtra("RESET_TOKEN")
        email = intent.getStringExtra("EMAIL")

        Log.d(TAG, "Reset Token: $resetToken")
        Log.d(TAG, "Email: $email")


        if (resetToken.isNullOrEmpty()) {
            Log.e(TAG, "Invalid reset token - finishing activity")
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

        Log.d(TAG, "View initialization complete")
    }

    private fun setOTPFocusListener() {
        otp1.addTextChangedListener(createTextWatcher(otp1))
        otp2.addTextChangedListener(createTextWatcher(otp2))
        otp3.addTextChangedListener(createTextWatcher(otp3))
        otp4.addTextChangedListener(createTextWatcher(otp4))
        otp5.addTextChangedListener(createTextWatcher(otp5))
        otp6.addTextChangedListener(createTextWatcher(otp6))
    }

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

    private fun onContinueClicked() {
        val otp = getOTP()


        Log.d(TAG, "Continue button clicked")
        Log.d(TAG, "OTP entered: '$otp'")
        Log.d(TAG, "OTP length: ${otp.length}")


        if (otp.length == 6) {
            verifyOTP(otp)
        } else {
            Log.w(TAG, "Invalid OTP length: ${otp.length}")
            Toast.makeText(this, "Please enter a valid 6-digit verification code", Toast.LENGTH_SHORT).show()
        }
    }

    //Detailed logging for OTP verification
    private fun verifyOTP(otp: String) {

        Log.d(TAG, "VERIFY OTP CALLED")
        Log.d(TAG, "  OTP: '$otp'")
        Log.d(TAG, "  OTP Length: ${otp.length}")
        Log.d(TAG, "  OTP Type: String")
        Log.d(TAG, "  Reset Token: '$resetToken'")
        Log.d(TAG, "  Reset Token Length: ${resetToken?.length ?: 0}")


        showLoadingDialog()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = VerifyOTPRequest(resetToken!!, otp)

                Log.d(TAG, "Sending request to API")
                Log.d(TAG, "  Request: $request")

                val response = retrofitInstance.apiService.verifyOTP(request)

                Log.d(TAG, "Response received")
                Log.d(TAG, "  HTTP Code: ${response.code()}")
                Log.d(TAG, "  Is Successful: ${response.isSuccessful}")
                Log.d(TAG, "  Response Message: ${response.message()}")
                Log.d(TAG, "  Response Body: ${response.body()}")

                //Log error body if present
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error Body: $errorBody")
                }

                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()

                    if (response.isSuccessful && response.body()?.success == true) {
                        val verifiedToken = response.body()?.data?.resetToken ?: ""

                        Log.d(TAG, "✅ OTP VERIFIED SUCCESSFULLY")
                        Log.d(TAG, "  - Verified Token: $verifiedToken")

                        Toast.makeText(
                            this@Verification_Code,
                            "OTP verified successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to Create New Password screen
                        Log.d(TAG, "🚀 Navigating to Create_New_Password")
                        val intent = Intent(this@Verification_Code, Create_New_Password::class.java)
                        intent.putExtra("RESET_TOKEN", verifiedToken)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = response.body()?.message ?: "Invalid or expired OTP"

                        Log.e(TAG, "❌ OTP VERIFICATION FAILED")
                        Log.e(TAG, "  - Error Message: $errorMessage")
                        Log.e(TAG, "  - Success Flag: ${response.body()?.success}")
                        Log.e(TAG, "  - Status Code: ${response.body()?.statusCode}")

                        Toast.makeText(
                            this@Verification_Code,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: HttpException) {
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "❌ HTTP EXCEPTION")
                Log.e(TAG, "  - Message: ${e.message}")
                Log.e(TAG, "  - Code: ${e.code()}")

                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e(TAG, "  - Error Body: $errorBody")
                } catch (ex: Exception) {
                    Log.e(TAG, "  - Could not read error body: ${ex.message}")
                }

                Log.e(TAG, "═══════════════════════════════════════")

                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@Verification_Code,
                        "HTTP error (${e.code()}). Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "❌ IO EXCEPTION (Network Error)")
                Log.e(TAG, "  - Message: ${e.message}")
                Log.e(TAG, "  - Cause: ${e.cause}")
                e.printStackTrace()
                Log.e(TAG, "═══════════════════════════════════════")

                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@Verification_Code,
                        "Network error. Check your connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "❌ UNEXPECTED EXCEPTION")
                Log.e(TAG, "  - Type: ${e.javaClass.simpleName}")
                Log.e(TAG, "  - Message: ${e.message}")
                Log.e(TAG, "  - Cause: ${e.cause}")
                e.printStackTrace()
                Log.e(TAG, "═══════════════════════════════════════")

                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@Verification_Code,
                        "An error occurred: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun getOTP(): String {
        return otp1.text.toString() +
                otp2.text.toString() +
                otp3.text.toString() +
                otp4.text.toString() +
                otp5.text.toString() +
                otp6.text.toString()
    }

    private fun onResendCodeClicked() {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "Resend Code button clicked")
        Log.d(TAG, "Email: $email")
        Log.d(TAG, "═══════════════════════════════════════")

        if (email.isNullOrEmpty()) {
            Log.e(TAG, "❌ Email not found")
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
            return
        }

        showLoadingDialog()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "📤 Requesting new OTP for: $email")

                val request = ForgotPasswordRequest(email = email!!)
                val response = retrofitInstance.apiService.forgotPassword(request)

                Log.d(TAG, "📥 Resend OTP response received")
                Log.d(TAG, "  - HTTP Code: ${response.code()}")
                Log.d(TAG, "  - Success: ${response.body()?.success}")
                Log.d(TAG, "  - Message: ${response.body()?.message}")

                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()

                    if (response.isSuccessful && response.body()?.success == true) {
                        resetToken = response.body()?.data?.resetToken ?: ""

                        Log.d(TAG, "✅ New OTP sent successfully")
                        Log.d(TAG, "  - New Reset Token: $resetToken")

                        Toast.makeText(
                            this@Verification_Code,
                            "New OTP sent to your email",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Reset the timer and restart
                        timerValue = 300
                        startCountdownTimer()

                        // Clear OTP fields
                        clearOTPFields()
                    } else {
                        Log.e(TAG, "❌ Failed to resend OTP")
                        Toast.makeText(
                            this@Verification_Code,
                            "Failed to resend OTP. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception while resending OTP: ${e.message}")
                e.printStackTrace()

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

    private fun clearOTPFields() {
        Log.d(TAG, "Clearing OTP fields")
        otp1.text.clear()
        otp2.text.clear()
        otp3.text.clear()
        otp4.text.clear()
        otp5.text.clear()
        otp6.text.clear()
        otp1.requestFocus()
    }

    private fun showLoadingDialog() {
        Log.d(TAG, "Showing loading dialog")
        if (dialog == null) {
            dialog = Dialog(this)
            dialog?.setContentView(R.layout.loading_dialog)
            dialog?.setCancelable(false)
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog?.show()
    }

    private fun dismissLoadingDialog() {
        Log.d(TAG, "Dismissing loading dialog")
        dialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Cleaning up")
        timerHandler.removeCallbacksAndMessages(null)
        dialog?.dismiss()
        dialog = null
    }
}