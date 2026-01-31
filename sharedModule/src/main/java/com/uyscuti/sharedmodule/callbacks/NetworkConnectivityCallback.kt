package com.uyscuti.sharedmodule.callbacks

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.uyscuti.sharedmodule.FlashWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

class NetworkConnectivityCallback(

    private val context: Context) :
    ConnectivityManager.NetworkCallback() {

    private val WORKER_TAG = "flash_socket_worker"
    private val callbackScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        Log.d(TAG, "Internet available!")
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        Log.d(TAG, "Internet lost.")
    }

    private fun startMobileDataWorker(){
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED).build()

        val request = PeriodicWorkRequestBuilder<MobileDataWork>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "mobile_data_worker", ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private fun startMobileDataWorkerOne(){
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED).build()

        val request = OneTimeWorkRequestBuilder<MobileDataWork>().build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "unique_mobile_data",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun startCallWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<FlashWorker>()
            .setInitialDelay(15, TimeUnit.SECONDS)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    private fun startChatSocketWorker() {
        val workManager = WorkManager.getInstance(context)

        val existingWorkPolicy = if (workManager.getWorkInfosByTag(WORKER_TAG).get().isEmpty()) {
            ExistingWorkPolicy.REPLACE
        } else {
            Log.d(TAG, "Work already exists, not enqueueing a new one")
            return
        }

        val request = OneTimeWorkRequestBuilder<ChatSocketWorker>()
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .addTag(WORKER_TAG)
            .build()

        try {
            workManager.enqueueUniqueWork(WORKER_TAG, existingWorkPolicy, request)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error enqueuing work: ${e.message}")
        }

        observeWorkStatus(request.id)
    }

    @SuppressLint("RestrictedApi")
    private fun observeWorkStatus(workRequestId: UUID) {
        val workManager = WorkManager.getInstance(context)

        callbackScope.launch {
            workManager.getWorkInfoByIdFlow(workRequestId).collect { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        Log.d(TAG, "Work finished successfully")
                    }
                    WorkInfo.State.FAILED -> {
                        val failureReason = workInfo.outputData.getString("FAILURE_REASON")
                        Log.d(TAG, "Work failed with reason: $failureReason")
                    }
                    WorkInfo.State.CANCELLED -> {
                        Log.d(TAG, "Work was cancelled")
                    }
                    WorkInfo.State.BLOCKED -> {
                        Log.d(TAG, "Work is blocked")
                    }
                    WorkInfo.State.ENQUEUED -> {
                        Log.d(TAG, "Work is enqueued")
                    }
                    WorkInfo.State.RUNNING -> {
                        Log.d(TAG, "Work is running")
                    }
                    null -> {
                        Log.d(TAG, "Work Info is null")
                    }
                }
            }
        }
    }

    private fun startCombinedWorker() {
        val workManager = WorkManager.getInstance(context)

        val existingWorkPolicy = if (workManager.getWorkInfosByTag(WORKER_TAG).get().isEmpty()) {
            ExistingWorkPolicy.REPLACE
        } else {
            Log.d(TAG, "Combined Work already exists, Replacing.......")
            ExistingWorkPolicy.REPLACE
        }

        val request = OneTimeWorkRequestBuilder<CombinedWorker>()
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .addTag(WORKER_TAG)
            .build()

        try {
            workManager.enqueueUniqueWork(WORKER_TAG, existingWorkPolicy, request)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error enqueuing work: ${e.message}")
        }

        observeWorkStatus(request.id)
    }

    private fun startCombinedWorkerPeriodic() {
        Log.d(TAG, "Creating Work Instance")
        try {
            val workManager = WorkManager.getInstance(context)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<CombinedWorker>(
                repeatInterval = 30,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
                flexTimeInterval = 20,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .setInitialDelay(20, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build()

            try {
                workManager.enqueueUniquePeriodicWork(
                    "Flash_Master_Periodic",
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                    request
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Error enqueuing work: ${e.message}")
            }

            observeWorkStatus(request.id)
        } catch (e: Exception) {
            Log.d(TAG, "Error creating Work Instance: ${e.message}")
        }
    }

    // Clean up coroutines when callback is no longer needed
    fun cleanup() {
        callbackScope.cancel()
    }

    companion object {
        private const val TAG = "NetworkConnectivityCallback"
    }
}