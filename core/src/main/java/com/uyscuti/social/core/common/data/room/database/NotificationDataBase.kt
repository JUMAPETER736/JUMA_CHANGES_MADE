package com.uyscuti.social.core.common.data.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.uyscuti.social.core.common.data.room.dao.NotificationDao
import com.uyscuti.social.core.common.data.room.entity.NotificationEntity

@Database(entities = [NotificationEntity::class], version = 1, exportSchema = false)
abstract class NotificationDataBase:RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    companion object {
        @Volatile
        private var INSTANCE: NotificationDataBase? = null
        fun getInstance(context: Context): Any {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotificationDataBase::class.java,
                    "notifications"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}