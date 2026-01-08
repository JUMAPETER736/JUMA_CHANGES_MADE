package com.uyscuti.social.network.api.request.business.users

import com.uyscut.network.api.request.business.users.LocationInfo

data class BusinessLocation(
    val enabled: Boolean,
    val locationInfo: LocationInfo
)