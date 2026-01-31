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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Forgot_Password : AppCompatActivity() {

    private val TAG = "Forgot_Password"
    private lateinit var forgotPasswordEmail: EditText
    private lateinit var btnContinue: AppCompatButton
    private var dialog: Dialog? = null

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    /**
     * Enum to represent different input types
     */
    enum class InputType {
        EMAIL,
        USERNAME,
        USER_ID
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        Log.d(TAG, "onCreate: Forgot Password activity started")

        // Initialize UI elements
        forgotPasswordEmail = findViewById(R.id.forgot_password_email)
        btnContinue = findViewById(R.id.btnContinue)

        // Set up button click listener
        btnContinue.setOnClickListener {
            val input = forgotPasswordEmail.text.toString().trim()
            Log.d(TAG, "Continue button clicked with input: '$input'")

            if (input.isEmpty()) {
                Log.w(TAG, "Input is empty")
                Toast.makeText(
                    this,
                    "Please enter your email, username, or user ID",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Proceed with password reset logic
                sendPasswordResetRequest(input)
            }
        }
    }

    private fun sendPasswordResetRequest(input: String) {
        Log.d(TAG, "sendPasswordResetRequest: Starting password reset for input: '$input'")
        showLoadingDialog()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Determine input type: email, username, or userId
                val inputType = determineInputType(input)
                Log.d(TAG, "Input type detected: $inputType")

                // Create request based on input type
                val request = when (inputType) {
                    InputType.EMAIL -> {
                        Log.d(TAG, "Creating request with EMAIL: $input")
                        ForgotPasswordRequest(email = input)
                    }
                    InputType.USER_ID -> {
                        Log.d(TAG, "Creating request with USER ID: $input")
                        ForgotPasswordRequest(userId = input)
                    }
                    InputType.USERNAME -> {
                        Log.d(TAG, "Creating request with USERNAME: $input")
                        ForgotPasswordRequest(username = input)
                    }
                }

                Log.d(TAG, "Request object created: $request")
                Log.d(TAG, "Making API call to forgotPassword endpoint...")

                val response = retrofitInstance.apiService.forgotPassword(request)

                Log.d(TAG, "Response received:")
                Log.d(TAG, "  - Response code: ${response.code()}")
                Log.d(TAG, "  - Is successful: ${response.isSuccessful}")
                Log.d(TAG, "  - Response body: ${response.body()}")
                Log.d(TAG, "  - Response message: ${response.message()}")

                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()

                    // Handle response based on HTTP status code
                    when (response.code()) {
                        200 -> {
                            // HTTP 200 - Check success flag in body
                            val body = response.body()
                            if (body?.success == true) {
                                // Success: Extract data
                                val data = body.data
                                val resetToken = data?.resetToken ?: ""
                                val email = data?.email ?: ""
                                val message = body.message

                                Log.d(TAG, "✅ SUCCESS (200)")
                                Log.d(TAG, "  - Success: ${body.success}")
                                Log.d(TAG, "  - Message: $message")
                                Log.d(TAG, "  - StatusCode: ${body.statusCode}")
                                Log.d(TAG, "  - Reset Token: $resetToken")
                                Log.d(TAG, "  - Email: $email")

                                if (resetToken.isNotEmpty() && email.isNotEmpty()) {
                                    Toast.makeText(
                                        this@Forgot_Password,
                                        message,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Navigate to Verification Code page
                                    Log.d(TAG, "🚀 Navigating to Verification_Code activity")
                                    val intent = Intent(this@Forgot_Password, Verification_Code::class.java)
                                    intent.putExtra("RESET_TOKEN", resetToken)
                                    intent.putExtra("EMAIL", email)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Log.e(TAG, "❌ Missing resetToken or email in response data")
                                    Toast.makeText(
                                        this@Forgot_Password,
                                        "Invalid response from server. Please try again.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                // HTTP 200 but success = false (shouldn't happen with your backend)
                                val message = body?.message ?: "Request failed"
                                Log.e(TAG, "❌ HTTP 200 but success=false")
                                Log.e(TAG, "  - Message: $message")

                                Toast.makeText(
                                    this@Forgot_Password,
                                    message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        400 -> {
                            // Bad Request - Parse error from body
                            handleErrorFromBody(
                                response.errorBody()?.string(),
                                400,
                                "Please provide email, username, or user ID"
                            )
                        }

                        404 -> {
                            // User Not Found - Parse error from body
                            handleErrorFromBody(
                                response.errorBody()?.string(),
                                404,
                                "User not found. Please check your email, username, or user ID."
                            )
                        }

                        422 -> {
                            // Unprocessable Entity
                            handleErrorFromBody(
                                response.errorBody()?.string(),
                                422,
                                "Cannot process request. Please check your input."
                            )
                        }

                        500 -> {
                            // Server Error
                            handleErrorFromBody(
                                response.errorBody()?.string(),
                                500,
                                "Server error. Please try again later."
                            )
                        }

                        else -> {
                            // Other HTTP codes
                            val code = response.code()
                            val message = response.message()

                            Log.e(TAG, "❌ HTTP ERROR ($code)")
                            Log.e(TAG, "  - Message: $message")

                            Toast.makeText(
                                this@Forgot_Password,
                                "Error ($code). Please try again.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: HttpException) {
                Log.e(TAG, "❌ HTTP Exception occurred!")
                Log.e(TAG, "  - Message: ${e.message}")
                Log.e(TAG, "  - Code: ${e.code()}")

                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()

                    // Try to parse error body
                    val errorBody = try {
                        e.response()?.errorBody()?.string()
                    } catch (ex: Exception) {
                        null
                    }

                    handleErrorFromBody(
                        errorBody,
                        e.code(),
                        when (e.code()) {
                            400 -> "Please provide email, username, or user ID"
                            404 -> "User not found. Please check your email, username, or user ID."
                            422 -> "Cannot process request. Please check your input."
                            500 -> "Server error. Please try again later."
                            else -> "Error (${e.code()}). Please try again."
                        }
                    )
                }
            } catch (e: IOException) {
                Log.e(TAG, "❌ IO Exception occurred!")
                Log.e(TAG, "  - Message: ${e.message}")
                Log.e(TAG, "  - Cause: ${e.cause}")
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@Forgot_Password,
                        "Network error. Check your internet connection.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected Exception occurred!")
                Log.e(TAG, "  - Type: ${e.javaClass.simpleName}")
                Log.e(TAG, "  - Message: ${e.message}")
                Log.e(TAG, "  - Cause: ${e.cause}")
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@Forgot_Password,
                        "An error occurred: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Handles error response from body by parsing JSON
     */
    private fun handleErrorFromBody(errorBody: String?, statusCode: Int, fallbackMessage: String) {
        try {
            errorBody?.let {
                val jsonObject = org.json.JSONObject(it)
                val success = jsonObject.optBoolean("success", false)
                val message = jsonObject.optString("message", fallbackMessage)
                val code = jsonObject.optInt("statusCode", statusCode)

                Log.e(TAG, "❌ ERROR ($statusCode)")
                Log.e(TAG, "  - Success: $success")
                Log.e(TAG, "  - Message: $message")
                Log.e(TAG, "  - StatusCode: $code")

                Toast.makeText(
                    this@Forgot_Password,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            } ?: run {
                // No error body, use fallback
                Log.e(TAG, "❌ ERROR ($statusCode) - No error body")
                Toast.makeText(
                    this@Forgot_Password,
                    fallbackMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing error response: ${e.message}")
            Toast.makeText(
                this@Forgot_Password,
                fallbackMessage,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Determines the type of input provided by the user
     * Priority: Email > User ID (MongoDB ObjectID) > Username
     */
    private fun determineInputType(input: String): InputType {
        return when {
            // Check if it's a valid email
            isValidEmail(input) -> {
                Log.d(TAG, "Input identified as EMAIL")
                InputType.EMAIL
            }
            // Check if it's a MongoDB ObjectID (24 hex characters)
            isValidObjectId(input) -> {
                Log.d(TAG, "Input identified as USER_ID (ObjectID)")
                InputType.USER_ID
            }
            // Otherwise treat as username
            else -> {
                Log.d(TAG, "Input identified as USERNAME")
                InputType.USERNAME
            }
        }
    }

    /**
     * Validates if the input is a valid email address
     */
    private fun isValidEmail(input: String): Boolean {
        val result = Patterns.EMAIL_ADDRESS.matcher(input).matches()
        Log.d(TAG, "isValidEmail check for '$input': $result")
        return result
    }

    /**
     * Validates if the input is a valid MongoDB ObjectID
     * MongoDB ObjectID is 24 hexadecimal characters
     */
    private fun isValidObjectId(input: String): Boolean {
        val objectIdPattern = "^[a-fA-F0-9]{24}$".toRegex()
        val result = objectIdPattern.matches(input)
        Log.d(TAG, "isValidObjectId check for '$input': $result")
        return result
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
        dialog?.dismiss()
        dialog = null
    }
}