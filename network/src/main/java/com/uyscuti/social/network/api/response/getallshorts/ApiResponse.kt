package com.uyscuti.social.network.api.response.getallshorts

data class ApiResponse<T>(
    val statusCode: Int,
    val data: T,
    val message: String,
    val success: Boolean
)
