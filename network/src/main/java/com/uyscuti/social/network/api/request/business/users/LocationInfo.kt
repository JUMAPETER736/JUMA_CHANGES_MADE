package com.uyscut.network.api.request.business.users

data class LocationInfo(
    val accuracy: String,
    val latitude: String,
    val longitude: String,
    val range: String
)