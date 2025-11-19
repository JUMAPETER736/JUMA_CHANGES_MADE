package com.uyscuti.social.core.common.data.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uyscuti.social.core.common.data.room.converters.ShortsConverter
import com.uyscuti.social.core.common.data.room.converters.UserConverter
import com.uyscuti.social.core.common.data.room.dao.CallLogDao
import com.uyscuti.social.core.common.data.room.dao.CommentFilesDao
import com.uyscuti.social.core.common.data.room.dao.DialogDao
import com.uyscuti.social.core.common.data.room.dao.FollowListDao
import com.uyscuti.social.core.common.data.room.dao.FollowUnFollowDao
import com.uyscuti.social.core.common.data.room.dao.GroupDialogDao
import com.uyscuti.social.core.common.data.room.dao.LocalUserDao
import com.uyscuti.social.core.common.data.room.dao.MessageDao
import com.uyscuti.social.core.common.data.room.dao.ProfileDao
import com.uyscuti.social.core.common.data.room.dao.RecentUserDao
import com.uyscuti.social.core.common.data.room.dao.ShortCommentReplyDao
import com.uyscuti.social.core.common.data.room.dao.ShortCommentsDao
import com.uyscuti.social.core.common.data.room.dao.ShortsDao
import com.uyscuti.social.core.common.data.room.dao.TabDao
import com.uyscuti.social.core.common.data.room.dao.UserDao
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity
import com.uyscuti.social.core.common.data.room.entity.CommentsFilesEntity
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.core.common.data.room.entity.GroupDialogEntity
import com.uyscuti.social.core.common.data.room.entity.LocalUserEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.ProfileEntity
import com.uyscuti.social.core.common.data.room.entity.RecentUser
import com.uyscuti.social.core.common.data.room.entity.ShortCommentEntity
import com.uyscuti.social.core.common.data.room.entity.ShortCommentReply
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.core.common.data.room.entity.TabsEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity

@Database(
    entities = [MessageEntity::class, DialogEntity::class, GroupDialogEntity::class, UserEntity::class,
        CallLogEntity::class, TabsEntity::class, LocalUserEntity::class, ProfileEntity::class,
        RecentUser::class, ShortsEntity::class, UserShortsEntity::class, FollowUnFollowEntity::class,
               ShortsEntityFollowList::class, ShortCommentEntity::class, ShortCommentReply::class,
        CommentsFilesEntity::class
               ],
    version = 9
)
@TypeConverters(UserConverter::class, ShortsConverter::class)

abstract class ChatDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    abstract fun dialogDao(): DialogDao

    abstract fun groupDialogDao(): GroupDialogDao

    abstract fun userDao(): UserDao

    abstract fun callLogDao(): CallLogDao

    abstract fun tabDao(): TabDao

    abstract fun profileDao(): ProfileDao

    abstract fun localUserDao(): LocalUserDao

    abstract fun recentUserDao(): RecentUserDao

    abstract fun shortsDao(): ShortsDao

    abstract fun followUnFollowDao(): FollowUnFollowDao

    abstract fun shortsEntityFollowListDao(): FollowListDao

    abstract fun shortCommentsDao(): ShortCommentsDao

    abstract fun shortCommentReplyDao(): ShortCommentReplyDao
    abstract fun shortCommentFilesDao(): CommentFilesDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getInstance(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "chat_database"
                ).addMigrations(MIGRATION_X_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Define your migration strategy
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // You should write SQL statements to update the schema here.
                // In your case, you need to create the UserEntity and DialogEntity tables.
                // Note that this is a simplified example; you should replace the table creation SQL
                // with the actual SQL needed for your entities.
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `users` (" +
                            "`id` TEXT NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`avatar` TEXT NOT NULL, " +
                            "`online` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`id`))"
                )

                // Create the DialogEntity table
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `dialogs` (" +
                            "`id` TEXT NOT NULL, " +
                            "`dialogPhoto` TEXT NOT NULL, " +
                            "`dialogName` TEXT NOT NULL, " +
                            "`lastMessage` TEXT NOT NULL, " +
                            "`unreadCount` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`id`))"
                )
            }
        }

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the new table for CallLogEntity
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS call_log (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "callerName TEXT, " +
                            "createdAt INTEGER, " +
                            "callDuration INTEGER, " +
                            "callType TEXT, " +
                            "callStatus TEXT, " +
                            "callerAvatar TEXT, " +
                            "callerId TEXT)"
                )
            }
        }

        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `tabs` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `unreadCount` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }


        private val MIGRATION_XY = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN fileSize INTEGER NOT NULL DEFAULT 0")
            }
        }


        private val MIGRATION_X_2: Migration = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add SQL statement to alter the table and add the 'deleted' column
                db.execSQL("ALTER TABLE messages ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_X_3: Migration = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add SQL statement to alter the 'group_dialogs' table and make 'lastMessage' nullable
                db.execSQL("PRAGMA foreign_keys=off;")
                db.execSQL("CREATE TABLE group_dialogs_temp AS SELECT * FROM group_dialogs;")
                db.execSQL("DROP TABLE group_dialogs;")
                db.execSQL("CREATE TABLE group_dialogs (" +
                        "id TEXT NOT NULL PRIMARY KEY," +
                        "adminId TEXT NOT NULL," +
                        "adminName TEXT NOT NULL," +
                        "dialogPhoto TEXT NOT NULL," +
                        "dialogName TEXT NOT NULL," +
                        "unreadCount INTEGER NOT NULL," +
                        "createdAt INTEGER NOT NULL," +
                        "updatedAt INTEGER NOT NULL," +
                        "lastMessage TEXT," + // Make lastMessage nullable
                        "FOREIGN KEY(adminId) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE" +
                        ");")
                db.execSQL("INSERT INTO group_dialogs SELECT * FROM group_dialogs_temp;")
                db.execSQL("DROP TABLE group_dialogs_temp;")
                db.execSQL("PRAGMA foreign_keys=on;")
            }
        }

        suspend fun clearAllTables(database: ChatDatabase) {
            database.dialogDao().deleteAll()
            database.messageDao().deleteAll()
            database.callLogDao().deleteAll()
            database.tabDao().deleteAll()
            database.userDao().deleteAll()
        }
    }
}
