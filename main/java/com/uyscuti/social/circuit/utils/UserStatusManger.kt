package com.uyscuti.social.circuit.utils

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper

import com.uyscuti.social.network.api.response.userstatus.UserStatusResponse
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance

class UserStatusManager(
    private val retrofitInstance: RetrofitInstance,
    private val userId: String,
    private val onUserStatusUpdate: (UserStatusResponse?) -> Unit
) {

    private val handler = Handler(Looper.getMainLooper())
    private val updateIntervalMillis = 60 * 1000 // 2 minutes

    init {
        fetchUserStatus()
    }

    fun start() {
        scheduleUserStatusUpdate()
    }

    private fun scheduleUserStatusUpdate() {
        handler.postDelayed(
            {
                // Check if the task is already running
                if (userStatusTask?.status != AsyncTask.Status.RUNNING) {
                    // Create a new instance of the task and execute it
                    userStatusTask = GetUserStatusTask(retrofitInstance) { userStatus ->
                        onUserStatusUpdate(userStatus)
                        scheduleUserStatusUpdate()
                    }
                    userStatusTask?.execute(userId)
                }
            },
            updateIntervalMillis.toLong()
        )
    }

    private fun fetchUserStatus() {
        // Create a new instance of the task and execute it
        userStatusTask = GetUserStatusTask(retrofitInstance) { userStatus ->
            onUserStatusUpdate(userStatus)
            scheduleUserStatusUpdate()
        }
        userStatusTask?.execute(userId)
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
        // Cancel the task if it's running
        userStatusTask?.cancel(true)
    }

    // Make userStatusTask nullable
    private var userStatusTask: GetUserStatusTask? = null
}
