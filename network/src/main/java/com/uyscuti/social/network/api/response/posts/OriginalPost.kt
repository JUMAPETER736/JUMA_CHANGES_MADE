package com.uyscuti.social.network.api.response.posts

import java.io.Serializable

data class OriginalPost(
    val __v: Int,
    val _id: String,
    val author: AuthorX,
    val bookmarkCount: Int,
    val bookmarks: List<Bookmark>,
    var commentCount: Int,
    val content: String,
    val contentType: String,
    val createdAt: String,
    val duration: List<Duration>,
    val feedShortsBusinessId: String,
    val fileIds: List<String>,
    val fileNames: List<FileName>,
    val fileSizes: List<FileSizeX>,
    val fileTypes: List<FileType>,
    val files: List<File>,
    val isReposted: Boolean,
    val likeCount: Int,
    val numberOfPages: List<NumberOfPageX>,
    val originalPostId: Any,
    val originalPostReposter: List<Any?>,
    val repostCount: Int,
    val repostedByUserId: Any,
    val repostedUsers: List<Any?>,
    val tags: List<Any?>,
    val thumbnail: List<ThumbnailX>,
    val updatedAt: String,
    val shareCount: Int,

    // Business/Shop related fields
    val isBusinessPost: Boolean? = null,
    val category: String? = null,

    // Favorites related fields
    var isFavorited: Boolean? = null,
    val favorites: List<String>? = null


): Serializable