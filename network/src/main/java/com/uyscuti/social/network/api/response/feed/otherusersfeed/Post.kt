package com.uyscuti.social.network.api.response.feed.otherusersfeed

import com.uyscuti.social.network.api.response.feed.otherusersfeed.Author
import com.uyscuti.social.network.api.response.feed.otherusersfeed.Duration
import com.uyscuti.social.network.api.response.feed.otherusersfeed.File
import com.uyscuti.social.network.api.response.feed.otherusersfeed.FileName
import com.uyscuti.social.network.api.response.feed.otherusersfeed.FileSize
import com.uyscuti.social.network.api.response.feed.otherusersfeed.FileType
import com.uyscuti.social.network.api.response.feed.otherusersfeed.NumberOfPage

data class Post(
    val __v: Int,
    val _id: String,
    val author: Author,
    val comments: Int,
    val content: String,
    val contentType: String,
    val createdAt: String,
    val duration: List<Duration>,
    val fileIds: List<String>,
    val fileNames: List<FileName>,
    val fileSizes: List<FileSize>,
    val fileTypes: List<FileType>,
    val files: List<File>,
    val isBookmarked: Boolean,
    val isLiked: Boolean,
    val likes: Int,
    val numberOfPages: List<NumberOfPage>,
    val tags: List<String>,
    val thumbnail: List<Any>,
    val updatedAt: String
)