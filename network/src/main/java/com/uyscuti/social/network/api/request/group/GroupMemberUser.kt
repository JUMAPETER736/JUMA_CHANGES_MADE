package com.uyscuti.social.network.api.request.group

data class GroupMemberUser(
    val _id: String,
    val username: String?,
    val fullName: String?,
    val avatar: AvatarData?,
    val email: String?
)