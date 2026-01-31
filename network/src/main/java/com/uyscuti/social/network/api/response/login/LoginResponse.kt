package com.uyscuti.social.network.api.response.login

import com.uyscuti.social.network.api.response.login.IData

data class LoginResponse(
    val `data`: IData,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)

// ============================================
// FORGOT PASSWORD MODELS
// ============================================

data class ForgotPasswordRequest(
    val email: String? = null,
    val username: String? = null,
    val userId: String? = null
)

//  data field is now NULLABLE
data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: ForgotPasswordData?
)

data class ForgotPasswordData(
    val resetToken: String,
    val email: String
)

// ============================================
// VERIFY OTP MODELS
// ============================================

data class VerifyOTPRequest(
    val resetToken: String,
    val otp: String
)

// data field is now NULLABLE
data class VerifyOTPResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: VerifyOTPData?
)

data class VerifyOTPData(
    val resetToken: String,
    val verified: Boolean
)

// ============================================
// RESET PASSWORD MODELS
// ============================================

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

data class ResetPasswordResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: Map<String, Any>? = null
)

// ============================================
// RESEND EMAIL MODELS
// ============================================

data class ResendEmailRequest(
    val email: String
)