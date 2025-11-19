package com.uyscuti.social.network.api.models

import com.uyscuti.social.network.api.models.AvatarX
import java.io.Serializable

data class Sender(
    val _id: String,
    val avatar: AvatarX,
    val email: String,
    val username: String
): Serializable