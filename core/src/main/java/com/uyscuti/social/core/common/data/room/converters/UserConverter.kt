package com.uyscuti.social.core.common.data.room.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import java.util.Date

class UserConverter {
    @TypeConverter
    fun fromUserList(userList: List<UserEntity>): String {
        val gson = Gson()
        return gson.toJson(userList)
    }

    @TypeConverter
    fun toUserList(userListString: String): List<UserEntity> {
        val gson = Gson()
        val type = object : TypeToken<List<UserEntity>>() {}.type
        return gson.fromJson(userListString, type)
    }

    @TypeConverter
    fun userEntityToString(userEntity: UserEntity): String {
        val gson = Gson()

        return gson.toJson(userEntity)
    }

    @TypeConverter
    fun stringToUserEntity(data: String): UserEntity {
        val gson = Gson()

        val type = object : TypeToken<UserEntity>() {}.type
        return gson.fromJson(data, type)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

