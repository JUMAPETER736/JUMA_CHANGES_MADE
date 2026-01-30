package com.uyscuti.social.network.api.response.login

import com.uyscuti.social.network.api.response.login.IData

data class LoginResponse(
    val `data`: IData,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)


data class ForgotPasswordRequest(
    val email: String? = null,
    val username: String? = null
)

data class VerifyOTPRequest(
    val resetToken: String,
    val otp: String
)


data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

data class ResendEmailRequest(
    val email: String
)


data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: ForgotPasswordData
)

data class ForgotPasswordData(
    val resetToken: String,
    val email: String
)


data class VerifyOTPResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: VerifyOTPData
)

data class VerifyOTPData(
    val resetToken: String,
    val verified: Boolean
)

data class ResetPasswordResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: Map<String, Any> = emptyMap()
)

