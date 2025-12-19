package com.uyscuti.social.network.api.response.userstatus

import java.util.Date

data class UserStatusResponse(
    val isOnline: Boolean,
    val lastSeen: Date
)
