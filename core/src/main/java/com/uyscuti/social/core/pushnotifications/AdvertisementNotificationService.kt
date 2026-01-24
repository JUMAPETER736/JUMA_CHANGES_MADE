package com.uyscuti.social.core.pushnotifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.uyscuti.social.core.R
import com.uyscuti.social.core.broadcastreceiver.BusinessNotificationReceiver
import com.uyscuti.social.core.models.AdsNotification
import com.uyscuti.social.core.models.BillboardAdvertisement
import com.uyscuti.social.core.models.UserData
import com.uyscuti.social.core.remoteviews.BusinessAdRemoteView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL
import java.util.Date

class AdvertisementNotificationService: Service() {

    private val CHANNEL_ID = "FlashAdvertisementNotifications"

    val foregroundNotificationId = 9090

    private  lateinit var notificationManager: NotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var isForegroundStarted = false

    companion object{
        const val BUSINESS_LOCATION_ADVERTISEMENT = "Business_location"
        const val BILLBOARD_LOCATION_ADVERTISEMENT = "Billboard_location"


        // Intent extras
        const val EXTRA_BUSINESS_ID = "business_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"

        const val EXTRA_BUSINESS_OWNER = "user"


        // Intent actions
        const val ACTION_VIEW_MORE = "com.uyscuti.social.circuit.OPEN_VIEW_MORE"
        const val ACTION_DISMISS = "com.uyscuti.social.circuit.DISMISS_BUSINESS_AD"
    }



    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // CRITICAL: Call startForeground() IMMEDIATELY, before any other logic
        if (!isForegroundStarted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // If no permission, stop immediately
                stopSelf()
                return START_NOT_STICKY
            }

            // Start foreground IMMEDIATELY - must be called within 5-10 seconds of startForegroundService()
            val notification = createForegroundNotification()
            startForeground(foregroundNotificationId, notification)
            isForegroundStarted = true

