package com.uyscuti.social.network.api.response.createRepostFeedPost

import com.uyscuti.social.network.api.response.feed.Image

data class Repost(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val duration: List<Any>,
    val fileIds: List<Any>,
    val fileNames: List<Any>,
    val fileSizes: List<Any>,
    val fileTypes: List<Any>,
    val files: List<Image>,
    val isReposted: Boolean,
    val numberOfPages: List<Any>,
    val originalPostId: String,
    val repostedByUserId: String,
    val repostedUsers: List<Any>,
    val tags: List<Any>,
    val thumbnail: List<Any>,
    val updatedAt: String
)