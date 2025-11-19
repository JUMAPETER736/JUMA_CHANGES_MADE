package com.uyscuti.social.core.common.data.room.repository

import java.util.Date
import com.uyscuti.social.core.common.data.room.dao.MessageDao

class RoomMessageLoader(private val messageDao: MessageDao) {
    private var lastLoadedDate: Date = Date()

//    fun loadMessagesFromDatabase(chatId: String): List<MessageEntity> {
//        val messages = messageDao.getMessagesAfter(chatId, lastLoadedDate)
//
//        if (messages.isNotEmpty()) {
////            lastLoadedDate = messages.last().createdAt
//            messages.last()
//        }
//
//        return messages
//    }
}
