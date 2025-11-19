package com.uyscuti.social.business.retro.response.create

import com.uyscuti.social.business.retro.response.create.Business

data class CreateProfileResponse(
    val business: Business,
    val message: String
)