package com.uyscuti.social.network.api.response.getrepostsPostsoriginal

import java.io.Serializable

data class File(
    val _id: String,
    val fileId: String,
    val localPath: String?,
    var url: String,
    val type: String?,
    var mimeType: String?,
    val fileType: String?

):Serializable


