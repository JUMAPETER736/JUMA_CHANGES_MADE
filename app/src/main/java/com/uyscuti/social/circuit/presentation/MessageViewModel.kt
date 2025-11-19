package com.uyscuti.social.circuit.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.repository.MessageRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _allMessages = mutableListOf<MessageEntity>()
    var allMessages: List<MessageEntity> = _allMessages

    private val _pendingMessages = MutableLiveData<List<MessageEntity>>()
    var pendingMessages: LiveData<List<MessageEntity>> = _pendingMessages

    suspend fun getMessages(chatId: String) {
        try {
            val messages = messageRepository.getMessagesListByChatId(chatId)
            _allMessages.clear()
            _allMessages.addAll(messages)
            allMessages = _allMessages
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getPendingMessages(chatId: String) {
        try {
            val messages = messageRepository.getPendingMessages(chatId)
            _pendingMessages.value = messages
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearAll(){
        messageRepository.clearAll()
    }

    suspend fun observePendingMessages(chatId: String): LiveData<List<MessageEntity>> {
        return messageRepository.getSendingMessages(chatId)
    }

    suspend fun observeTempMessages(name: String): LiveData<List<MessageEntity>>{
        return messageRepository.getTempMessages(name)
    }

    suspend fun updateMessageStatus(message: MessageEntity) {
        try {
            messageRepository.updateMessageStatus(message)
        } catch (e:Exception){
            e.printStackTrace()
        }
    }

    suspend fun updateMessageStatus(messageId: String, newStatus: String) {
        try {
            val message = messageRepository.getMessageByMessageId(messageId)
            if (message != null) {
                message.status = newStatus
                messageRepository.updateMessageStatus(message)
            }
        } catch (e: Exception){
            Log.e("MessageViewModel", "Error updating message status: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun markMessagesDeleted(ids: List<String>){
        try {
            messageRepository.markMessagesDeleted(ids)
        } catch (e:Exception){
            e.printStackTrace()
        }
    }

    suspend fun getMyMessages(chatId: String): List<MessageEntity>{
        return messageRepository.getMyMessages(chatId)
    }

    suspend fun deleteMessages(ids: List<String>){
        try {
            messageRepository.deleteMessages(ids)
        } catch (e:Exception){
            Log.e("Deletion", "Deletion failed : ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun deleteMessagesByChat(chatId: String){
        try {
            messageRepository.deleteMessageByChat(chatId)
        } catch (e:Exception){
            Log.e("Deletion", "Deletion failed : ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun getLastMessage(chatId: String): MessageEntity?{
        return messageRepository.getLastMessage(chatId)
    }

    suspend fun getLastMessage(chatId: String, myId: String): MessageEntity?{
        return messageRepository.getLastMessage(chatId, myId)
    }

    suspend fun getMessage(messageId: String): MessageEntity?{
        return messageRepository.getMessageByMessageId(messageId)
    }

    suspend fun getMyLastMessageByChat(chatId: String): MessageEntity?{
        return messageRepository.getMyLastMessageByChatId(chatId)
    }

    suspend fun updateMessage(message: MessageEntity){
        try {
            messageRepository.updateMessage(message)
        } catch (e:Exception){
            e.printStackTrace()
        }
    }

    suspend fun markDeleted(chatId: String){
        try {
            messageRepository.markDeletedByChat(chatId)
        } catch (e:Exception){
            e.printStackTrace()
        }
    }

    suspend fun messages(chatId: String): List<MessageEntity>{
        return messageRepository.getMessagesListByChatId(chatId)
    }

    suspend fun getLastMessagesByChatId(chatId: String): List<MessageEntity>{
        return messageRepository.getLastMessagesByChatId(chatId)
    }

    suspend fun insertMessage(message: MessageEntity) {
        try {
            messageRepository.insertMessage(message)
        } catch (e:Exception){
            e.printStackTrace()
        }
    }
}