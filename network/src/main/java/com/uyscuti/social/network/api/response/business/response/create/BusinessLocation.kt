package com.uyscuti.social.network.api.response.business.response.create

import com.uyscuti.social.network.api.request.business.create.LocationInformation

data class BusinessLocation(
    val enabled: Boolean,
    val locationInfo: LocationInformation
)