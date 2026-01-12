package com.uyscuti.social.network.api.response.trialTwo

import com.uyscuti.social.network.api.response.trialTwo.Data

data class TrialTwo(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)