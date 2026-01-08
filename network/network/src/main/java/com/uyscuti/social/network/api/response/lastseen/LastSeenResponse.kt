package com.uyscuti.social.network.api.response.lastseen

import com.uyscuti.social.network.api.response.lastseen.LastSeenData

data class LastSeenResponse(
    val status: Int,
    val data: LastSeenData?,
    val message: String
)