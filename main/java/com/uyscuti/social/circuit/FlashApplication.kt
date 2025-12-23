package com.uyscuti.social.circuit

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.ExoDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.uyscuti.social.circuit.callbacks.ChatSocketWorker
import com.uyscuti.social.circuit.callbacks.NetworkConnectivityCallback
import com.uyscuti.social.circuit.callbacks.NetworkConnectivityService
import dagger.hilt.android.HiltAndroidApp
import com.uyscuti.social.circuit.callbacks.SocketJobService
import com.uyscuti.social.circuit.User_Interface.feed.FeedUploadWorker
import com.uyscuti.social.circuit.User_Interface.shorts.ShortsUploadWorker
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.CoreChatSocketClient
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@UnstableApi @HiltAndroidApp
class FlashApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
//    val applicationViewModel: UserProfileShortsViewModel by viewModels()

//
//    @Inject
//    lateinit var flashWorkerFactory: FlashWorkerFactory
//
//    @Inject
//    lateinit var chatSocketWorkerFactory: ChatSocketWorkerFactory

//    override val workManagerConfiguration: Configuration get()  = Configuration.Builder()
//        .setWorkerFactory(shortsUploadWorker)
//        .build()

    class CompositeWorkerFactory @Inject constructor(
//        private val combinedWorkerFactory: CombinedWorkerFactory,
        private val shortsUploadWorker: ShortsWorkerFactory,
        private val feedUploadWorker: FeedWorkerFactory
    ) : WorkerFactory() {

        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            // Depending on the workerClassName, return the corresponding worker
            return when (workerClassName) {
                ShortsUploadWorker::class.java.name ->
                    shortsUploadWorker.createWorker(appContext, workerClassName, workerParameters)
                FeedUploadWorker::class.java.name ->
                    feedUploadWorker.createWorker(appContext, workerClassName, workerParameters)
//                CombinedWorkerFactory::class.java.name -> {
//                    combinedWorkerFactory.createWorker(appContext, workerClassName, workerParameters)
//                }
                else -> null
            }
        }
    }


    override val workManagerConfiguration: Configuration
        get() {
//            val compositeFactory = CompositeWorkerFactory(
//                listOf(
//                    combinedWorkerFactory,
//                    feedUploadWorker,
//                    shortsUploadWorker,
//
//                )
//            )

//            val compositeFactory = CompositeWorkerFactory(combinedWorkerFactory, shortsUploadWorker, feedUploadWorker, )
            val compositeFactory = CompositeWorkerFactory(shortsUploadWorker, feedUploadWorker, )
            return Configuration.Builder()
                .setWorkerFactory(compositeFactory)
                .build()
        }
//    override val workManagerConfiguration: Configuration
//        get() = Configuration.Builder()
//            .setWorkerFactories(listOf(shortsUploadWorker, combinedWorkerFactory))
//            .build()

//    @Inject
//    lateinit var combinedWorkerFactory: CombinedWorkerFactory

    @Inject
    lateinit var shortsUploadWorker: ShortsWorkerFactory

    @Inject
    lateinit var feedUploadWorker: FeedWorkerFactory

    companion object {
//        lateinit var simpleCache: SimpleCache
        lateinit var cache: SimpleCache
        const val exoPlayerCacheSize: Long = 1024 * 1024 * 1024
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        lateinit var exoDatabaseProvider: ExoDatabaseProvider

        private lateinit var instance: FlashApplication

        // Method to get the instance
        fun getInstance(): FlashApplication {
            return instance
        }
        private lateinit var bInstance: FlashApplication

        fun getAppContext(): Context {
            return bInstance.applicationContext
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        bInstance = this
    }
    @OptIn(UnstableApi::class) override fun onCreate() {
        super.onCreate()
        // Check if WorkManager is already initialized
//        if (!isWorkManagerInitialized()) {
//            initializeWorkManager()
//        }
        instance = this
        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)
        exoDatabaseProvider = ExoDatabaseProvider(this)
        cache = SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor, exoDatabaseProvider)
        registerNetworkCallback(this)
        startSocketJob()
