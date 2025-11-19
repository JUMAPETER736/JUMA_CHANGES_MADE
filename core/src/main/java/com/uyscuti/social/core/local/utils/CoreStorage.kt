package com.uyscuti.social.core.local.utils

import android.content.Context
import android.content.SharedPreferences

class CoreStorage(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("LocalSettings", Context.MODE_PRIVATE)

    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun setToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    fun setThisCallAvatar(token: String) {
        sharedPreferences.edit().putString("thiscallavatar", token).apply()
    }

    fun getThisCallAvatar(): String {
        return sharedPreferences.getString("thiscallavatar", "") ?: ""
    }





    fun setChatId(chatId: String) {
        sharedPreferences.edit().putString("chatId", chatId).apply()
    }

    fun getChatId(): String {
        return sharedPreferences.getString("chatId", "") ?: ""
    }

    fun clearThisCallAvatar(){
        sharedPreferences.edit().remove("thiscallavatar").apply()
    }


    fun clearChatId(){
        sharedPreferences.edit().remove("chatId").apply()
    }

    fun getToken(): String {
        return sharedPreferences.getString("token", "") ?: ""
    }

    fun clearToken() {
        sharedPreferences.edit().remove("token").apply()
    }

    fun setUserId(userId: String) {
        sharedPreferences.edit().putString("userId", userId).apply()
    }

    fun getUserId(): String {
        return sharedPreferences.getString("userId", "") ?: ""
    }

    fun getUsername(): String {
        return sharedPreferences.getString("username", "") ?: ""
    }

    fun setUser(user: String) {
        sharedPreferences.edit().putString("user", user).apply()
    }

    fun getUser(): String {
        return sharedPreferences.getString("user", "") ?: ""
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun saveInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private var instance: CoreStorage? = null

        fun getInstance(context: Context): CoreStorage {
            if (instance == null) {
                instance = CoreStorage(context)
            }
            return instance as CoreStorage
        }
    }
}