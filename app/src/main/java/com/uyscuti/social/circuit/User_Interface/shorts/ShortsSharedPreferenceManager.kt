package com.uyscuti.social.circuit.User_Interface.shorts

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uyscuti.social.network.api.response.getallshorts.Post

//// Define your data class
//data class MyData(val id: Int, val name: String)

class ShortsSharedPreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ShortsPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Save a list of MyData to SharedPreferences
    fun saveList(myDataList: List<Post>) {
        val jsonString = gson.toJson(myDataList)
        sharedPreferences.edit().putString("myDataList", jsonString).apply()
    }

    // Retrieve a list of MyData from SharedPreferences
    fun getList(): List<Post> {
        val jsonString = sharedPreferences.getString("myDataList", "")
        return if (jsonString.isNullOrEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<Post>>() {}.type
            gson.fromJson(jsonString, type)
        }
    }
}
