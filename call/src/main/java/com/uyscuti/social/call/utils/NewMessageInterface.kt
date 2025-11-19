package com.uyscuti.social.call.utils

import com.uyscuti.social.call.models.MessageModel


interface NewMessageInterface {
    fun onNewMessage(message: MessageModel)
}