package com.uyscuti.social.core.service

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LogoutService(private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("app_pref", Context.MODE_PRIVATE)
    }

    fun logout() {
        // Clear data in Room Database
        clearRoomDatabaseData()

        // Clear data in SharedPreferences
        clearSharedPreferencesData()

        // Redirect to the login activity
        redirectToLoginActivity()
    }

    private fun clearRoomDatabaseData() {
//        lifecycleScope.launch(Dispatchers.IO) {
//            // Perform Room database operations to delete user data
//            // Replace 'YourDatabase' with the actual name of your Room database
//            YourDatabase.getInstance(context).clearUserData()
//        }
    }

    private fun clearSharedPreferencesData() {
        sharedPreferences.edit().clear().apply()
    }

    private fun redirectToLoginActivity() {
        // Create an Intent to start the login activity
//        val intent = Intent(context, LoginActivity::class.java)
//
//
//
//        // Clear the back stack to prevent the user from navigating back to the previous screen
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//
//        // Start the login activity
//        context.startActivity(intent)

        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        context.startActivity(intent)

    }
}
