package com.uyscuti.social.circuit.callbacks

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class MobileDataWork(val context: Context, params: WorkerParameters) : Worker(context, params) {
//    val context: Context = context
    override fun doWork(): Result {
        // Your logic to check mobile data state or perform desired tasks (e.g., logging, sending notifications)
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo

        // Your logic to handle mobile data state
    return if (activeNetwork?.type == ConnectivityManager.TYPE_MOBILE) {
        Log.d(TAG, "Mobile data enabled!")
        Result.success()
    } else {
        Log.d(TAG, "Mobile data disabled or not available.")
        // Adjust based on your requirements
        Result.failure()
    }
    }

    companion object {
        private const val TAG = "MobileDataWork"
    }
}
