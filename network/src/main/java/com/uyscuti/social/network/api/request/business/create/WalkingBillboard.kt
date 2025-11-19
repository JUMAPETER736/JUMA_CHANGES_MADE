package com.uyscuti.social.network.api.request.business.create

import com.uyscuti.social.network.api.request.business.create.LocationInformation

data class WalkingBillboard(
    val enabled: Boolean,
    val liveLocationInfo: LocationInformation
)