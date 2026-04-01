package com.uyscuti.social.network.api.request.group

data class ChangeRoleRequest(
    val role: String                  // "admin" | "moderator" | "member"
)