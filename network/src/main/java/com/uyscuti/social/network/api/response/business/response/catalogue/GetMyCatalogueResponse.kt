package com.uyscuti.social.network.api.response.business.response.catalogue

import com.uyscuti.social.network.api.request.business.catalogue.Data

data class GetMyCatalogueResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)