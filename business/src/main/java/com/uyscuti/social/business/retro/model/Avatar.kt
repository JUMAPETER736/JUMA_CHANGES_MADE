package com.uyscuti.social.business.retro.model

import java.io.Serializable

data class Avatar(
    val _id: String,
    val localPath: String,
    val url: String
): Serializable