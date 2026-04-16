package com.uyscuti.social.notifications.feed

import com.uyscuti.social.network.api.response.getUnifiedNotification.GetUnifiedNotifications
import com.uyscuti.social.network.api.response.notification.ReadNotificationResponse
import retrofit2.Response

interface FeedNotificationRepo {
    suspend fun getAllUserNotifications(page: Int,limit: Int): Response<GetUnifiedNotifications>
    suspend fun markAsRead(notificationId: String): Response<ReadNotificationResponse>
    suspend fun markAsUnread(notificationId: String): Response<ReadNotificationResponse>
    suspend fun deleteNotification(notificationId: String): Response<ReadNotificationResponse>
}