package com.uyscuti.social.business.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.uyscuti.social.business.room.converter.StringListTypeConverter


@Entity(tableName = "my-products")
@TypeConverters(StringListTypeConverter::class)
data class MyProductEntity(

    @PrimaryKey val _id: String,
    val __v: Int,
    val catalogue: String,
    val createdAt: String,
    val description: String,
    val features: List<String>,
    val images: List<String>,
    val itemName: String,
    val owner: String,
    val price: String,
    val updatedAt:String
)