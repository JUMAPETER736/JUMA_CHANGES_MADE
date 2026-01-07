package com.uyscuti.social.core.models

import androidx.room.PrimaryKey

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
