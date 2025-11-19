package com.uyscuti.social.network.api.response.getrepostsPostsoriginal

import com.uyscuti.social.network.api.response.getfeedandresposts.Comment
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.Duration
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.FileName
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.FileSize
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.NumberOfPages
import com.uyscuti.social.network.api.response.getfeedandresposts.Thumbnail
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Author
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.FileType
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Follow
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.OriginalPost
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
    val fileSizes: List<FileSize>,
    val fileTypes: List<FileType>,
    val files: List<File>,
    val follow: Follow,
    var isBookmarked: Boolean,
    val isExpanded: Boolean,
    val isLocal: Boolean,
    var isReposted: Boolean,
    var likes: Int,
    var isLiked : Boolean,
    val numberOfPages: List<NumberOfPages>,
    val originalPost: List<OriginalPost>,
    val repostedByUserId: String,
    val repostedUsers: List<String>,
    val tags: List<Any?>,
    val thumbnail: List<Thumbnail>,
    val updatedAt: String

):Serializable