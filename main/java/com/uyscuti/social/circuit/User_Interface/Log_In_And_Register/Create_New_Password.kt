package com.uyscuti.social.circuit.User_Interface.Log_In_And_Register

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.uyscuti.social.circuit.R

class Create_New_Password : AppCompatActivity() {

    // Declare views
    private lateinit var tvCreateNewPassword: TextView
    private lateinit var tvPasswordGuidelines: TextView
    private lateinit var tvNewPasswordLabel: TextView
    private lateinit var etNewPassword: EditText
    private lateinit var ivNewPasswordToggle: ImageView
    private lateinit var tvConfirmPasswordLabel: TextView
    private lateinit var etConfirmPassword: EditText
    private lateinit var ivConfirmPasswordToggle: ImageView
    private lateinit var btnResetPassword: AppCompatButton

    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_new_password)

        // Initialize views
        tvCreateNewPassword = findViewById(R.id.tvCreateNewPassword)
        tvPasswordGuidelines = findViewById(R.id.tvPasswordGuidelines)
        tvNewPasswordLabel = findViewById(R.id.tvNewPasswordLabel)
        etNewPassword = findViewById(R.id.New_Password_Label)
        ivNewPasswordToggle = findViewById(R.id.New_Password)
        tvConfirmPasswordLabel = findViewById(R.id.Confirm_Password_Label)
        etConfirmPassword = findViewById(R.id.Confirm_New_Password_Label)
        ivConfirmPasswordToggle = findViewById(R.id.Confirm_New_Password)
        btnResetPassword = findViewById(R.id.btnResetPassword)

        // Set the default input type for passwords (hidden)
        etNewPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        // Set default icon for the password toggle (hidden)
        ivNewPasswordToggle.setImageResource(R.drawable.ic_eye_close)
        ivConfirmPasswordToggle.setImageResource(R.drawable.ic_eye_close)

        // Set click listener for new password visibility toggle
        ivNewPasswordToggle.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            togglePasswordVisibility(etNewPassword, ivNewPasswordToggle, isNewPasswordVisible)
        }

        // Set click listener for confirm password visibility toggle
        ivConfirmPasswordToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(etConfirmPassword, ivConfirmPasswordToggle, isConfirmPasswordVisible)
        }

        // Set click listener for the reset password button
        btnResetPassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            // Check if the input fields are empty
            if (newPassword.isEmpty()) {
                etNewPassword.error = "New password cannot be empty"
            } else if (confirmPassword.isEmpty()) {
                etConfirmPassword.error = "Confirm password cannot be empty"
            } else if (newPassword != confirmPassword) {
                // Check if the passwords match
                etNewPassword.error = "Passwords do not match"
                etConfirmPassword.error = "Passwords do not match"
            } else if (!isPasswordValid(newPassword)) {
                // Check if the new password meets the criteria
                etNewPassword.error = "Password must be at least 8 characters, contain one uppercase letter, one lowercase letter, one digit, and one special character."
            } else {
                // Proceed with password reset logic
                resetPassword(newPassword)
            }
        }
    }

    // Function to toggle password visibility
    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imageView.setImageResource(R.drawable.ic_eye_open) // Show eye open icon
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(R.drawable.ic_eye_close) // Show eye closed icon
        }
        editText.setSelection(editText.text.length) // Move cursor to the end
    }

    // Function to validate password
    private fun isPasswordValid(password: String): Boolean {
        // Regex to check password requirements
        val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    // Function to handle password reset logic
    private fun resetPassword(newPassword: String) {
        // Update the password in your database or backend
        val intent = Intent(this, Password_Reset_Success::class.java) // Redirect to a success page
        startActivity(intent)
        finish()
    }
}