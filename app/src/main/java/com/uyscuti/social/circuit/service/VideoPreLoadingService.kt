package com.uyscuti.social.circuit.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.SimpleCache
import kotlinx.coroutines.*
import com.uyscuti.social.circuit.FlashApplication
import com.uyscuti.social.circuit.utils.Constants
import androidx.core.net.toUri
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@UnstableApi
class VideoPreLoadingService : IntentService(VideoPreLoadingService::class.java.simpleName) {

    private val TAG = "VideoPreLoadingService"

    private lateinit var mContext: Context
    private var videosList: ArrayList<String>? = null

    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private val cache: SimpleCache = FlashApplication.cache

    // Aggressive parallel processing
    private val maxConcurrentDownloads = 5 // Balanced for network stability
    private val customDispatcher = Executors.newFixedThreadPool(maxConcurrentDownloads).asCoroutineDispatcher()
    private val scope = CoroutineScope(customDispatcher + SupervisorJob())

    // Track progress
    private val completedCount = AtomicInteger(0)
    private val failedCount = AtomicInteger(0)

    // Cache only first portion of video for instant playback
    private val partialCacheSizeBytes = 3 * 1024 * 1024L // 3MB  for 10-15 seconds

    @Deprecated("Deprecated in Java")
    @OptIn(UnstableApi::class)
    override fun onHandleIntent(intent: Intent?) {
        mContext = applicationContext

        // Ultra-optimized HTTP settings
        httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(5000) //  faster timeout
            .setReadTimeoutMs(5000)
            .setKeepPostFor302Redirects(true)
            .setUserAgent("ExoPlayer/2.0 (Linux;Android)")

        defaultDataSourceFactory = DefaultDataSourceFactory(
            this, httpDataSourceFactory
        )

        if (intent != null) {
            val extras = intent.extras
            videosList = extras?.getStringArrayList(Constants.VIDEO_LIST)

            if (!videosList.isNullOrEmpty()) {
                val totalVideos = videosList!!.size
                Log.d(TAG, "Starting cache for $totalVideos videos")

                // Prioritize first 10 videos for immediate playback
                val priorityVideos = videosList!!.take(10)
                val remainingVideos = videosList!!.drop(10)

                // Cache priority videos first
                preCacheVideosParallel(priorityVideos, isHighPriority = true)

                // Then cache remaining in background
                if (remainingVideos.isNotEmpty()) {
                    preCacheVideosParallel(remainingVideos, isHighPriority = false)
                }

                Log.d(TAG, "Cache complete: ${completedCount.get()} | ✗ ${failedCount.get()}")
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun preCacheVideosParallel(videosList: List<String>, isHighPriority: Boolean) {
        runBlocking(customDispatcher) {
            videosList.map { videoUrl ->
                async {
                    cacheVideoAsync(videoUrl, isHighPriority)
                }
            }.awaitAll()
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun cacheVideoAsync(videoUrl: String, isHighPriority: Boolean) {
        if (videoUrl.isBlank()) return

        // Skip if already cached
        val videoUri = videoUrl.toUri()
        if (isVideoCached(videoUri)) {
            Log.d(TAG, "Already cached: $videoUrl")
            completedCount.incrementAndGet()
            return
        }

        val cacheDataSource = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            .createDataSource()

        // Build data spec with partial caching for high priority
        val dataSpec = if (isHighPriority) {
            // Only cache first 3MB for instant playback
            DataSpec.Builder()
                .setUri(videoUri)
                .setLength(partialCacheSizeBytes)
                .setFlags(DataSpec.FLAG_ALLOW_GZIP)
                .build()
        } else {
            // Full cache for lower priority
            DataSpec.Builder()
                .setUri(videoUri)
                .setFlags(DataSpec.FLAG_ALLOW_GZIP)
                .build()
        }

        // Minimal logging for speed
        val progressListener = CacheWriter.ProgressListener { requestLength, bytesCached, _ ->
            // Only log completion
            if (bytesCached >= requestLength) {
                val cacheType = if (isHighPriority) "PARTIAL" else "FULL"
                Log.d(TAG, "$cacheType cached: $videoUrl")
            }
        }

        withContext(Dispatchers.IO) {
            cacheVideo(dataSpec, progressListener, cacheDataSource, videoUrl)
        }
    }

    @OptIn(UnstableApi::class)
    private fun cacheVideo(
        dataSpec: DataSpec,
        progressListener: CacheWriter.ProgressListener,
        cacheDataSource: CacheDataSource,
        videoUrl: String
    ) {
        runCatching {
            CacheWriter(
                cacheDataSource,
                dataSpec,
                null,
                progressListener
            ).cache()
            completedCount.incrementAndGet()
        }.onFailure { error ->
            failedCount.incrementAndGet()
            Log.e(TAG, "Failed: $videoUrl - ${error.message}")
        }
    }

    @OptIn(UnstableApi::class)
    private fun isVideoCached(uri: Uri): Boolean {
        return try {
            val cachedBytes = cache.getCachedBytes(uri.toString(), 0, partialCacheSizeBytes)
            cachedBytes >= partialCacheSizeBytes * 0.8 // 80% cached is good enough
        } catch (e: Exception) {
            false
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        customDispatcher.close()
        Log.d(TAG, "Service destroyed. Final stats:  ${completedCount.get()} | ✗ ${failedCount.get()}")
    }
}