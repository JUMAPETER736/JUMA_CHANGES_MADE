package com.uyscuti.social.network.api.response.getmyprofile

data class GetMyProfile(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)