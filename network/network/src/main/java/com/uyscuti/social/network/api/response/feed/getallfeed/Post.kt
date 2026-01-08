package com.uyscuti.social.network.api.response.feed.getallfeed

import com.uyscuti.social.network.api.response.feed.getallfeed.Author
import com.uyscuti.social.network.api.response.feed.getallfeed.FeedThumbnail
import com.uyscuti.social.network.api.response.feed.getallfeed.File
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.Duration
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.FileName
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.FileType
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.NumberOfPages
import java.io.Serializable

data class Post(
    val __v: Int,
    var _id: String,
    val author: Author,
    var comments: Int,
    val content: String,
    val contentType: String,
    val createdAt: String,
    val duration: List<Duration>?,
    val files: List<File>,
    var isBookmarked: Boolean,
    var isLiked: Boolean,
    var likes: Int,
    val tags: List<String>,
    val thumbnail: List<FeedThumbnail>,
    val fileTypes: List<FileType>?,
    val fileNames: List<FileName>?,
    val numberOfPages: List<NumberOfPages>?,
    val fileIds: List<String>,
    val updatedAt: String,
    var isExpanded: Boolean = false,
    var isLocal:Boolean = false,
    var feedShortsBusinessId: String = "",
    var replies: Int? = 0,


    ): Serializable