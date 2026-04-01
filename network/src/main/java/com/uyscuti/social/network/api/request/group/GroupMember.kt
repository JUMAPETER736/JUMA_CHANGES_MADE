package com.uyscuti.social.network.api.request.group

data class GroupMember(
    val user: GroupMemberUser,
    val role: GroupRole,
    val joinedAt:    String?,
    val promotedBy:  String?,
    val isMuted:     Boolean = false
)