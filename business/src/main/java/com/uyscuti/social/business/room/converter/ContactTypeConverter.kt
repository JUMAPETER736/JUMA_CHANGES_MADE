package com.uyscuti.social.business.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.uyscuti.social.network.api.request.business.create.Contact


class ContactTypeConverter {
    @TypeConverter
    fun contactToString(contact: Contact): String {
        return Gson().toJson(contact)
    }

    @TypeConverter
    fun stringToContact(json: String): Contact {
        return Gson().fromJson(json, Contact::class.java)
    }
}