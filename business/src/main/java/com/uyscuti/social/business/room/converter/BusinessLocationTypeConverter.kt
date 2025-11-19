package com.uyscuti.social.business.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.uyscuti.social.network.api.request.business.create.BusinessLocation

class BusinessLocationTypeConverter {

    @TypeConverter
    fun businessLocationToString(businessLocation: BusinessLocation): String {
        return Gson().toJson(businessLocation)
    }

    @TypeConverter
    fun stringToBusinessLocation(json: String): BusinessLocation {
        return Gson().fromJson(json, BusinessLocation::class.java)
    }
}