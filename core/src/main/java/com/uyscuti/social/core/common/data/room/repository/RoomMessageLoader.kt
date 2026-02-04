package com.uyscuti.social.core.common.data.room.repository

import java.util.Date
import com.uyscuti.social.core.common.data.room.dao.MessageDao

class RoomMessageLoader(private val messageDao: MessageDao) {
    private var lastLoadedDate: Date = Date()


}
