package com.uyscuti.social.network.api.response.business.response.create

import com.uyscuti.social.network.api.response.business.response.create.Business

data class CreateProfileResponse(
    val business: Business,
    val message: String
)