package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
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

}