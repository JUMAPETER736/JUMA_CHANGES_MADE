package com.uyscuti.social.circuit.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.uyscuti.social.circuit.R
import com.uyscuti.social.network.api.response.login.ForgotPasswordRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject


class Forgot_Password : AppCompatActivity() {

    private lateinit var forgotPasswordEmail: EditText
    private lateinit var btnContinue: AppCompatButton
    private var dialog: Dialog? = null

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

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
        showLoadingDialog()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = ForgotPasswordRequest(email)
                val response = retrofitInstance.apiService.forgotPassword(request)

                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()

                    if (response.isSuccessful && response.body()?.success == true) {
                        val resetToken = response.body()?.data?.resetToken ?: ""

                        Toast.makeText(
                            this@Forgot_Password,
                            "Password reset OTP sent to $email",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Redirect to verification code page
                        val intent = Intent(this@Forgot_Password, Verification_Code::class.java)
                        intent.putExtra("RESET_TOKEN", resetToken)
                        intent.putExtra("EMAIL", email)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@Forgot_Password,
                            response.body()?.message ?: "Failed to send OTP. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: HttpException) {
                Log.e("ForgotPassword", "HTTP Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@Forgot_Password,
                        "HTTP error. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                Log.e("ForgotPassword", "IO Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@Forgot_Password,
                        "Network error. Check your internet connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("ForgotPassword", "Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@Forgot_Password,
                        "An error occurred. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

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
        dialog?.dismiss()
        dialog = null
    }
}