package com.uyscuti.social.business.retro.response.create

import com.uyscuti.social.business.retro.request.create.LocationInformation


data class BusinessLocation(
    val enabled: Boolean,
    val locationInfo: LocationInformation
)