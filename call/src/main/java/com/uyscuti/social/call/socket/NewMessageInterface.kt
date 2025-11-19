package com.uyscuti.social.call.socket

import com.uyscuti.social.call.socket.MessageModel


interface NewMessageInterface {
    fun onNewMessage(message: MessageModel)
}