package com.uyscuti.social.network.api.request.group

data class GroupLinkData(
    val inviteLink: String,           // full URL e.g. "https://yourapp.com/join/group/abc123"
    val inviteToken: String,          // just the token
    val generatedBy: String           // userId
)