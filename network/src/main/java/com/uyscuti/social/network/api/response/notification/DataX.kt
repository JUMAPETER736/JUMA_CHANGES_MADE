package com.uyscuti.social.network.api.response.notification

data class DataX(
    val __v: Int,
    val _id: String,
    val avatar: String,
    val createdAt: String,
    val message: String,
    val owner: String,
    val read: Boolean,
    val sender: String,
    val updatedAt: String
)