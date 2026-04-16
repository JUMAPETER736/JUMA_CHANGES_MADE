package com.uyscuti.sharedmodule.viewmodels.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uyscuti.social.notifications.feed.FeedNotificationRepo

class FeedNotificationViewModelFactory(
    val repository: FeedNotificationRepo
) : ViewModelProvider.Factory  {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedNotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FeedNotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}