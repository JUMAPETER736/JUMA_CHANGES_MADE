package com.uyscuti.social.business.retro.request.create

import com.uyscuti.social.business.retro.request.create.LocationInformation

data class WalkingBillboard(
    val enabled: Boolean,
    val liveLocationInfo: LocationInformation
)