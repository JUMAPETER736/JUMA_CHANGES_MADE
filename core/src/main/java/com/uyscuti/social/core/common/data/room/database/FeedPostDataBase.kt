package com.uyscuti.social.core.common.data.room.database//package com.uyscut.core.common.data.room.database
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import com.uyscut.core.common.data.room.dao.FeedPostDao
//import com.uyscut.core.common.data.room.entity.FeedPostsEntity
//
//@Database(entities = [FeedPostsEntity::class], version = 1, exportSchema = false)
//abstract class FeedPostDataBase: RoomDatabase(){
//    abstract fun feedPostDao(): FeedPostDao
//    companion object{
//        @Volatile
//        private var INSTANCE : FeedPostDataBase? = null
//        fun getInstance(context: Context): Any {
//            return INSTANCE ?: synchronized(this){
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    FeedPostDataBase::class.java,
//                    "feed_posts"
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//
//}