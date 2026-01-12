package com.uyscuti.social.core.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.uyscuti.social.core.R
import com.uyscuti.social.core.local.utils.LocationData
import com.uyscuti.social.core.util.LocationServiceUtil
import com.uyscuti.social.core.util.NetworkUtil
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import javax.inject.Inject

@AndroidEntryPoint
class LocationService  : Service() {

    private val binder = LocationBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest


    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    @Inject
    lateinit var localStorage: LocalStorage

    private var hasBusinessProfile = false

    private var isBillBoardLocationEnabled = false

    private var range: Int? = null

    private var locationSharedPreferences: SharedPreferences? = null

    private val LOCATION_PREF_NAME = "user_location_pref"

    companion object {
        const val CHANNEL_ID = "FlashLocationService"
        const val NOTIFICATION_ID = 10000
        val currentLocation = MutableLiveData<Location?>()
        val isTracking = MutableLiveData<Boolean>(false)
    }

    inner class LocationBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,5000L)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(2000L)
            .setMaxUpdateDelayMillis(10000L)
            .build()



        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    if(location.accuracy <= 10f) {
                        currentLocation.postValue(location)
                        updateNotification(location)
                    }
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_TRACKING" -> startLocationTracking()
            "STOP_TRACKING" -> stopLocationTracking()
        }
        return START_STICKY
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationTracking() {

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        isTracking.postValue(true)
    }

    private fun stopLocationTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isTracking.postValue(false)
        stopForeground(true)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Push ads Channel",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Push ads Channel"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(location: Location? = null): Notification {
        val notificationLayout = RemoteViews(packageName, R.layout.notification_small)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setCustomContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification(location: Location) {
        val notification = createNotification(location)
        calculateDistanceFromPreviousLocation(location)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun storeLastKnownLocation(location: Location) {
        val gson = Gson()
        val lastLocation = LocationData(location)
        val json = gson.toJson(lastLocation)

        locationSharedPreferences = getSharedPreferences(LOCATION_PREF_NAME, MODE_PRIVATE)

        val editor = locationSharedPreferences?.edit()
        editor?.putString("lastLocation", json)
        editor?.apply()
    }

    private  fun calculateDistanceFromPreviousLocation(newLocation: Location) {
        val gson = Gson()
        var distance = 0f
        locationSharedPreferences = getSharedPreferences(LOCATION_PREF_NAME, MODE_PRIVATE)

        val json = locationSharedPreferences?.getString("lastLocation", null)

        if(json == null) {
            storeLastKnownLocation(newLocation)
            return
        }
        val locationData = gson.fromJson(json, LocationData::class.java)
        val location = locationData.toLocation()

        // getting business profile for user if it exits
        CoroutineScope(Dispatchers.IO).launch {
            if(NetworkUtil.isConnected(applicationContext)) {
                val businessProfile = retrofitInterface.apiService.getBusinessProfile()
                if(businessProfile.isSuccessful) {
                    hasBusinessProfile = true
                    val body = businessProfile.body()
                    if (body!!.location.walkingBillboard.enabled) {
                        isBillBoardLocationEnabled = body.location.walkingBillboard.enabled
                        range = body.location.walkingBillboard.liveLocationInfo?.range?.toInt()
                    } else {
                        isBillBoardLocationEnabled = false
                        range = 0
                    }
                } else {
                    hasBusinessProfile = false
                    isBillBoardLocationEnabled = false
                    range = 0
                }
            } else{
                return@launch
            }
        }

        val combinedAccuracy = newLocation.accuracy + location.accuracy
        Log.d("Permission", "Combined Accuracy $combinedAccuracy")

        if(combinedAccuracy <= 20.0f) {
            distance = location.distanceTo(newLocation)
        } else {
            storeLastKnownLocation(newLocation)
            return
        }

        Log.d("Permission", "Distance $distance")
        Log.d("Permission", "Has business Profile $hasBusinessProfile")
        Log.d("Permission", "Bill Board enable $isBillBoardLocationEnabled")
        Log.d("Permission", "Bill board range: $range")



        if(distance > 20) {
            val latitude = RequestBody.create("text/plain".toMediaTypeOrNull(), newLocation.latitude.toString())
            val longitude = RequestBody.create("text/plain".toMediaTypeOrNull(), newLocation.longitude.toString())
            val accuracy = RequestBody.create("text/plain".toMediaTypeOrNull(), newLocation.accuracy.toString())


            if(hasBusinessProfile && isBillBoardLocationEnabled) {
                    val enabled = RequestBody.create("text/plain".toMediaTypeOrNull(), this@LocationService.isBillBoardLocationEnabled.toString())
                    val range = RequestBody.create("text/plain".toMediaTypeOrNull(), this@LocationService.range.toString())
                    // updating walking billboard live location if user has business profile and enable the walking billboard
                    if (NetworkUtil.isConnected(applicationContext) && LocationServiceUtil.isLocationEnabled(applicationContext)) {
                        CoroutineScope(Dispatchers.IO).launch {
                           retrofitInterface.apiService.updateLiveLocation(
                                enabled,
                                latitude,
                                longitude,
                                accuracy,
                                range
                            )
                        }
                    } else {
                        return
                    }
                }
            // sending user location to server for processing
            CoroutineScope(Dispatchers.IO).launch {
                 retrofitInterface.apiService.processUserLocationAndBusinessLocation(
                    latitude,
                    longitude,
                    accuracy
                )

                 retrofitInterface.apiService.processUserLocationAndWalkingBillboardLocation(
                    latitude,
                    longitude,
                    accuracy
                )

            }

            storeLastKnownLocation(newLocation)
        } else {
            return
        }


    }
}



