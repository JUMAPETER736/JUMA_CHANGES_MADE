package com.uyscuti.social.business.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.uyscuti.social.business.room.dao.BusinessDao
import com.uyscuti.social.business.room.entity.BusinessEntity
import com.uyscuti.social.business.room.entity.MyProductEntity


@Database(entities = [BusinessEntity::class, MyProductEntity::class], version = 1)
abstract class   BusinessDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao

    companion object {
        const val DB_NAME = "business.db"

        @Volatile
        private var INSTANCE: BusinessDatabase? = null

        fun getInstance(context: Context): BusinessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BusinessDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}