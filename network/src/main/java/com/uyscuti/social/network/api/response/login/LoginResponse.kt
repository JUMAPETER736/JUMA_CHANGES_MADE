package com.uyscuti.social.network.api.response.login

import com.uyscuti.social.network.api.response.login.IData

data class LoginResponse(
    val `data`: IData,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)


data class ResetPasswordRequest(
    val token: String,  // Reset token from email
    val newPassword: String
)


data class ResetPasswordResponse(
    val message: String,
    val statusCode: Int,
    val success: Boolean
)


data class ForgotPasswordRequest(
    val email: String
)

data class VerifyOTPRequest(
    val resetToken: String,
    val otp: String
)


data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)