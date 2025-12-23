package com.uyscuti.social.circuit.callbacks

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.uyscuti.social.circuit.CallHelper
import com.uyscuti.social.circuit.CallResult
import com.uyscuti.social.circuit.R
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.CoreChatSocketClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException

@HiltWorker
class CombinedWorker @AssistedInject constructor(
    @Assisted val callHelper: CallHelper,
    @Assisted val chatSocketClient: CoreChatSocketClient,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Log.d("CombinedWorker", "Combined Worker Started")

//        return Result.success()
        return try {
            connect()

        } catch (e: IOException) {
            // Network-related exception, retry the work
            Result.retry()
        } catch (e: Exception) {
            // Other exceptions, mark as failure
            Log.d("CombinedWorker", "Combined Worker Failed ${e.message}")
            Result.failure(Data.Builder().putString("error", e.toString()).build())
        }
    }

    private suspend fun connect(): Result {
        return try {
            Log.d("CombinedWorker", "Trying to connect to socket")
            chatSocketClient.connect()

            when (val result = callHelper.initializeCallService()) {
                is CallResult.Success -> {
                    // Additional operations if needed
                    Log.d("CombinedWorker", "Connected to socket")
                    Result.success()
                }
                is CallResult.Failure -> Result.failure(
                    Data.Builder().putString("error", result.errorMessage).build()
                )
                is CallResult.Retry -> Result.retry()
                else -> Result.retry()
            }
        } catch (e: Exception) {
            // Handle other exceptions here
            Log.d("CombinedWorker", "Failed to connect to socket ${e.message}")
            Result.failure(Data.Builder().putString("error", e.toString()).build())
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        Log.d("CombinedWorker", "Getting foreground info")
        try {
            createNotificationChannel(applicationContext)
        } catch (e: Exception){
            e.printStackTrace()
        }
//        createNotificationChannel(applicationContext)
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Flash")
            .setContentText("Connecting...")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create a NotificationChannel for Android Oreo and higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Network Check Channel",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1515
        private const val CHANNEL_ID = "network_check_channel"
    }
}
