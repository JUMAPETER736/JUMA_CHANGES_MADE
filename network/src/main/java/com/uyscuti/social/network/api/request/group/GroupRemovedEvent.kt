package com.uyscuti.social.network.api.request.group

data class GroupRemovedEvent(
    val chatId: String,
    val removedUserId: String
)