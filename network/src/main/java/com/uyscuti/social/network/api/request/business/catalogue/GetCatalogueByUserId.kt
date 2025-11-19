package com.uyscuti.social.network.api.request.business.catalogue

import com.uyscuti.social.network.api.request.business.catalogue.Data

data class GetCatalogueByUserId(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)