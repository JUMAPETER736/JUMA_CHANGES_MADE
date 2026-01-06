package com.uyscuti.social.business.viewmodel.notificationViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.sharedmodule.model.notifications_data_class.INotification
import com.uyscuti.social.business.repository.IFlashApiRepositoryImplementation
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val retrofitInstance: RetrofitInstance
) : ViewModel() {

    private val repository = IFlashApiRepositoryImplementation(retrofitInstance)
    private val _notifications = MutableLiveData<List<INotification>>()
    val notifications: LiveData<List<INotification>> get() = _notifications

    private val _selectedNotificationCount = MutableLiveData<Int>()
    val selectedNotificationCount: LiveData<Int> get() = _selectedNotificationCount

    private val _notificationCount = MutableStateFlow(0)

    val notificationCount = _notificationCount.asStateFlow()

    // List to keep track of selected dialog items
    private val _selectedNotificationList = mutableListOf<INotification>()
    val selectedNotificationList: List<INotification> get() = _selectedNotificationList

    private val _deleteSelectedList = MutableLiveData<List<INotification>>()
    val deleteSelectedList: LiveData<List<INotification>> get() = _deleteSelectedList


    // Single shared flow - only one polling job
    private val notificationFlow = repository.observeNewNotification()
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Stop after 5s of no subscribers
            replay = 1 // Cache latest value
        )


    init {
        loadUnreadNotification()
       // observeRealTimeNotificationUpdate()
    }


    fun getDeletedNotification(): MutableLiveData<List<INotification>> {
        return _deleteSelectedList
    }

    fun setNotifications(notifications: ArrayList<INotification>) {

        _notifications.value = notifications
    }

    fun addNotification(notification: INotification) {
        val currentList = _notifications.value?.toMutableList() ?: mutableListOf()
        currentList.add(notification)
        _notifications.value = currentList
    }

    fun clearNotification() {
        _notificationCount.value = 0
    }

    fun loadUnreadNotification() {
        // Start observing immediately
        viewModelScope.launch {
            notificationFlow
                .catch { e -> Log.e("NotificationVM", "Error", e) }
                .collect { count ->
                    _notificationCount.value = count
                }
        }
    }

    fun observeRealTimeNotificationUpdate() {
        viewModelScope.launch {
            repository.observeNewNotification().collect { count ->
                _notificationCount.value = count
            }
        }
    }

    fun removeNotification(notification: INotification) {
        val currentList = _notifications.value?.toMutableList()
        currentList?.remove(notification)
        _notifications.value = currentList!!
    }

    fun deleteNotification(notification: INotification) {
        val currentList = _notifications.value.orEmpty().toMutableList() ?: mutableListOf()
        currentList.add(notification)
        _deleteSelectedList.value = currentList
    }

    fun deleteNotifications(notifications: List<INotification>) {
        val currentList = _notifications.value?.toMutableList() ?: mutableListOf()
        currentList.addAll(notifications)
        _deleteSelectedList.value = currentList
    }


    fun resetSelectedNotificationCount() {
        _selectedNotificationCount.value = 0
        _selectedNotificationList.clear()
    }

    fun incrementAndAddToSelectedNotifications(item: INotification) {
        _selectedNotificationCount.value = (_selectedNotificationCount.value ?: 0) + 1
        _selectedNotificationList.add(item)
    }

    fun decrementAndRemoveFromSelectedNotifications(item: INotification) {
        val currentCountN = _notificationCount.value ?: 0
        val currentCount = _selectedNotificationCount.value ?: 0

        if (currentCountN  > 0) {
            _notificationCount.value = (currentCountN - 1)
        }

        if (currentCount > 0) {
            _selectedNotificationCount.value = currentCount - 1
            _selectedNotificationList.remove(item)
        }
    }
}
