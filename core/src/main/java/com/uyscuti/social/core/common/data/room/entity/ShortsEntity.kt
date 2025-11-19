package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.uyscuti.social.core.common.data.room.converters.ShortsConverter
import com.uyscuti.social.network.api.response.getallshorts.Author
import com.uyscuti.social.network.api.response.getallshorts.Image
import com.uyscuti.social.network.api.response.getallshorts.Thumbnail

import java.io.Serializable

@Entity(tableName = "shorts")
@TypeConverters(ShortsConverter::class)
data class ShortsEntity (
    val __v: Int,
    @PrimaryKey val _id: String,
    val author: Author,
    var comments: Int,
    val content: String,
    val createdAt: String,
    val images: List<Image>,
    val thumbnail: List<Thumbnail>,
    var isBookmarked: Boolean,
    var isLiked: Boolean,
    var likes: Int,
    val tags: List<String>,
    val updatedAt: String,
    val feedShortsBusinessId: String = ""
):Serializable

