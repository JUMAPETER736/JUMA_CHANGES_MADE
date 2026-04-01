package com.uyscuti.social.network.api.request.group

data class GroupChatDetail(
    val _id: String,
    val name: String,
    val isGroupChat: Boolean,
    val admin: String,
    val members: List<GroupMember>,
    val participants: List<GroupMemberUser>,
    val inviteToken: String?,
    val inviteTokenEnabled: Boolean,
    val editInfoLocked: Boolean = false,
    val lastMessage: LastMessageData?,
    val createdAt: String?,
    val updatedAt: String?,
    val description: String? = null,
    val groupAvatar: AvatarData? = null
)