package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DialogDao {
    @Query("SELECT * FROM dialogs")
    fun getDialogs(): LiveData<List<DialogEntity>>

    @Query("SELECT * FROM dialogs WHERE dialogName = :dialogName")
    fun observeDialogByName(dialogName: String): LiveData<DialogEntity>

    @Query("SELECT * FROM dialogs WHERE (SELECT COUNT(*) FROM users ) > 1")
    fun getDialogsGreaterThanOneUser(): LiveData<List<DialogEntity>>


    @Transaction
    suspend fun updateDialogEntityId(oldDialogId: String, newChatId: String) {
        // Retrieve the old dialog
        val oldDialog = getDialog(oldDialogId)
//        Log.d("DialogDao", "oldChatId: $oldDialogId")
//        Log.d("DialogDao", "oldChat Name: ${oldDialog.dialogName}")
//        Log.d("DialogDao", "oldChat: $oldDialog")

        // Create a new dialog with the new ID
        val newDialog = oldDialog.copy(id = newChatId)

//        Log.d("DialogDao", "newChatId: $newChatId")
//        Log.d("DialogDao", "newChat Name: ${newDialog.dialogName}")
//        Log.d("DialogDao", "newChat: $newDialog")

        // Insert the new dialog
        insertDialog(newDialog)

        // Delete the old dialog
        deleteDialog(oldDialog)

        val deleted = getDialog(oldDialogId)
//        Log.d("DialogDao", "oldChatId: $oldDialogId")
//        Log.d("DialogDao", "deleted Dialog: $deleted")
    }

    @Query("SELECT * FROM dialogs WHERE id = dialogName")
    fun getTempDialogs(): LiveData<List<DialogEntity>>


//    @Query("SELECT * FROM dialogs WHERE (SELECT COUNT(*) FROM dialogs.users. WHERE users. = dialogs.id) = 1")
//    fun getDialogsWithSingleUser(): List<DialogEntity>

    @Query("SELECT * FROM dialogs")
    fun getGroupDialogs(): LiveData<List<DialogEntity>>

    @Query("SELECT * FROM dialogs WHERE (SELECT COUNT(*) FROM users) = 1")
    fun getPersonalDialogs(): LiveData<List<DialogEntity>>

    @Query("SELECT * FROM dialogs")
    fun getDialogsFlow(): Flow<List<DialogEntity>>

    @Query("SELECT * FROM dialogs")
    suspend fun getDialogList(): List<DialogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDialog(dialog: DialogEntity)

    @Update
    fun updateDialog(dialog: DialogEntity)

    @Query("SELECT * FROM dialogs WHERE id = :dialogId")
    fun getDialogById(dialogId: String): LiveData<DialogEntity?>

    @Query("SELECT * FROM dialogs WHERE dialogName = :dialogName LIMIT 1")
    fun getDialogByName(dialogName: String): LiveData<DialogEntity?>

    @Query("SELECT * FROM dialogs WHERE id = :dialogId")
     suspend fun getDialog(dialogId: String): DialogEntity

    @Query("SELECT * FROM dialogs WHERE id = :dialogId")
    suspend fun checkDialog(dialogId: String): DialogEntity?

    @Query("SELECT * FROM dialogs WHERE dialogName = :name")
    suspend fun checkDialogByName(name: String): DialogEntity?

    // Modify the function to return a Flow<DialogEntity?>
    @Query("SELECT * FROM dialogs WHERE id = :dialogId")
    fun getDialogByIdFlow(dialogId: String): Flow<DialogEntity?>


//    @Query("UPDATE dialogs SET lastMessage = :newLastMessage WHERE id = :dialogId")
//    fun updateLastMessage(dialogId: String, newLastMessage: MessageEntity)

    @Query("UPDATE dialogs SET unreadCount = + 1 WHERE id = :dialogId")
    fun updateUnreadCount(dialogId: String)

    @Update
    fun updateLastMessage(dialog: DialogEntity)


    @Update
    suspend fun replaceDialog(oldDialog: DialogEntity, newDialog: DialogEntity)

    @Query("DELETE FROM dialogs")
    fun deleteAll()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDialogs(dialogs: List<DialogEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewDialogs(dialogs: List<DialogEntity>)

    @Query("SELECT COUNT(*) FROM dialogs WHERE unreadCount > 0")
    suspend fun getUnreadDialogsCount(): Int

    @Query("SELECT COUNT(*) FROM dialogs WHERE unreadCount > 0")
    fun getLiveUnreadDialogsCount(): LiveData<Int>

    @Delete
    fun deleteDialog(dialog: DialogEntity)

    @Query("DELETE FROM dialogs WHERE id IN (:dialogIds)")
    suspend fun deleteDialogsByIds(dialogIds: List<String>)

//    @Query("SELECT * FROM dialogs ORDER BY (SELECT MAX(createdAt) FROM messages WHERE messages.id = dialogs.lastMessageId) DESC LIMIT 1")
//    fun getRecentlyUpdatedDialog(): LiveData<DialogEntity?>

}
