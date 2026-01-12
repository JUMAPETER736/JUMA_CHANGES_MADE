package com.uyscuti.social.network.api.response.getPostRepostTrial

data class OriginalPost(
    val __v: Int,
    val _id: String,
    val author: AuthorX,
    val content: String,
    val contentType: String,
    val createdAt: String,
    val duration: List<Duration>,
    val feedShortsBusinessId: String,
    val fileIds: List<String>,
    val fileNames: List<FileName>,
    val fileSizes: List<FileSize>,
    val fileTypes: List<FileType>,
    val files: List<File>,
    val isReposted: Boolean,
    val numberOfPages: List<NumberOfPage>,
    val originalPostId: String,
    val repostedByUserId: String,
    val repostedUsers: List<String>,
    val tags: List<Any?>,
    val thumbnail: List<ThumbnailX>,
    val updatedAt: String
)