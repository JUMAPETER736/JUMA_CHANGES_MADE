package com.uyscuti.social.call.representation

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named


class MainViewModel @Inject constructor(
    @Named("chat_notification_compat_builder")

    private val notificationBuilder: NotificationCompat.Builder,
    @Named("chat_notification_manager_compat")

    private val notificationManager: NotificationManagerCompat
) : ViewModel() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showSimpleNotification(target: String) {

        notificationManager.notify(1, notificationBuilder.setContentTitle(target).build())
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun updateSimpleNotification() {
        notificationManager.notify(
            1, notificationBuilder
                .setContentTitle("NEW TITLE")
                .build()
        )
    }

    fun cancelSimpleNotification() {
        notificationManager.cancel(1)
    }

}