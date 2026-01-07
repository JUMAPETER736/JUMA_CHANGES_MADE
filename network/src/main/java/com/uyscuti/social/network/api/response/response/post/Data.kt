package com.uyscuti.social.network.api.response.response.post

import com.uyscuti.social.network.api.response.post.Image
import com.uyscuti.social.network.api.response.post.Thumbnail
import com.uyscuti.social.network.api.response.posts.Author

data class Data(
    val __v: Int,
    val _id: String,
    val author: Author,
    val comments: Int,
    val content: String,
    val createdAt: String,
    val images: List<Image>,
    var isBookmarked: Boolean,
    var isLiked: Boolean,
    var likes: Int,
    val tags: List<String>,
    val thumbnail: List<Thumbnail>,
    val updatedAt: String
)