package com.uyscuti.social.core.local.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalProfile  @Inject constructor(@ApplicationContext context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_local_profile", Context.MODE_PRIVATE)

    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun setToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    fun setBio(bio: String){
        sharedPreferences.edit().putString("bio", bio).apply()
    }

    fun setFirstName(name: String){
        sharedPreferences.edit().putString("firstname", name).apply()
    }

    fun setLastName(name: String){
        sharedPreferences.edit().putString("lastname", name).apply()
    }

    fun getFirstName(): String{
        return sharedPreferences.getString("firstname", "")?: ""
    }

    fun getLastName(): String{
        return sharedPreferences.getString("lastname", "")?: ""
    }


    fun getBio(): String{
        return sharedPreferences.getString("bio", "")?: ""
    }



    fun setChatId(chatId: String) {
        sharedPreferences.edit().putString("chatId", chatId).apply()
    }

    fun setUserAvatar(userAvatar: String){
        sharedPreferences.edit().putString("userAvatar", userAvatar).apply()
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

    fun setUserName(username: String) {
        sharedPreferences.edit().putString("username", username).apply()
    }

    fun getUserAvatar(): String {
        return sharedPreferences.getString("userAvatar", "") ?: ""
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
        private var instance: SharedStorage? = null

        fun getInstance(context: Context): SharedStorage {
            if (instance == null) {
                instance = SharedStorage(context)
            }
            return instance as SharedStorage
        }
    }
}