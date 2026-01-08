package com.uyscuti.social.network.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStorage @Inject constructor(@ApplicationContext context: Context) {

    companion object {
        private var instance: LocalStorage? = null
        private const val KEY_FOLLOWING_LIST = "following_list"
        private const val KEY_USER_COINS = "user_coins"
        fun getInstance(context: Context): LocalStorage {
            if (instance == null) {
                instance = LocalStorage(context)
            }
            return instance as LocalStorage
        }
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("LocalSettings", Context.MODE_PRIVATE)

    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)


    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun setToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    fun setChatId(chatId: String) {
        sharedPreferences.edit().putString("chatId", chatId).apply()
    }

    fun getChatId(): String {
        return sharedPreferences.getString("chatId", "") ?: ""
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

    fun setUserName(user: String) {
        sharedPreferences.edit().putString("username", user).apply()
    }


    fun getUserCoins(): Int {
        return prefs.getInt(KEY_USER_COINS, 0) // default 0 coins
    }

    fun updateUserCoins(newBalance: Int) {
        prefs.edit().putInt(KEY_USER_COINS, newBalance).apply()
    }

    fun addUserCoins(amount: Int) {
        val current = getUserCoins()
        updateUserCoins(current + amount)
    }

    fun deductUserCoins(amount: Int): Boolean {
        val current = getUserCoins()
        return if (current >= amount) {
            updateUserCoins(current - amount)
            true
        } else {
            false
        }
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

    fun saveUserData(
        userId: String,
        username: String,
        email: String?,
        avatarUrl: String?,
        fullName: String,
        accessToken: String?
    ) {
        sharedPreferences.edit().apply {
            putString("user_id", userId)
            putString("username", username)
            putString("email", email)
            putString("avatar_url", avatarUrl)
            putString("full_name", fullName)
            putString("access_token", accessToken)
            apply()
        }
    }



    fun saveFollowingList(json: String) {
        sharedPreferences.edit().putString(KEY_FOLLOWING_LIST, json).apply()
    }

    fun getFollowingList(): Set<String> {
        return try {
            val json = sharedPreferences.getString(KEY_FOLLOWING_LIST, null)
            if (json != null) {
                val list = Gson().fromJson<List<String>>(
                    json,
                    object : TypeToken<List<String>>() {}.type
                )
                list.toSet()
            } else {
                emptySet()
            }
        } catch (e: Exception) {
            Log.e("LocalStorage", "Error loading following list", e)
            emptySet()
        }
    }

    fun clearFollowingList() {
        sharedPreferences.edit().remove(KEY_FOLLOWING_LIST).apply()
    }


}
