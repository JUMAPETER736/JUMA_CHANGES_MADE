package com.uyscuti.social.business.retro.response.create

import com.uyscuti.social.network.api.request.business.create.BusinessLocation

data class Location(
    val businessLocation: BusinessLocation,
    val walkingBillboard: WalkingBillboard
)