package com.uyscuti.social.network.api.response.allFeedRepostsPost

import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.Duration
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.FileName
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.FileSize
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.NumberOfPages
import com.uyscuti.social.network.api.response.getfeedandresposts.Thumbnail
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Author
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.FileType
import java.io.Serializable


data class OriginalPost(
    val __v: Int,
    val _id: String,
    val author: List<Author>,
    val content: String,
    val contentType: String,
    val createdAt: String,
    val duration: List<Duration>,
    val fileIds: List<Any?>,
    val fileNames: List<FileName>,
    val fileSizes: List<FileSize>,
    val fileTypes: List<FileType>,
    var files: List<File>,
    val isReposted: Boolean,
    val isBookmarkCount: Boolean,
    val isLikedCount: Boolean,
    val numberOfPages: List<NumberOfPages>,
    val originalPostId: String,
    val originalPostReposter: List<OriginalPostReposter>,
    val repostedByUserId: String,
    val repostedUsers: List<String>,
    val tags: List<Any?>,
    val feedShortsBusinessId: String,
    val thumbnail: List<Thumbnail>,
    val updatedAt: String,
    var commentCount: Int,
    val likeCount: Int,
    val shareCount: Int,
    val repostCount: Int,
    val bookmarkCount: Int,
    val bookmarks: List<Any?>,
    val url: String,
    val type: String,
    val profilePicture: String?,
    val username: String?,
    val fullName: String?,
    val profilePicUrl: String?


) : Serializable

