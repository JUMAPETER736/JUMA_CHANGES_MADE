package com.uyscuti.social.network.api.response.post

import com.uyscuti.social.network.api.response.post.Data

data class GetPostById(
    val data: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean

)