package com.uyscuti.social.network.api.response.getUnifiedNotification

data class GetUnifiedNotifications(
    val `data`: List<FeedNotification>,
    val currentPage: Int,
    val totalPages: Int,
    val hasNextPage: Boolean
)