package com.uyscuti.social.network.api.response.business.response.livelocation

import com.uyscuti.social.network.api.response.business.response.background.UpdatedBusinessProfile

data class LiveLocationResponse(
    val message: String,
    val updatedBusinessProfile: UpdatedBusinessProfile
)
