package com.uyscuti.social.core.common.data.room.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.uyscuti.social.network.api.response.getmyprofile.CoverImage


class CoverImageConverter {
    @TypeConverter
    fun fromCoverImage(coverImage: CoverImage): String {
        val gson = Gson()
        return gson.toJson(coverImage)
    }

    @TypeConverter
    fun toCoverImage(coverImage: String): CoverImage {
        val gson = Gson()
        return gson.fromJson(coverImage, CoverImage::class.java)
    }
}