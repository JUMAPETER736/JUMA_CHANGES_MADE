package com.uyscuti.social.network.api.response.getallshorts

import java.io.Serializable

data class Post(
    val __v: Int,
    val _id: String,
    val author: Author,
    val comments: Int,
    val content: String,
    val createdAt: String,
    val images: List<Image>,
    val thumbnail: List<Thumbnail>,
    var isBookmarked: Boolean,
    var isLiked: Boolean,
    val likes: Int,
    val tags: List<String>,
    val updatedAt: String,
    val feedShortsBusinessId: String = ""
):Serializable