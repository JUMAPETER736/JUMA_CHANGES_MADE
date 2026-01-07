package com.uyscuti.social.network.api.response.getrepostsPostsoriginal

import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Data
import java.io.Serializable

data class GetRepostsPostsOriginal(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
):Serializable