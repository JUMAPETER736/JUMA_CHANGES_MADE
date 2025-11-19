package com.uyscuti.social.business.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.uyscuti.social.business.room.converter.BusinessCatalogueTypeConverter
import com.uyscuti.social.business.room.converter.BusinessLocationTypeConverter
import com.uyscuti.social.business.room.converter.ContactTypeConverter
import com.uyscuti.social.business.room.converter.LocationInformationTypeConverter
import com.uyscuti.social.business.room.converter.LocationTypeConverter
import com.uyscuti.social.business.room.converter.WalkingBillboardTypeConverter
import com.uyscuti.social.network.api.request.business.create.Contact
import com.uyscuti.social.network.api.request.business.create.Location
import com.uyscuti.social.business.room.entity.BusinessCatalogueEntity


@Entity(tableName = "business")
@TypeConverters(
    LocationInformationTypeConverter::class, ContactTypeConverter::class,
    WalkingBillboardTypeConverter::class,
    BusinessLocationTypeConverter::class, BusinessCatalogueTypeConverter::class,
    LocationTypeConverter::class
   )
data class BusinessEntity(
    @PrimaryKey val _id: String,
    val __v: Int,
    val backgroundPhoto: String,
    val backgroundVideo: String?,
    val videoThumbnail: String?,
    val businessCatalogue: List<BusinessCatalogueEntity>,
    val businessDescription: String,
    val businessName: String,
    val businessType: String,
    val contact: Contact,
    val createdAt: String,
    val location: Location,
    val owner: String,
    val updatedAt: String
)