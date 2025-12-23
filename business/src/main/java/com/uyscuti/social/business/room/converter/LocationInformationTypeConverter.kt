package com.uyscuti.social.business.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.uyscuti.social.network.api.request.business.create.LocationInformation

class LocationInformationTypeConverter {

    @TypeConverter
    fun locationInfoToString(location: LocationInformation): String {
        return Gson().toJson(location)}

    @TypeConverter
    fun stringToLocationInfo(json: String): LocationInformation {
        return Gson().fromJson(json, LocationInformation::class.java)
    }
}