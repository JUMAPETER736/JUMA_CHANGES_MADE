package com.uyscuti.social.business.retro.response.background

import com.uyscuti.social.business.retro.request.create.LocationInformation


data class WalkingBillboardX(
    val enabled: Boolean,
    val liveLocationInfo: LocationInformation
)