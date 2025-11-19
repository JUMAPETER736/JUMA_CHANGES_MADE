package com.uyscuti.social.business.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringListTypeConverter {

    @TypeConverter
    fun stringListToString(stringList: List<String>): String {
        return Gson().toJson(stringList)
    }

    @TypeConverter
    fun stringToStringList(json: String): List<String> {
        return Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)
    }
}