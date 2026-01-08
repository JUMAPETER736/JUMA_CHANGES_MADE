package com.uyscuti.social.network.api.request.shorts

import android.net.Uri

data class ShortsRequest (
    val content: String,
    val tags: MutableList<String> = mutableListOf<String>()
)