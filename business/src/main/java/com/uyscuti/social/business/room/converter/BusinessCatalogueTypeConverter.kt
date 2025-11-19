package com.uyscuti.social.business.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uyscuti.social.business.room.entity.BusinessCatalogueEntity

class BusinessCatalogueTypeConverter {

    @TypeConverter
    fun businessCatalogueToString(businessCatalogue: BusinessCatalogueEntity): String {
        return Gson().toJson(businessCatalogue)
    }

    @TypeConverter
    fun stringToBusinessCatalogue(json: String): BusinessCatalogueEntity {
        return Gson().fromJson(json, BusinessCatalogueEntity::class.java)
    }

    @TypeConverter
    fun fromBusinessCatalogueList(businessCatalogueList: List<BusinessCatalogueEntity>): String {
        val gson = Gson()
        return gson.toJson(businessCatalogueList)
    }

    @TypeConverter
    fun toBusinessCatalogueList(businessCatalogueListString: String): List<BusinessCatalogueEntity> {
        val gson = Gson()
        val type = object : TypeToken<List<BusinessCatalogueEntity>>() {}.type
        return gson.fromJson(businessCatalogueListString, type)
    }

}