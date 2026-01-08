package com.uyscuti.social.network.api.response.getrepostsPostsoriginal

import java.io.Serializable

data class FileType(
    val postId: String,
    val fileId: String,
    val fileType: String,
    val url: String,
    val type: String
):Serializable