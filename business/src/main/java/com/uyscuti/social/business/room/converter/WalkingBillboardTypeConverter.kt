package com.uyscuti.social.business.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.uyscuti.social.network.api.request.business.create.WalkingBillboard


class WalkingBillboardTypeConverter {

    @TypeConverter
    fun walkingBillboardToString(walkingBillboard: WalkingBillboard): String {return Gson().toJson(walkingBillboard)
    }

    @TypeConverter
    fun stringToWalkingBillboard(json: String): WalkingBillboard {
        return Gson().fromJson(json, WalkingBillboard::class.java)
    }
}