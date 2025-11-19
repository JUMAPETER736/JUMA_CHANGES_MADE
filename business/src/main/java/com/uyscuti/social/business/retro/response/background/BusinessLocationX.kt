package com.uyscuti.social.business.retro.response.background

import com.uyscuti.social.business.retro.request.create.LocationInformation


data class BusinessLocationX(
    val enabled: Boolean,
//    val locationInfo: String
    val locationInfo: LocationInformation
)