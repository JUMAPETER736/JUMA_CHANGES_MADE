package com.uyscuti.social.network.api.response.otherusersprofileshorts

import com.uyscuti.social.network.api.response.otherusersprofileshorts.Author
import com.uyscuti.social.network.api.response.otherusersprofileshorts.Image

data class Post(
    val __v: Int,
    val _id: String,
    val author: Author,
    val comments: Int,
    val content: String,
    val createdAt: String,
    val images: List<Image>,
    val isBookmarked: Boolean,
    val isLiked: Boolean,
    val likes: Int,
    val tags: List<String>,
    val updatedAt: String
)