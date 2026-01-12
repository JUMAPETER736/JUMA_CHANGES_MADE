package com.uyscuti.sharedmodule.utils

import android.content.Context
import android.location.LocationManager
import android.provider.Settings
import androidx.core.location.LocationManagerCompat

/**
 * Utility class for checking location service status on Android
 */
object LocationServiceUtil {

    /**
     * Checks if location services are enabled on the device
     *
     * @param context Application or Activity context
     * @return true if location services are enabled, false otherwise
     */
    fun isLocationEnabled(context: Context): Boolean {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            locationManager?.let {
                LocationManagerCompat.isLocationEnabled(it)
            } ?: false
        } catch (e: Exception) {
            // Handle any potential SecurityException or other exceptions
            false
        }
    }

    /**
     * Alternative method using Settings provider
     * Useful as a backup or for older Android versions
     *
     * @param context Application or Activity context
     * @return true if location services are enabled, false otherwise
     */
    fun isLocationEnabledViaSettings(context: Context): Boolean {
        return try {
            val locationMode = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE
            )
            locationMode != Settings.Secure.LOCATION_MODE_OFF
        } catch (e: Settings.SettingNotFoundException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if GPS provider is specifically enabled
     * Note: This requires location permissions to be granted
     *
     * @param context Application or Activity context
     * @return true if GPS provider is enabled, false otherwise
     */
    fun isGpsEnabled(context: Context): Boolean {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if Network provider is enabled (WiFi/cellular location)
     * Note: This requires location permissions to be granted
     *
     * @param context Application or Activity context
     * @return true if Network provider is enabled, false otherwise
     */
    fun isNetworkLocationEnabled(context: Context): Boolean {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets detailed location service status
     *
     * @param context Application or Activity context
     * @return LocationServiceStatus object with detailed information
     */
    fun getLocationServiceStatus(context: Context): LocationServiceStatus {
        return LocationServiceStatus(
            isLocationEnabled = isLocationEnabled(context),
            isGpsEnabled = isGpsEnabled(context),
            isNetworkLocationEnabled = isNetworkLocationEnabled(context)
        )
    }
}

/**
 * Data class representing the status of location services
 */
data class LocationServiceStatus(
    val isLocationEnabled: Boolean,
    val isGpsEnabled: Boolean,
    val isNetworkLocationEnabled: Boolean
) {
    /**
     * @return true if any location provider is available
     */
    val hasAnyLocationProvider: Boolean
        get() = isGpsEnabled || isNetworkLocationEnabled

    /**
     * @return human readable status string
     */
    override fun toString(): String {
        return when {
            !isLocationEnabled -> "Location services disabled"
            isGpsEnabled && isNetworkLocationEnabled -> "GPS and Network location enabled"
            isGpsEnabled -> "GPS enabled, Network location disabled"
            isNetworkLocationEnabled -> "Network location enabled, GPS disabled"
            else -> "Location enabled but no providers available"
        }
    }
}