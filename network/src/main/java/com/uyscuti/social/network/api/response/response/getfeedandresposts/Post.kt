package com.uyscuti.social.network.api.response.getfeedandresposts

import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.FileSize
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.NumberOfPages
import java.io.Serializable

data class Post(
    val __v: Int,
    var _id: String,
    val author: List<Author>,
    var comments: List<Comment>,
    val content: String,
    val contentType: String,
    val createdAt: String,
    val duration: List<Duration>,
    val feedShortsBusinessId: String,
    val fileIds: List<String>,
    val fileNames: List<FileName>,
    val fileSizes: List<FileSize>?,
    val fileTypes: List<FileType>,
    val files: List<File>,
    val follow: Follow,
    var isBookmarked: Boolean,
    var isLiked:Boolean = false,
    var isReposted: Boolean = false,
    var likes: Int,
    val numberOfPages: List<NumberOfPages>?,
    val originalPost: List<OriginalPost>,
    var repostedByUserId: Any?,
    var repostedUsers: List<String>,
    val tags: List<Any>,
    val thumbnail: List<Thumbnail>,
    val updatedAt: String,
    var isExpanded: Boolean = false,
    var isLocal: Boolean = false,

    ) : Serializable