//
//        if (isWorkManagerInitialized()){
//            registerNetworkCallback(this)
//        }

//        Handler().postDelayed({
//            registerNetworkCallback(this)
//        }, 5000)
    }

    private fun startSocketJob(){
        // Set the job interval to 1 hour
        val jobIntervalMillis = TimeUnit.MINUTES.toMillis(30)
        val jobIntervalFlexMillis = TimeUnit.MINUTES.toMillis(15)

        // Create a JobInfo object with your JobService component
        val componentName = ComponentName(this, SocketJobService::class.java)
        val jobInfo = JobInfo.Builder(58, componentName)
            .setPeriodic(jobIntervalMillis,jobIntervalFlexMillis)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
//            .setPriority(JobInfo.PRIORITY_HIGH)
            .build()

        // Schedule the job
        val jobScheduler = getSystemService(JobScheduler::class.java)
        jobScheduler.schedule(jobInfo)
    }

    private fun isWorkManagerInitialized(): Boolean {
        return try {
            // Try to get the WorkManager instance
            WorkManager.getInstance(this)
            true // If successful, WorkManager is already initialized
        } catch (e: IllegalStateException) {
            false // WorkManager is not initialized
        }
    }

    private fun initializeWorkManager() {
        val configuration = Configuration.Builder()
            .setMinimumLoggingLevel(Log.VERBOSE)
//            .setWorkerFactory(chatSocketWorkerFactory)
//            .setWorkerFactory(flashWorkerFactory)
//            .setWorkerFactory(workerFactory)
//            .setWorkerFactory(combinedWorkerFactory)
            .build()

        WorkManager.initialize(this, configuration)
    }

    private fun registerNetworkCallback(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = NetworkConnectivityCallback(context) // Initialize NetworkConnectivityCallback.kt here
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Start the Foreground Service
            val serviceIntent = Intent(context, NetworkConnectivityService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        } else {
            // Handle the case where the device is running Android version lower than 10
            Log.d("Application", "Foreground service not supported on devices running Android version below 10.")
            // You may want to provide an alternative implementation or notify the user accordingly.
        }

    }

}




//class CompositeWorkerFactory @Inject constructor(
//    private val factories: List<WorkerFactory>
//) : WorkerFactory() {
//
//    override fun createWorker(
//        appContext: Context,
//        workerClassName: String,
//        workerParameters: WorkerParameters
//    ): ListenableWorker? {
//        var createdWorker: ListenableWorker? = null
//
//        for (factory in factories) {
//            val worker = factory.createWorker(appContext, workerClassName, workerParameters)
//            if (worker != null) {
//                Log.d("CompositeWorkerFactory", "Worker created by ${factory.javaClass.simpleName}")
//                createdWorker = worker
//            }
//        }
//
//        return createdWorker
//    }
//}


//@FlashWorkerScope
class FlashWorkerFactory @Inject constructor(private val callHelper: CallHelper) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = FlashWorker(callHelper,appContext,workerParameters)
}

//@ChatSocketWorkerScope
class ChatSocketWorkerFactory @Inject constructor(private val chatSocketClient: CoreChatSocketClient) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = ChatSocketWorker(chatSocketClient,appContext,workerParameters)
}

//class CombinedWorkerFactory @Inject constructor(
//    private val callHelper: CallHelper,
//    private val chatSocketClient: CoreChatSocketClient
//) : WorkerFactory() {
//    override fun createWorker(
//        appContext: Context,
//        workerClassName: String,
//        workerParameters: WorkerParameters
//    ): ListenableWorker {
//        return CombinedWorker(callHelper, chatSocketClient, appContext, workerParameters)
//    }
//}
class ShortsWorkerFactory @Inject constructor(
    private val retrofitInstance: RetrofitInstance
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return ShortsUploadWorker(appContext, workerParameters, retrofitInstance)
    }
}


class FeedWorkerFactory @Inject constructor(
    private val retrofitInstance: RetrofitInstance
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return FeedUploadWorker(appContext, workerParameters, retrofitInstance)
    }
}


