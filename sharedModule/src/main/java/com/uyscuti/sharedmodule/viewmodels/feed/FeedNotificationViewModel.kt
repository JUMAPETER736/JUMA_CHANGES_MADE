package com.uyscuti.sharedmodule.viewmodels.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.network.api.response.getUnifiedNotification.FeedNotification
import com.uyscuti.social.notifications.feed.FeedNotificationRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedNotificationViewModel(
    val repository: FeedNotificationRepo
): ViewModel() {

    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val response = repository.getAllUserNotifications(page = 1, limit = 10)

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    _state.value = NotificationState(
                        notifications = data.data,
                        currentPage = data.currentPage,
                        totalPages = data.totalPages,
                        hasNextPage = data.hasNextPage,
                        isLoading = false,
                        isLoadingMore = false,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to load notifications"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }


