package com.uyscuti.social.network.api.response.example2

data class Post(
    val __v: Int,
    val _id: String,
    val author: Author,
    val comments: List<Any?>,
    val content: String,
    val contentType: String,
    val createdAt: String,
    val duration: List<Duration>,
    val fileIds: List<Any?>,
    val fileNames: List<FileName>,
    val fileSizes: List<FileSize>,
    val fileTypes: List<FileType>,
    val files: List<File>,
    val isLiked: Boolean,
    val isReposted: Boolean,
    val likes: Int,
    val numberOfPages: List<NumberOfPage>,
    val originalPost: List<OriginalPost>,
    val repostedUser: RepostedUser,
    val repostedUsers: List<String>,
    val tags: List<Any?>,
    val thumbnail: List<Any?>,
    val updatedAt: String
)