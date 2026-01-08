package com.uyscuti.social.network.chatsocket

import android.content.Context
import android.content.Intent
import javax.inject.Inject

class ServiceRepository@Inject constructor(
    private val context: Context
) {


    fun startService(){
        val intent = Intent(context, ChatNotificationService::class.java)
        intent.action = ChatNotificationServiceActions.ON_ONE_ON_ONE_MESSAGE.name
        context.startService(intent)
    }
}