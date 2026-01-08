package com.uyscuti.social.network.api.request.group

data class RequestGroupChat(
    val name: String,
    val participants: List<String>
)