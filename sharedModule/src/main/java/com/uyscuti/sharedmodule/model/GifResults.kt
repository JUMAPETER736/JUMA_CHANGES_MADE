package com.uyscuti.sharedmodule.model

import com.uyscuti.social.network.api.response.gif.allgifs.GifModel

data class GifResults(
    val gifs: MutableList<GifModel>,
    val hasNextPage: Boolean,
    val pageNumber: Int
)