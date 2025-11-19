package com.uyscuti.social.circuit.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uyscuti.social.circuit.data.model.Dialog
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel() {

    // MutableLiveData for keeping track of the selected dialogs count
    private val _selectedDialogsCount = MutableLiveData<Int>()
    val selectedDialogsCount: LiveData<Int> get() = _selectedDialogsCount


    private val _selectedCallLogsCount = MutableLiveData<Int>()
    val selectedCallLogsCount: LiveData<Int> get() = _selectedCallLogsCount

    // List to keep track of selected dialog items
    private val _selectedDialogsList = mutableListOf<Dialog>()
    val selectedDialogsList: List<Dialog> get() = _selectedDialogsList

    private val _selectedCallLogs = mutableListOf<CallLogEntity>()
    val selectedCallLogs: List<CallLogEntity> get() = _selectedCallLogs

    init {
        // Initialize the selected dialogs count to 0
        _selectedDialogsCount.value = 0
//        _selectedCallLogsCount.value = 0
    }

    // Method to increment the selected dialogs count
    fun incrementSelectedDialogsCount() {
        _selectedDialogsCount.value = (_selectedDialogsCount.value ?: 0) + 1
    }

    fun incrementSelectedCallLogsCount() {
        _selectedCallLogsCount.value = (_selectedCallLogsCount.value ?: 0) + 1
    }

    fun incrementSelectedCallLogs(item: CallLogEntity){
        _selectedCallLogsCount.value = (_selectedCallLogsCount.value ?: 0) + 1
        _selectedCallLogs.add(item)
    }

    fun decrementSelectedCallLogs(item: CallLogEntity){
        _selectedCallLogsCount.value = (_selectedCallLogsCount.value ?: 0) - 1
        _selectedCallLogs.remove(item)
    }

    fun incrementAndAddToSelectedDialogs(item: Dialog) {
        _selectedDialogsCount.value = (_selectedDialogsCount.value ?: 0) + 1
        _selectedDialogsList.add(item)
    }


    // Method to decrement the selected dialogs count and remove the item from the list
    fun decrementAndRemoveFromSelectedDialogs(item: Dialog) {
        val currentCount = _selectedDialogsCount.value ?: 0
        if (currentCount > 0) {
            _selectedDialogsCount.value = currentCount - 1
            _selectedDialogsList.remove(item)
        }
    }

    // Method to decrement the selected dialogs count
    fun decrementSelectedDialogsCount() {
        val currentCount = _selectedDialogsCount.value ?: 0
        if (currentCount > 0) {
            _selectedDialogsCount.value = currentCount - 1
        }
    }

    // Method to reset the selected dialogs count
    fun resetSelectedDialogsCount() {
        _selectedDialogsCount.value = 0
        _selectedCallLogsCount.value = 0
        _selectedCallLogs.clear()
        _selectedDialogsList.clear()
    }
}