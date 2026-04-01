package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uyscuti.social.core.common.data.room.entity.GroupDialogEntity


@Dao
interface GroupDialogDao {
    @Query("SELECT * FROM group_dialogs")
    fun getGroupDialogs(): LiveData<List<GroupDialogEntity>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertGroupDialog(dialog: GroupDialogEntity)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllGroupDialogs(dialogs: List<GroupDialogEntity>)

    @Query("SELECT * FROM group_dialogs WHERE id = :dialogId")
    fun getGroupDialogById(dialogId: String): LiveData<GroupDialogEntity?>

    @Query("SELECT * FROM group_dialogs WHERE id = :dialogId")
    fun getGroupDialog(dialogId: String): GroupDialogEntity


    @Query("SELECT * FROM group_dialogs WHERE id = :dialogId")
    fun checkGroup(dialogId: String): GroupDialogEntity?


    @Query("UPDATE group_dialogs SET unreadCount = + 1 WHERE id = :dialogId")
    fun updateUnreadCount(dialogId: String)

    @Update
    fun updateLastMessage(dialog: GroupDialogEntity)

    @Update
    fun updateDialog(dialog: GroupDialogEntity)

    @Query("SELECT COUNT(*) FROM group_dialogs WHERE unreadCount > 0")
    fun getLiveUnreadGroupDialogsCount(): LiveData<Int>


    @Query("DELETE FROM group_dialogs")
    fun deleteAll()

    @Query("DELETE FROM group_dialogs WHERE id IN (:dialogIds)")
    suspend fun deleteGroupDialogsByIds(dialogIds: List<String>)

    @Query("DELETE FROM group_dialogs WHERE id = :chatId")
    suspend fun deleteGroupDialogById(chatId: String)

    @Query("UPDATE group_dialogs SET dialogPhoto = :avatarUrl WHERE id = :chatId")
    suspend fun updateGroupAvatar(chatId: String, avatarUrl: String)

    @Query("UPDATE group_dialogs SET dialogName = :name WHERE id = :chatId")
    suspend fun updateGroupName(chatId: String, name: String)

    @Query("UPDATE group_dialogs SET description = :description WHERE id = :chatId")
    suspend fun updateGroupDescription(chatId: String, description: String)

    @Query("SELECT * FROM group_dialogs WHERE id IN (:dialogIds)")
    fun getGroupDialogsByIds(dialogIds: List<String>): LiveData<List<GroupDialogEntity>>

    // Update users list after adding new members
    @Query("UPDATE group_dialogs SET users = :usersJson WHERE id = :chatId")
    suspend fun updateGroupUsers(chatId: String, usersJson: String)

    // Update unread count to 0 (reset)
    @Query("UPDATE group_dialogs SET unreadCount = 0 WHERE id = :chatId")
    suspend fun resetUnreadCount(chatId: String)

    // Update participants count
    @Query("UPDATE group_dialogs SET users = :usersJson WHERE id = :chatId")
    suspend fun updateParticipants(chatId: String, usersJson: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGroupDialog(dialog: GroupDialogEntity)


    @Query("UPDATE group_dialogs SET cachedMembersJson = :json WHERE id = :chatId")
    suspend fun updateCachedMembers(chatId: String, json: String)

    @Query("SELECT cachedMembersJson FROM group_dialogs WHERE id = :chatId")
    suspend fun getCachedMembers(chatId: String): String?

    @Query("UPDATE group_dialogs SET lastMessage = :messageJson WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, messageJson: String)

    @Update
    suspend fun updateDialogSuspend(dialog: GroupDialogEntity)

}