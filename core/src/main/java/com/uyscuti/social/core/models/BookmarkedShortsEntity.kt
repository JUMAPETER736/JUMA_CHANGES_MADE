package com.uyscuti.social.core.models

import androidx.room.PrimaryKey
import com.uyscuti.social.network.api.response.getallshorts.Thumbnail
import com.uyscuti.social.network.api.response.post.Image
import com.uyscuti.social.network.api.response.posts.Author

import java.io.Serializable

data class BookmarkedShortsEntity (
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
): Serializable
