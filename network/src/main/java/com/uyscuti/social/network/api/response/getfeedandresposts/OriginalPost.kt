package com.uyscuti.social.network.api.response.getfeedandresposts


import com.uyscuti.social.network.api.response.getfeedandresposts.AuthorX
import com.uyscuti.social.network.api.response.getfeedandresposts.Duration
import com.uyscuti.social.network.api.response.getfeedandresposts.File
import com.uyscuti.social.network.api.response.getfeedandresposts.FileName
import com.uyscuti.social.network.api.response.getfeedandresposts.FileSize
import com.uyscuti.social.network.api.response.getfeedandresposts.FileType
import com.uyscuti.social.network.api.response.getfeedandresposts.NumberOfPage
import java.io.Serializable

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
    val originalPostId: Any,
    val repostedByUserId: Any,
    val repostedUsers: List<String>,
    val tags: List<Any?>,
    val thumbnail: List<Any?>,
    val updatedAt: String
):Serializable

