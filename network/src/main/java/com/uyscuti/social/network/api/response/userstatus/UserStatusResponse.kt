package com.uyscuti.social.network.api.response.userstatus

import com.google.gson.annotations.SerializedName
import java.util.Date

data class UserStatusResponse(
    @SerializedName("isOnline")
    val isOnline: Boolean,

    @SerializedName("lastSeen")
    val lastSeen: Date
)
