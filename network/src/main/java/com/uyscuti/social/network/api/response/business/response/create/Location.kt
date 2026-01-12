package com.uyscuti.social.network.api.response.business.response.create

import com.uyscuti.social.network.api.request.business.create.BusinessLocation
import com.uyscuti.social.network.api.request.business.create.WalkingBillboard

data class Location(
    val businessLocation: BusinessLocation,
    val walkingBillboard: WalkingBillboard
)