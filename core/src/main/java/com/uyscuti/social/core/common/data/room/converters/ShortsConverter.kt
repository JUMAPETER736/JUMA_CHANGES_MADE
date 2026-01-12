package com.uyscuti.social.core.common.data.room.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


import com.uyscuti.social.network.api.response.getallshorts.Author
import com.uyscuti.social.network.api.response.getallshorts.Image
import com.uyscuti.social.network.api.response.getallshorts.Thumbnail
import com.uyscuti.social.network.api.response.getmyprofile.Account
import com.uyscuti.social.network.api.response.getmyprofile.CoverImage

class ShortsConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromAuthor(author: Author): String {
        return gson.toJson(author)
    }

    @TypeConverter
    fun toAuthor(authorJson: String): Author {
        return gson.fromJson(authorJson, Author::class.java)
    }

//    private val gson = Gson()

    @TypeConverter
    fun fromImageList(imageList: List<Image>): String {
        return gson.toJson(imageList)
    }

//    @TypeConverter
//    fun toImageList(imageListJson: String): List<Image> {
//        return gson.fromJson(imageListJson, List::class.java) as List<Image>
//    }
    @TypeConverter
    fun toImageList(imageListJson: String): List<Image> {
        val listType = object : TypeToken<List<Image>>() {}.type
        return gson.fromJson(imageListJson, listType)
    }

    @TypeConverter
    fun fromThumbnailList(thumbnailList: List<Thumbnail>): String {
        return gson.toJson(thumbnailList)
    }

    //    @TypeConverter
//    fun toImageList(imageListJson: String): List<Image> {
//        return gson.fromJson(imageListJson, List::class.java) as List<Image>
//    }
    @TypeConverter
    fun toThumbnailList(thumbnailListJson: String): List<Thumbnail> {
        val listType = object : TypeToken<List<Thumbnail>>() {}.type
        return gson.fromJson(thumbnailListJson, listType)
    }


    @TypeConverter
    fun fromStringList(stringList: List<String>): String {
        return gson.toJson(stringList)
    }

    @TypeConverter
    fun toStringList(stringListJson: String): List<String> {
        return gson.fromJson(stringListJson, List::class.java) as List<String>
    }

    @TypeConverter
    fun fromAccount(account: Account): String {
        val gson = Gson()
        return gson.toJson(account)
    }

    @TypeConverter
    fun toAccount(account: String): Account {
        val gson = Gson()
        return gson.fromJson(account, Account::class.java)
    }

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