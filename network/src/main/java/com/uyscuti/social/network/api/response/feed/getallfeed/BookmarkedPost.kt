package com.uyscuti.social.network.api.response.feed.getallfeed


data class BookmarkedPost(
    val __v: Int,
    val _id: String,
    val author: Author,
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