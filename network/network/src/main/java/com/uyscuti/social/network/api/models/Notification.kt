package com.uyscuti.social.network.api.models

import java.io.Serializable

data class Notification(
    val _id: String,
    val avatar: String,
    val createdAt: String,
    val message: String,
    val owner: String,
    val read: Boolean,
    val sender: Sender,
    val updatedAt: String,
    val postId : String,
    val commentId :String,
): Serializable