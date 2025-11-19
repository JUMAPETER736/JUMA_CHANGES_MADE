package com.uyscuti.social.network.api.response.posts


data class File(
    val _id: String,
    val fileId: String,
    val localPath: String,
    val url: String,
    val mimeType: String?,

)