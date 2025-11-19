package com.uyscuti.social.network.api.models.business

import java.io.Serializable

data class Avatar(
    val _id: String,
    val localPath: String,
    val url: String
): Serializable