package com.uyscuti.social.core.common.data.room.converters

import androidx.room.TypeConverter
import com.google.gson.Gson

import com.uyscuti.social.network.api.response.getmyprofile.Account

class AccountConverter {

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
}