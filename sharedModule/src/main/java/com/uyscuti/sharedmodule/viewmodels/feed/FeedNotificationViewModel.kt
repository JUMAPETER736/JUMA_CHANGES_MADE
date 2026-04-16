package com.uyscuti.sharedmodule.viewmodels.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.network.api.response.getUnifiedNotification.FeedNotification
import com.uyscuti.social.notifications.feed.FeedNotificationRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.plus

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


    fun loadNextPage() {
        val currentState = _state.value

        // Prevent multiple simultaneous loads
        if (currentState.isLoadingMore || !currentState.hasNextPage || currentState.isLoading) {
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingMore = true)

            try {
                val nextPage = currentState.currentPage + 1
                val response = repository.getAllUserNotifications(page = nextPage, limit = 10)

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    _state.value = _state.value.copy(
                        notifications = currentState.notifications + data.data,
                        currentPage = data.currentPage,
                        totalPages = data.totalPages,
                        hasNextPage = data.hasNextPage,
                        isLoadingMore = false
                    )
                } else {
                    _state.value = _state.value.copy(isLoadingMore = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoadingMore = false)
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                // Optimistically update UI first
                val updatedNotifications = _state.value.notifications.map { notification ->
                    if (notification._id == notificationId) {
                        notification.copy(read = true)
                    } else {
                        notification
                    }
                }

                _state.value = _state.value.copy(notifications = updatedNotifications)

                // Make API call
                val response = repository.markAsRead(notificationId)

                if (!response.isSuccessful) {
                    // Revert if API call fails
                    val revertedNotifications = _state.value.notifications.map { notification ->
                        if (notification._id == notificationId) {
                            notification.copy(read = false)
                        } else {
                            notification
                        }
                    }
                    _state.value = _state.value.copy(notifications = revertedNotifications)
                }
            } catch (e: Exception) {
                // Revert on error
                val revertedNotifications = _state.value.notifications.map { notification ->
                    if (notification._id == notificationId) {
                        notification.copy(read = false)
                    } else {
                        notification
                    }
                }
                _state.value = _state.value.copy(notifications = revertedNotifications)
            }
        }
    }

    fun markAsUnread(notificationId: String) {
        viewModelScope.launch {
            try {
                // Optimistically update UI first
                val updatedNotifications = _state.value.notifications.map { notification ->
                    if (notification._id == notificationId) {
                        notification.copy(read = false)
                    } else {
                        notification
                    }
                }

                _state.value = _state.value.copy(notifications = updatedNotifications)

                // Make API call
                val response = repository.markAsUnread(notificationId)

                if (!response.isSuccessful) {
                    // Revert if API call fails
                    val revertedNotifications = _state.value.notifications.map { notification ->
                        if (notification._id == notificationId) {
                            notification.copy(read = true)
                        } else {
                            notification
                        }
                    }
                    _state.value = _state.value.copy(notifications = revertedNotifications)
                }
            } catch (e: Exception) {
                // Revert on error
                val revertedNotifications = _state.value.notifications.map { notification ->
                    if (notification._id == notificationId) {
                        notification.copy(read = true)
                    } else {
                        notification
                    }
                }
                _state.value = _state.value.copy(notifications = revertedNotifications)
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                // Store the notification in case we need to revert
                val notificationToDelete = _state.value.notifications.find { it._id == notificationId }
                val originalPosition = _state.value.notifications.indexOfFirst { it._id == notificationId }

                // Optimistically remove from UI first
                val updatedNotifications = _state.value.notifications.filter {
                    it._id != notificationId
                }

                _state.value = _state.value.copy(notifications = updatedNotifications)

                // Make API call
                val response = repository.deleteNotification(notificationId)

                if (!response.isSuccessful) {
                    // Revert if API call fails - add the notification back
                    if (notificationToDelete != null && originalPosition != -1) {
                        val revertedNotifications = _state.value.notifications.toMutableList()
                        revertedNotifications.add(originalPosition, notificationToDelete)
                        _state.value = _state.value.copy(notifications = revertedNotifications)
                    }
                }
            } catch (e: Exception) {
                // On error, you might want to reload all notifications to ensure consistency
                // Or you could try to revert like in the unsuccessful response case
                loadNotifications()
            }
        }
    }

