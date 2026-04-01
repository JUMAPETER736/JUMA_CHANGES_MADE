package com.uyscuti.social.network.api.request.group

data class GroupMembersResponse(
    val statusCode: Int,
    val success: Boolean,
    val message: String?,
    val data: List<GroupMember>?
)