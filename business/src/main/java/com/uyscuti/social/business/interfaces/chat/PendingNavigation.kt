package com.uyscuti.social.business.interfaces.chat

data class PendingNavigation(
    val userId: String,
    val chatType: String,
    val timestamp: Long = System.currentTimeMillis()
)
