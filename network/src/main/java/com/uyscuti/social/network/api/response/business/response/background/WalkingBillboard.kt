package com.uyscuti.social.network.api.response.business.response.background

import com.uyscuti.social.network.api.request.business.create.LocationInformation

data class WalkingBillboard(
    val enabled: Boolean,
    val liveLocationInfo: LocationInformation
)