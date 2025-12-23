package com.uyscuti.social.business.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.uyscuti.social.network.api.request.business.create.Location

class LocationTypeConverter {

    @TypeConverter
    fun locationToString(location: Location): String {
        return Gson().toJson(location)}

    @TypeConverter
    fun stringToLocation(json: String): Location {
        return Gson().fromJson(json, Location::class.java)
    }


}