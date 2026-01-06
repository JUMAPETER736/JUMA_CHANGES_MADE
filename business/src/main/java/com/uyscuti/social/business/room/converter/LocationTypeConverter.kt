package com.uyscuti.social.business.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.uyscuti.social.network.api.request.business.create.Location
import com.uyscuti.social.network.api.request.business.create.LocationInformation

class LocationTypeConverter {

    @TypeConverter
    fun locationToString(location: Location): String {
        return Gson().toJson(location)}

    @TypeConverter
    fun stringToLocation(json: String): Location {
        return Gson().fromJson(json, Location::class.java)
    }

}

class Location2TypeConverter {

    @TypeConverter
    fun location2ToString(location: com.uyscuti.social.network.api.response.business.response.profile.Location): String {
        return Gson().toJson(location)}

    @TypeConverter
    fun stringToLocation2(json: String): com.uyscuti.social.network.api.response.business.response.profile.Location {
        return Gson().fromJson(json, com.uyscuti.social.network.api.response.business.response.profile.Location::class.java)
    }

}