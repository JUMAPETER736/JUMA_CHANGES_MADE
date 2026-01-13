package com.uyscuti.social.network.api.response.backApiResponse

data class OriginalPost(
    val __v: Int,
    val _id: String,
    val author: List<Any?>,
    val content: String,
    val contentType: String,
    val createdAt: String,
    val duration: List<Duration>,
    val fileIds: List<Any?>,
    val fileNames: List<FileName>,
    val fileSizes: List<FileSize>,
    val fileTypes: List<FileType>,
    val files: List<File>,
    val isReposted: Boolean,
    val numberOfPages: List<NumberOfPage>,
    val originalPostId: String,
    val originalPostReposter: List<OriginalPostReposter>,
    val repostedByUserId: String,
    val repostedUsers: List<String>,
    val tags: List<Any?>,
    val thumbnail: List<Any?>,
    val updatedAt: String
)