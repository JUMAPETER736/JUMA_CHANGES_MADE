package com.uyscuti.social.core.remoteviews

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.widget.RemoteViews
import androidx.core.app.TaskStackBuilder
import com.uyscuti.social.core.R
import com.uyscuti.social.core.broadcastreceiver.BusinessNotificationReceiver
import com.uyscuti.social.core.models.BillboardAdvertisement
import com.uyscuti.social.core.models.Product
import com.uyscuti.social.core.models.UserData
import com.uyscuti.social.core.pushnotifications.AdvertisementNotificationService
import kotlinx.coroutines.*
import java.io.IOException
import java.net.URL
import java.util.Date

class BusinessAdRemoteView(
    val context: Context,
    val notificationId: Int,
    val businessAd: BillboardAdvertisement
) {

    companion object {
        // Action identifiers
        const val ACTION_VIEW_MORE = "com.uyscuti.social.circuit.OPEN_VIEW_MORE"
        const val ACTION_DISMISS = "com.uyscuti.social.circuit.DISMISS_BUSINESS_AD"

        // Extra keys
        const val EXTRA_BUSINESS_ID = "business_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"

        const val EXTRA_BUSINESS_OWNER = "user"
    }

    /**
     * Builds and returns the RemoteViews for the notification
     * This should be called from a background thread or coroutine
     */
    suspend fun build(): RemoteViews = withContext(Dispatchers.IO) {
        val remoteViews = RemoteViews(context.packageName, R.layout.business_ads_nofitication)

        // Load all images first
        val businessIconBitmap = loadBitmapFromUrl(businessAd.image?.url)
        val productBitmaps = loadProductImages()

        // Setup views with pre-loaded bitmaps
        withContext(Dispatchers.Main) {
            setupBusinessHeader(remoteViews, businessIconBitmap)
            setupBusinessDescription(remoteViews)
            setupProductsGrid(remoteViews, productBitmaps)
            setupViewMoreText(remoteViews)
            setupActionButtons(remoteViews)
        }

        remoteViews
    }


    /**
     * Loads product images asynchronously using coroutines
     */
    private suspend fun loadProductImages(): List<Bitmap?> = coroutineScope {
        businessAd.items.take(3).map { product ->
            async(Dispatchers.IO) {
                loadProductBitmap(product)
            }
        }.awaitAll()
    }

    /**
     * Loads product images synchronously
     */
    private fun loadProductImagesSync(): List<Bitmap?> {
        return businessAd.items.take(3).map { product ->
            loadProductBitmap(product)
        }
    }

    /**
     * Loads bitmap for a single product
     */
    private fun loadProductBitmap(product: Product): Bitmap? {
        val imageUrl = product.images.firstOrNull() ?: return null

        return if (isVideoUrl(imageUrl)) {
            loadVideoThumbnail(imageUrl)
        } else {
            loadBitmapFromUrl(imageUrl)
        }
    }

    /**
     * Sets up the business header section (icon, name, location, ad badge)
     */
    private fun setupBusinessHeader(remoteViews: RemoteViews, businessIconBitmap: Bitmap?) {
        remoteViews.setTextViewText(R.id.business_name, businessAd.businessName)
        remoteViews.setTextViewText(R.id.business_location, "Located in ${businessAd.city}")

        businessIconBitmap?.let {
            remoteViews.setImageViewBitmap(R.id.business_icon, it)
        }
    }

    /**
     * Loads bitmap from URL with proper error handling and size optimization
     */
    private fun loadBitmapFromUrl(imageUrl: String?): Bitmap? {
        if (imageUrl.isNullOrEmpty()) return null

        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connectTimeout = 10000 // 10 seconds timeout
            connection.readTimeout = 10000
            connection.doInput = true
            connection.connect()

            val inputStream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Scale down if too large for RemoteViews
            scaleBitmapIfNeeded(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Loads video thumbnail
     */
    private fun loadVideoThumbnail(videoUrl: String): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoUrl, HashMap<String, String>())
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()

            bitmap?.let { scaleBitmapIfNeeded(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Scales bitmap if it's too large (RemoteViews have size limitations)
     */
    private fun scaleBitmapIfNeeded(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null

        val maxWidth = 512
        val maxHeight = 512

        if (bitmap.width <= maxWidth && bitmap.height <= maxHeight) {
            return bitmap
        }

        val ratio = Math.min(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )

        val scaledWidth = (bitmap.width * ratio).toInt()
        val scaledHeight = (bitmap.height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }

    /**
     * Sets up the business description text
     */
    private fun setupBusinessDescription(remoteViews: RemoteViews) {
        remoteViews.setTextViewText(R.id.business_description, businessAd.businessDescription)
    }

    /**
     * Sets up the products grid (3 product items)
     */
    private fun setupProductsGrid(remoteViews: RemoteViews, productBitmaps: List<Bitmap?>) {
        val productViews = listOf(
            ProductViewIds(
                containerId = R.id.product_item_1,
                imageId = R.id.product_image_1,
                nameId = R.id.product_name_1,
                priceId = R.id.product_price_1
            ),
            ProductViewIds(
                containerId = R.id.product_item_2,
                imageId = R.id.product_image_2,
                nameId = R.id.product_name_2,
                priceId = R.id.product_price_2
            ),
            ProductViewIds(
                containerId = R.id.product_item_3,
                imageId = R.id.product_image_3,
                nameId = R.id.product_name_3,
                priceId = R.id.product_price_3
            )
        )

        businessAd.items.take(3).forEachIndexed {index, product ->
            setupProductItem(remoteViews, productViews[index], product, productBitmaps.getOrNull(index))
            }


    }

    /**
     * Sets up a single product item
     */
    private fun setupProductItem(
        remoteViews: RemoteViews,
        viewIds: ProductViewIds,
        product: Product,
        bitmap: Bitmap?
    ) {

        remoteViews.setTextViewText(viewIds.nameId, product.itemName)
        remoteViews.setTextViewText(viewIds.priceId, "MWK ${product.price}")

        bitmap?.let {
            remoteViews.setImageViewBitmap(viewIds.imageId, it)
        }
    }

    private fun isVideoUrl(url: String): Boolean {
        val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".avi", ".mov")
        return videoExtensions.any { url.endsWith(it, ignoreCase = true) }
    }

    /**
     * Sets up the "view more" text
     */
    private fun setupViewMoreText(remoteViews: RemoteViews) {
        val remainingCount = businessAd.items.size - 3

        if (remainingCount > 0) {
            remoteViews.setTextViewText(R.id.view_more_text, "+$remainingCount more")
        } else {
            remoteViews.setTextViewText(R.id.view_more_text, "")
        }
    }

    /**
     * Sets up action buttons (Dismiss and View)
     */
    private fun setupActionButtons(remoteViews: RemoteViews) {
        // Dismiss button
        val dismissIntent = createDismissIntent()
        remoteViews.setOnClickPendingIntent(R.id.btn_dismiss, dismissIntent)

        // View details button
        viewMoreAction(remoteViews)
//        val viewIntent = createViewIntent()
//        remoteViews.setOnClickPendingIntent(R.id.btn_view_details, viewIntent)
    }

    /**
     * Creates PendingIntent for dismissing the notification
     */
    private fun createDismissIntent(): PendingIntent {
        val intent = Intent(context, BusinessNotificationReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_BUSINESS_ID, businessAd.businessId)
        }
        val requestCode = notificationId + 0
        return createPendingIntent(intent, requestCode)
    }

    /**
     * Creates PendingIntent for viewing business details
     */
    private fun createViewIntent(): PendingIntent {
        val intent = Intent(context, BusinessNotificationReceiver::class.java).apply {
            action = ACTION_VIEW_MORE
            putExtra(EXTRA_BUSINESS_ID, businessAd.businessId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_BUSINESS_OWNER, businessAd.owner)
        }

        val requestCode = notificationId + 1
        return createPendingIntent(intent, requestCode)
    }

    /**
     * Helper method to create PendingIntent with proper flags
     */
    private fun createPendingIntent(intent: Intent, requestCode: Int): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun viewMoreAction(remoteViews: RemoteViews) {

        val user = UserData(
            businessAd.owner.userId,
            businessAd.owner.avatar,
            "",
            false,
            "user",
            businessAd.owner.username,
            Date()
        )

        val viewMoreIntent = Intent(AdvertisementNotificationService.Companion.ACTION_VIEW_MORE).apply {
            putExtra(AdvertisementNotificationService.Companion.EXTRA_BUSINESS_OWNER, user)
            putExtra(AdvertisementNotificationService.Companion.EXTRA_NOTIFICATION_ID, notificationId)
            setPackage(context.packageName)
        }

        // Resolve to explicit intent
        val resolveInfo = context.packageManager.resolveActivity(viewMoreIntent, 0)

        if (resolveInfo != null) {
            val explicitIntent = Intent(viewMoreIntent).apply {
                component = ComponentName(
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name
                )
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            // Create PendingIntent using TaskStackBuilder for proper back stack
            val pendingIntent = TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(explicitIntent)
                getPendingIntent(
                    notificationId, // Use unique request code
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            // Set the click listener on your RemoteViews button/view
            remoteViews.setOnClickPendingIntent(R.id.btn_view_details, pendingIntent)
            remoteViews.setOnClickPendingIntent(R.id.main_content_area, pendingIntent)
        }
    }
}

/**
 * Data class to hold product view IDs
 */
private data class ProductViewIds(
    val containerId: Int,
    val imageId: Int,
    val nameId: Int,
    val priceId: Int
)