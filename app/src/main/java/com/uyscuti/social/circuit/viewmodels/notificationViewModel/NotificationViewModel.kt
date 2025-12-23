package com.uyscuti.social.circuit.viewmodels.notificationViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uyscuti.social.circuit.model.notifications_data_class.INotification


class NotificationViewModel : ViewModel() {

    private val _notifications = MutableLiveData<List<INotification>>()
    val notifications: LiveData<List<INotification>> get() = _notifications

    private val _selectedNotificationCount = MutableLiveData<Int>()
    val selectedNotificationCount: LiveData<Int> get() = _selectedNotificationCount

    // List to keep track of selected dialog items
    private val _selectedNotificationList = mutableListOf<INotification>()
    val selectedNotificationList: List<INotification> get() = _selectedNotificationList

    private val _deleteSelectedList = MutableLiveData<List<INotification>>()
    val deleteSelectedList: LiveData<List<INotification>> get() = _deleteSelectedList


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
        val currentCount = _selectedNotificationCount.value ?: 0
        if (currentCount > 0) {
            _selectedNotificationCount.value = currentCount - 1
            _selectedNotificationList.remove(item)
        }
    }
}
