package com.uyscuti.social.business.retro.response.create

import com.uyscuti.social.business.retro.request.create.LocationInformation


data class WalkingBillboard(
    val enabled: Boolean,
    val liveLocationInfo: LocationInformation
)