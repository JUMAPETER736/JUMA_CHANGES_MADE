package com.uyscuti.social.circuit.callbacks

// SocketJobService.kt

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import com.uyscuti.social.call.socket.CallSocketClient
import com.uyscuti.social.circuit.callbacks.CombinedWorker
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
//import java.net.Socket
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@SuppressLint("SpecifyJobSchedulerIdRange")
@AndroidEntryPoint
class SocketJobService : JobService() {
    private val TAG = "SocketJobService"

    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var callSocketClient: CallSocketClient

    private val WORKER_TAG = "flash_socket_worker"




    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStartJob() called with: params = [$params]")

        Log.d(TAG, "App is in the background. Starting background tasks.")

        Thread {
            try {
//                startCombinedWorker()
                Log.d(TAG, "Job Completed Successfully")
            } catch (e: Exception) {
                // Handle connection failure
                Log.d(TAG, "Connection failed: $e")
            } finally {
                jobFinished(params, true) // Indicate that the job is complete
            }
        }.start()

        return true // The job has been completed synchronously
    }

    private fun isAppInBackground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = activityManager.runningAppProcesses

        for (processInfo in processes) {
            if (processInfo.processName == packageName) {
                return processInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }
        }

        return false
    }


    private fun startCombinedWorker() {
        val workManager = WorkManager.getInstance(applicationContext)

        val existingWorkPolicy = if (workManager.getWorkInfosByTag(WORKER_TAG).get().isEmpty()) {
            ExistingWorkPolicy.REPLACE
        } else {
            Log.d(TAG, "Combined Work already exists, Replacing.......")

            ExistingWorkPolicy.REPLACE
//            return
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

    private fun handleMessage(message: String) {
        // Handle the received message
        // You can perform any specific action or notify other components
        Log.d(TAG, "Received message: $message")
    }



    @SuppressLint("RestrictedApi")
    private fun observeWorkStatus(workRequestId: UUID) {
        val workManager = WorkManager.getInstance(applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            val workInfo = withContext(Dispatchers.IO) {
                workManager.getWorkInfoById(workRequestId).await()
            }

            when (workInfo?.state) {
                WorkInfo.State.SUCCEEDED -> {
                    Log.d(TAG, "Work finished successfully")
                    // Handle success if needed
                }

                WorkInfo.State.FAILED -> {
                    val failureReason = workInfo.outputData.getString("FAILURE_REASON")
                    Log.d(TAG, "Work failed with reason: $failureReason")
                    // Handle failure or notify the user
                }

                WorkInfo.State.CANCELLED -> {
                    Log.d(TAG, "Work was cancelled")
                    // Handle cancellation if needed
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

                else -> {
                    try {
                        Log.d(TAG, "Work Info State Changed")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    override fun onStopJob(params: JobParameters?): Boolean {
        return true // Reschedule the job if it was stopped
    }

    companion object {
        private const val JOB_ID = 123 // Replace with your desired job ID
    }

}
