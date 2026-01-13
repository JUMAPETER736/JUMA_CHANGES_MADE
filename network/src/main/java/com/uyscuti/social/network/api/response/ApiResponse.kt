package com.uyscuti.social.network.api.response

data class ApiResponse(
    val status: Int,
    val data: Any,  // Use the appropriate type for your data, e.g., a Map or a specific class
    val message: String,
)
