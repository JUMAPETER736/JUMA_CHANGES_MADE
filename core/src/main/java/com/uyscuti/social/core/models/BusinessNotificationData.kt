package com.uyscuti.social.core.models

import java.io.Serializable
import java.util.Date

data class BusinessNotificationData(
    val owner: User,
    val businessId: String,
    val businessName: String,
    val businessDescription: String,
    val distance: String,
    val image: ImageData? = null,
    val items: List<Any> = emptyList(),
)

data class ImageData(
    val url: String
): Serializable

data class User(
    val userId: String,
    val avatar: String,
    val username: String
): Serializable


data class AdsNotification(
    val owner: User,
    val businessProfileId: String,
    val businessName: String,
    val description: String,
    val distance: String,
    val imageUrl: String? = null,
    val items: List<String> = emptyList(),
    ): Serializable

data class Product(
    val owner: String,
    val itemName: String,
    val images: List<String> = emptyList(),
    val price: String
): Serializable


data class BillboardAdvertisement(
   val owner: User,
   val businessId: String,
   val businessName: String,
   val businessDescription: String,
   val city: String,
   val image: ImageData? = null,
   val items: List<Product> = emptyList()
): Serializable


data class UserData(
    val _id: String,
    val avatar: String? = null,
    val email: String,
    val isEmailVerified: Boolean,
    val role: String,
    val username: String,
    val lastSeen: Date
): Serializable
