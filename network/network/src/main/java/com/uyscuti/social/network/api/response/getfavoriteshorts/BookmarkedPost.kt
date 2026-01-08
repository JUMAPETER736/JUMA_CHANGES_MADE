package com.uyscuti.social.network.api.response.getfavoriteshorts

import com.uyscuti.social.network.api.response.getallshorts.Author
import com.uyscuti.social.network.api.response.getallshorts.Image
import com.uyscuti.social.network.api.response.getallshorts.Thumbnail

data class BookmarkedPost(
    val __v: Int,
    val _id: String,
    val author: Author,
    val comments: Int,
    val content: String,
    val createdAt: String,
    val images: List<Image>,
    val thumbnail: List<Thumbnail>,
    val isBookmarked: Boolean,
    val isLiked: Boolean,
    val likes: Int,
    val tags: List<String>,
    val updatedAt: String
)