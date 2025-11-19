package com.uyscuti.social.core.common.data.room.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.uyscuti.social.core.common.data.room.entity.MessageEntity


class MessageConverter {
    @TypeConverter
    fun fromMessage(message: MessageEntity?): String {
        if (message == null) {
            // Handle the null case, perhaps by returning an empty string or throwing an exception.
            return ""
        }
        val gson = Gson()
        return gson.toJson(message)
    }

    @TypeConverter
    fun toMessage(messageString: String): MessageEntity? {
        if (messageString.isEmpty()) {
            // Handle the empty string case, perhaps by returning null or throwing an exception.
            return null
        }
        val gson = Gson()
        return gson.fromJson(messageString, MessageEntity::class.java)
    }
}
