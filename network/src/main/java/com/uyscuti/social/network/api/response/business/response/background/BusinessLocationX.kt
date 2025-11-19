package com.uyscuti.social.network.api.response.business.response.background

import com.uyscuti.social.network.api.request.business.create.LocationInformation


data class BusinessLocationX(
    val enabled: Boolean,
//    val locationInfo: String
    val locationInfo: LocationInformation
)