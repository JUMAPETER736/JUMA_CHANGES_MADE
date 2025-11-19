package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId")
    fun getMessagesByChatId(chatId: String): LiveData<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId")
    suspend fun getMessagesListByChatId(chatId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt DESC LIMIT 20")
    fun getLatestMessagesByChatId(chatId: String): LiveData<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND username = 'You' ORDER BY createdAt DESC LIMIT 1")
    suspend fun getMyLastMessageByChatId(chatId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND username = 'You' AND status = 'Sent'")
    suspend fun getMyListMessageByChatId(chatId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt DESC LIMIT 20")
    suspend fun getLatestMessagesListByChatId(chatId: String): List<MessageEntity>
    @Insert(onConflict =  OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND username = 'You'")
    suspend fun getPendingMessages(chatId: String): List<MessageEntity>

    @Update
    suspend fun updateMessageStatus(messages: List<MessageEntity>)

    @Insert(onConflict =  OnConflictStrategy.IGNORE)
    suspend fun insertMessages(message: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt DESC LIMIT 1")
    fun getLastMessageByChatId(chatId: String): LiveData<MessageEntity?>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastMessage(chatId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND (userId != :myId OR (userId = :myId AND (id LIKE 'Image%' OR id LIKE 'Video%' OR id LIKE 'Audio%' OR id LIKE 'Text%' OR id LIKE 'Doc%'))) ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastMessage(chatId: String, myId: String): MessageEntity?

    // Modify the function to return a Flow<DialogEntity?>
    @Query("SELECT * FROM messages WHERE id = :dialogId")
    fun getMessageById(dialogId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND id = :messageId")
    fun getMessageByChatIdAndId(chatId: String, messageId: String): Flow<MessageEntity?>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND id = :messageId")
    fun getMessageByChatAndId(chatId: String, messageId: String): MessageEntity?

    @Update
    fun updateMessageStatus(message: MessageEntity)

    @Update
    fun markDeleted(messages: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND status = 'Sending'")
    fun getSendingMessagesByChatId(chatId: String): LiveData<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :name AND status = 'Sending'")
    fun getTempMessagesByChatId(name: String): LiveData<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND status = 'Sending'")
    fun getPendingMessagesByChatId(chatId: String): List<MessageEntity>


    @Transaction
    suspend fun markMessageAsSent(chatId: String, messageId: String) {
        val message = getMessageByChatIdAndId(chatId, messageId).firstOrNull()
        if (message != null) {
            message.status = "Sent"
            updateMessageStatus(message)
        }
    }

    @Query("DELETE FROM messages")
    fun deleteAll()


    @Query("SELECT COUNT(*) FROM messages")
    fun getMessageCount(): LiveData<Int>

    // Alternatively, if you prefer a suspend function:
    @Query("SELECT COUNT(*) FROM messages")
    suspend fun getMessageCountSuspend(): Int

    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId")
    fun getMessageCountByChatId(chatId: String): LiveData<Int>

    // Alternatively, if you prefer a suspend function:
    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId")
    suspend fun getMessageCountByChatIdSuspend(chatId: String): Int

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    // Alternatively, if you want to delete messages by ID:
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Delete
    suspend fun deleteMessages(messages: List<MessageEntity>)

    // Alternatively, if you want to delete messages by IDs:
    @Query("DELETE FROM messages WHERE id IN (:messageIds)")
    suspend fun deleteMessagesByIds(messageIds: List<String>)



    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)

//    @Query("SELECT * FROM messages WHERE chatId = :chatId AND createdAt > :startDate ORDER BY createdAt")
//    fun getMessagesAfter(chatId: String, startDate: Date): List<MessageEntity>

    // Other query and update methods as needed
}
