package com.uyscuti.social.business.retro.request.catalogue

import com.uyscuti.social.business.retro.request.catalogue.Data

data class GetCatalogueByUserId(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)