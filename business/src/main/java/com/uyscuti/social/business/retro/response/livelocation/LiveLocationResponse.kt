package com.uyscuti.social.business.retro.response.livelocation

import com.uyscuti.social.business.retro.response.background.UpdatedBusinessProfile


data class LiveLocationResponse(
    val message: String,
    val updatedBusinessProfile: UpdatedBusinessProfile
)
