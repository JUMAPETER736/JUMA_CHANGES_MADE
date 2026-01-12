package com.uyscuti.social.core.local.utils

import android.location.Location

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val time: Long = 0L,
    val provider: String? = null
) {
    // Constructor from Android Location
    constructor(location: Location) : this(
        latitude = location.latitude,
        longitude = location.longitude,
        altitude = location.altitude,
        accuracy = location.accuracy,
        time = location.time,
        provider = location.provider
    )

    // Convert back to Android Location
    fun toLocation(): Location {
        val location = Location(provider ?: "stored")
        location.latitude = latitude
        location.longitude = longitude
        location.altitude = altitude
        location.accuracy = accuracy
        location.time = time
        return location
    }
}
