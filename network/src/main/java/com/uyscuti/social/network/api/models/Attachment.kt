package com.uyscuti.social.network.api.models

import java.io.Serializable

data class Attachment(
    val url: String,
    val localPath: String,
    val _id: String
): Serializable