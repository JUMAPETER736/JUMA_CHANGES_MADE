package com.uyscuti.social.network.api.response.likeunlikeshort

import com.uyscuti.social.network.api.response.likeunlikeshort.Data

data class LikeUnLikeShortResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)