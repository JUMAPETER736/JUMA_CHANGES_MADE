package com.uyscuti.social.notifications.feed

import com.uyscuti.social.network.api.response.getUnifiedNotification.GetUnifiedNotifications
import com.uyscuti.social.network.api.response.notification.ReadNotificationResponse
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import retrofit2.Response

class FeedNotificationImplementation(
    private val retrofitInstance: RetrofitInstance
): FeedNotificationRepo {

    override suspend fun getAllUserNotifications(page: Int,limit: Int): Response<GetUnifiedNotifications> {
        return retrofitInstance.apiService.getMyUnifiedNotifications(page,limit)
    }

    override suspend fun markAsRead(notificationId: String): Response<ReadNotificationResponse> {
        return retrofitInstance.apiService.markNotificationRead(notificationId)
    }

    override suspend fun markAsUnread(notificationId: String): Response<ReadNotificationResponse> {
        return retrofitInstance.apiService.markNotificationUnread(notificationId)
    }

    override suspend fun deleteNotification(notificationId: String): Response<ReadNotificationResponse> {
        return retrofitInstance.apiService.deleteNotification(notificationId)
    }
}