            // Schedule stopping the foreground state (but keep service running)
            handler.postDelayed({
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                // Don't call stopSelf() here - let the service continue for notifications
            }, 3000) // Give it 3 seconds instead of 1
        }

        // Process the intent to show notifications
        intent?.let { cm ->
            when (cm.action) {
                BUSINESS_LOCATION_ADVERTISEMENT -> {
                    val adsNotification = cm.getSerializableExtra("adsNotification") as? AdsNotification
                    adsNotification?.let { showBusinessNotification(it) }
                }
                BILLBOARD_LOCATION_ADVERTISEMENT -> {
                    val adsBillboard = cm.getSerializableExtra("adsBillboard") as? BillboardAdvertisement
                    adsBillboard?.let { showBillboardNotification(applicationContext, it) }
                }
                else -> {}
            }
        }

        return START_STICKY
    }
    
    @SuppressLint("ObsoleteSdkInt")
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Advertisement Notifications Channel"
            val description = "Push Notifications for ADS"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            notificationManager.createNotificationChannel(channel)

        }
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Circuit")
            .setContentText("Business Ads")
            .setSmallIcon(R.drawable.ic_notification) // Use a very small, subtle icon
            .setPriority(NotificationCompat.PRIORITY_MIN) // Even lower than LOW
            .setOngoing(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setDefaults(0)
            .setSilent(true)
            .build()
    }

    private fun startForegroundService() {
        val notification = createForegroundNotification()
        startForeground(foregroundNotificationId, notification)
    }

    private fun createStableNotificationId(vararg components: String): Int {
        // Combine components into a single string and use its hashCode
        val combined = components.joinToString(separator = "|")
        return combined.hashCode().let { hash ->
            // Handle negative hashCodes and ensure it doesn't conflict with foreground ID
            val adjusted = if (hash < 0) hash + Int.MAX_VALUE else hash
            if (adjusted == foregroundNotificationId) adjusted + 1 else adjusted
        }
    }

    /**
     * Creates and displays a business advertisement notification
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showBusinessNotification(notificationData: AdsNotification) {
        val notificationId = createStableNotificationId(
            notificationData.businessProfileId,
            notificationData.owner.userId,
            BUSINESS_LOCATION_ADVERTISEMENT
        )
        // Create the custom RemoteViews
        val remoteViews = createCustomRemoteViews(notificationData, notificationId)

        // Build the notification
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Nearby Business")
            .setContentText("Business found nearby")
            .setCustomBigContentView(remoteViews)
            .setCustomHeadsUpContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        // Show the notification
        // notificationManager.notify(notificationId, notification)
        NotificationManagerCompat.from(this).notify(notificationId, notification)

        // Load business image asynchronously if available
        notificationData.imageUrl?.let { imageUrl ->
            loadBusinessImageAsync(imageUrl, notificationData, notificationId)
        }
    }
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showBillboardNotification(context: Context, billboardAd: BillboardAdvertisement){

        serviceScope.launch {

            val notificationId = createStableNotificationId(
                billboardAd.businessId,
                billboardAd.owner.userId,
                BILLBOARD_LOCATION_ADVERTISEMENT
            )

            val remoteView = BusinessAdRemoteView(context,notificationId , billboardAd)
            val remoteViews = remoteView.build()

            // Build the notification
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Advertisement")
                .setContentText("Business Advertisement")
                .setCustomBigContentView(remoteViews)
                .setCustomHeadsUpContentView(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build()

            // Show the notification
            // notificationManager.notify(notificationId, notification)
            NotificationManagerCompat.from(this@AdvertisementNotificationService).notify(notificationId, notification)

        }
    }

    /**
     * Creates the custom RemoteViews layout
     */
    private fun createCustomRemoteViews(
        notificationData: AdsNotification,
        notificationId: Int
    ): RemoteViews {
        val remoteViews = RemoteViews(packageName, R.layout.business_notification_layout)

        // Set basic text content
        remoteViews.setTextViewText(R.id.tv_business_name, notificationData.businessName)
        remoteViews.setTextViewText(R.id.tv_business_description, notificationData.description)
        remoteViews.setTextViewText(R.id.tv_distance,"${notificationData.distance} meters away")

        // Set default business image (placeholder)
        remoteViews.setImageViewResource(R.id.iv_business_image, R.drawable.sample_shop_image)

        // Set up items list
        setupItemsList(remoteViews, notificationData.items)

        // Set up click actions
        setupClickActions(remoteViews, notificationData, notificationId)

        return remoteViews
    }

    /**
     * Sets up the items list in the notification
     */
    private fun setupItemsList(remoteViews: RemoteViews, items: List<String>) {
        remoteViews.setTextViewText(R.id.tv_items_text,"")
        if (items.isNotEmpty()) {
            // Show up to 3 items in the notification
            val displayItems = items.take(10)
            val itemsText = displayItems.joinToString(" ") { " •$it" }

            remoteViews.setTextViewText(R.id.tv_items_text, itemsText)
            remoteViews.setViewVisibility(R.id.ll_items_fallback, android.view.View.GONE)
        } else {
            // Hide items section if no items
            remoteViews.setViewVisibility(R.id.ll_items_section, android.view.View.GONE)
        }
    }

    /**
     * Loads business image asynchronously and updates notification
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun loadBusinessImageAsync(
        imageUrl: String,
        businessData: AdsNotification,
        notificationId: Int
    ) {
        Thread {
            try {
                val bitmap = loadBitmapFromUrl(imageUrl)
                bitmap?.let {
                    updateNotificationWithImage(it, businessData, notificationId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Keep using placeholder image on error
            }
        }.start()
    }

    /**
     * Loads bitmap from URL
     */
    private fun loadBitmapFromUrl(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.doInput = true
            connection.connect()
            val inputStream = connection.getInputStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            null
        }
    }

    /**
     * Updates existing notification with loaded image
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun updateNotificationWithImage(
        bitmap: Bitmap,
        businessData: AdsNotification,
        notificationId: Int
    ) {
        val remoteViews = createCustomRemoteViews(businessData, notificationId)

        // Set the loaded business image
        remoteViews.setImageViewBitmap(R.id.iv_business_image, bitmap)

        // Build the notification
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Nearby Business")
            .setContentText("Business found nearby")
            .setCustomBigContentView(remoteViews)
            .setCustomHeadsUpContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        // Show the notification
        //   notificationManager.notify(notificationId, notification)
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    /**
     * Sets up click actions for buttons
     */
    private fun setupClickActions(
        remoteViews: RemoteViews,
        businessData: AdsNotification,
        notificationId: Int
    ) {

        // view more button and container click
       viewMoreAction(
           remoteViews,
           businessData,
           notificationId
       )

        // Dismiss button click
        dismissAction(
            remoteViews,
            businessData,
            notificationId
        )

    }

    private fun viewMoreAction(
        remoteViews: RemoteViews,
        businessData: AdsNotification,
        notificationId: Int
    ) {

        val user = UserData(
            businessData.owner.userId,
            businessData.owner.avatar,
            "",
            false,
            "user",
            businessData.owner.username,
            Date()
        )

        val viewMoreIntent = Intent(ACTION_VIEW_MORE).apply {
            putExtra(EXTRA_BUSINESS_OWNER, user)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            setPackage(packageName)
        }

        // Resolve to explicit intent
        val resolveInfo = packageManager.resolveActivity(viewMoreIntent, 0)

        if (resolveInfo != null) {
            val explicitIntent = Intent(viewMoreIntent).apply {
                component = ComponentName(
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name
                )
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            // Create PendingIntent using TaskStackBuilder for proper back stack
            val pendingIntent = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(explicitIntent)
                getPendingIntent(
                    notificationId, // Use unique request code
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            // Set the click listener on your RemoteViews button/view
            remoteViews.setOnClickPendingIntent(R.id.btn_view_more, pendingIntent)
            remoteViews.setOnClickPendingIntent(R.id.main_content_area, pendingIntent)
        }
    }

    private fun dismissAction(
        remoteViews: RemoteViews,
        businessData: AdsNotification,
        notificationId: Int
    ) {

        val dismissIntent = Intent(applicationContext, BusinessNotificationReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_BUSINESS_ID, businessData.businessProfileId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationId * 10 + 2,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        remoteViews.setOnClickPendingIntent(R.id.btn_dismiss, dismissPendingIntent)
    }


    override fun onBind(intent: Intent?): IBinder? = null
}