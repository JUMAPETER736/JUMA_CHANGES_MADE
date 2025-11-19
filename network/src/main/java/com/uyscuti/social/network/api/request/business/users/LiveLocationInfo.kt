package com.uyscuti.social.network.api.request.business.users

data class LiveLocationInfo(
    val accuracy: String,
    val latitude: String,
    val longitude: String,
    val range: String
)