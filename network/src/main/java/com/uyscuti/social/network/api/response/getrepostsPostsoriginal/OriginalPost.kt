package com.uyscuti.social.network.api.response.getrepostsPostsoriginal

import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.FileName
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.NumberOfPages
import com.uyscuti.social.network.api.response.getfeedandresposts.Duration
import com.uyscuti.social.network.api.response.getfeedandresposts.Thumbnail
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.AuthorX
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.FileSize
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.FileType
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
    val numberOfPages: List<NumberOfPages>,
    val originalPostId: Any,
    val repostedByUserId: Any,
    val repostedUsers: List<String>,
    val tags: List<Any?>,
    val thumbnail: List<Thumbnail>,
    val updatedAt: String,
    val commentCount: Int,
    val likeCount: Int,
):Serializable