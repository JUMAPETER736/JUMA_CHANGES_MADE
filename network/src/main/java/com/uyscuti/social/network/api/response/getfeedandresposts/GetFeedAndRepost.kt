package com.uyscuti.social.network.api.response.getfeedandresposts

import com.uyscuti.social.network.api.response.getfeedandresposts.Data
import java.io.Serializable

data class GetFeedAndRepost(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
): Serializable