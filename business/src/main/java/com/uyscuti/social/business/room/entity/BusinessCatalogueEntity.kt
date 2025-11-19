package com.uyscuti.social.business.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_catalogues")
data class BusinessCatalogueEntity(
    @PrimaryKey val _id: String,
    val description: String,
    val images: List<String>,
    val price: String,
    val title: String
)