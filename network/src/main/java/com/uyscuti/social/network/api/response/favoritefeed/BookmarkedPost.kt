package com.uyscuti.social.network.api.response.favoritefeed

import com.uyscuti.social.network.api.response.feed.getallfeed.FeedThumbnail
import com.uyscuti.social.network.api.response.feed.getallfeed.File

data class BookmarkedPost(
    val __v: Int,
    val _id: String,
    val author: com.uyscuti.social.network.api.response.feed.getallfeed.Author,
    var comments: Int,
    val content: String,
    val contentType: String,
    val createdAt: String,
    val duration: String?,
    val files: List<File>,
    var isBookmarked: Boolean,
    var isLiked: Boolean,
    var likes: Int,
    val tags: List<String>,
    val thumbnail: List<FeedThumbnail>,
    val docType: String?,
    val fileName: String?,
    val numberOfPages: String?,
    val updatedAt: String,
    var isExpanded: Boolean = false
)