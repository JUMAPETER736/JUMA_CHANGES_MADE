package com.uyscuti.social.business.retro.response.catalogue

import com.uyscuti.social.business.retro.response.catalogue.Data

data class GetMyCatalogueResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)