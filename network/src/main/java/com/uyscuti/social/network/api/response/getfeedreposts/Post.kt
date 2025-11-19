package com.uyscuti.social.network.api.response.getfeedreposts

import com.uyscuti.social.network.api.response.getfeedreposts.more_feed_data_classes.NumberOfPages
import java.io.Serializable

data class Post(
    val __v: Int,
    var _id: String,
    val author: Author,
//    val comments: List<Comment>,
    var comments: Int,
    val content: String,
    val contentType: String,
    val createdAt: String,
    val duration: List<Duration>,
    val feedShortsBusinessId: String,
    val fileIds: List<String>,
    val fileNames: List<FileName>,
    val fileSizes: List<FileSize>,
    val fileTypes: List<FileType>?,
    val files: List<File>,
    val follow: Follow,
//    val isBookmarked: List<Any>,
    var isBookmarked: Boolean,
    val isExpanded: Boolean,
//    var isLiked: List<Any>,
    var isLiked: Boolean,
    val isLocal: Boolean,
    val isReposted: Boolean,
    var likes: Int,
    val numberOfPages: List<NumberOfPages>?,
    val originalPost: List<Any>,
    val repostedByUserId: Any,
    val repostedUsers: List<String>,
    val tags: List<Any>,
    val thumbnail: List<Thumbnail>,
    val updatedAt: String
):Serializable