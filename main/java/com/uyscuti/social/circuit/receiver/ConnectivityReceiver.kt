package com.uyscuti.social.circuit.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.uyscuti.social.circuit.FlashWorker
import java.util.concurrent.TimeUnit

class ConnectivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("CallSocketClient", "onReceive Network State")
        if (context != null) {
            val isConnected = isNetworkConnected(context)
            if (isConnected) {
                // Enqueue your worker when connected to the internet
                startCallWorker(context)
            }
        }
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun startCallWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<FlashWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS) // Customize backoff criteria
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

}
