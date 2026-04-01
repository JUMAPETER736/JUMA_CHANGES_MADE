package com.uyscuti.social.network.api.request.group

data class LastMessageData(
    val _id: String,
    val content: String?,
    val encryptedContent: String?,
    val isEncrypted: Boolean,
    val sender: GroupMemberUser?,
    val createdAt: String?
)