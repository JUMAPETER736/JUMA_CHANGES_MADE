package com.uyscuti.social.business.retro.request.users

import com.uyscut.network.api.request.business.users.LocationInfo

data class BusinessLocation(
    val enabled: Boolean,
    val locationInfo: LocationInfo
)