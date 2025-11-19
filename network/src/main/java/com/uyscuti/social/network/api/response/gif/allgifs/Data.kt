package com.uyscuti.social.network.api.response.gif.allgifs

data class Data(
    val gifs: MutableList<GifModel>,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val limit: Int,
    val nextPage: Int,
    val page: Int,
    val prevPage: Any,
    val totalGif: Int,
)
