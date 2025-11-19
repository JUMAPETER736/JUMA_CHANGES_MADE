package com.uyscuti.social.circuit.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.repository.DialogRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OperationResult {
    object Success : OperationResult()
    data class Error(val message: String) : OperationResult()
}


@HiltViewModel
class DialogViewModel @Inject constructor(
    private val dialogRepository: DialogRepository
) : ViewModel() {

    fun setDialogRepository(repository: DialogRepository) {

    }

    // Define a MutableLiveData for the integer value
    private val _intValue = MutableLiveData<Int>()

    // Expose an immutable LiveData for observing
    val intValue: LiveData<Int>
        get() = _intValue

    // Function to update the integer value
    fun updateIntValue(newValue: Int) {
        _intValue.value = newValue
    }

    // Define a MutableLiveData for the integer value
    private val _idValue = MutableLiveData<String>()

    // Expose an immutable LiveData for observing
    val idValue: LiveData<String>
        get() = _idValue

    // Function to update the integer value
    fun updateIdValue(newValue: String) {
        _idValue.value = newValue
    }

    val allDialogs: LiveData<List<DialogEntity>> = dialogRepository.allDialogs

    val allPersonalChats: LiveData<List<DialogEntity>> = dialogRepository.allPersonal

    val allGroupDialogs: LiveData<List<DialogEntity>> = dialogRepository.allGroupDialogs

    private val _allDialogs = MutableLiveData<List<DialogEntity>>()
    val allLiveDialogs: LiveData<List<DialogEntity>> = _allDialogs

    val allUnreadDialogsCount: LiveData<Int> = dialogRepository.allUnreadDialogs




    private val _updatedDialog = MutableLiveData<DialogEntity>()

    val updatedDialog: LiveData<DialogEntity>
        get() = _updatedDialog

    fun updateDialogEntityId(oldDialogId: String, newChatId: String) {
        viewModelScope.launch {
            dialogRepository.updateDialogEntityId(oldDialogId, newChatId)

            // Retrieve and emit the updated dialog after the transaction is completed
            val updatedDialog = dialogRepository.getDialog(newChatId)
            _updatedDialog.value = updatedDialog
        }
    }



    suspend fun getTempDialogs(): LiveData<List<DialogEntity>>{
        return dialogRepository.getTempDialogs()
    }



    suspend fun getDialogsFlow(): Flow<List<DialogEntity>> {
        return dialogRepository.getDialogsFlow()
    }
    suspend fun clearAll(){
        dialogRepository.clearAll()
    }
    suspend fun deleteDialogs(ids: List<String>){
        dialogRepository.deleteDialogs(ids)
    }

    suspend fun insertDialog(dialog: DialogEntity): OperationResult {
        return try {
            dialogRepository.insertDialog(dialog)
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error("Insert failed: ${e.message}")
        }
    }

    fun getDialogByID(dialogId: String): LiveData<DialogEntity?> {
        return dialogRepository.getDialogByID(dialogId)
    }

    fun getDialogByName(name: String): LiveData<DialogEntity?>{
        return dialogRepository.getDialogByName(name)
    }

    suspend fun getDialog(dialogId: String): DialogEntity {
        return dialogRepository.getDialog(dialogId)
    }

    fun getDialogByIDFlow(dialogId: String): Flow<DialogEntity?> {
        return dialogRepository.getDialogByIDFlow(dialogId)
    }

    suspend fun observeThisDialog(name: String): LiveData<DialogEntity>{
        return  dialogRepository.observeThisDialog(name)
    }

    suspend fun replaceDialog(old: DialogEntity, new: DialogEntity){
        dialogRepository.replaceDialog(old, new)
    }

    suspend fun updateDialog(dialog: DialogEntity): OperationResult {
        return try {
            dialogRepository.updateDialog(dialog)
            OperationResult.Success
        } catch (e: Exception) {
            Log.e("DialogUpdate", "Failure : ${e.message}")
            e.printStackTrace()
            OperationResult.Error("Update failed: ${e.message}")
        }
    }

    suspend fun getDialogsList(): List<DialogEntity> {
        return dialogRepository.getDialogList()
    }

    suspend fun incrementUnreadCount(dialogId: String): OperationResult {
        return try {
            dialogRepository.incrementUnreadCount(dialogId)
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error("Increment unread count failed: ${e.message}")
        }
    }

    suspend fun updateLastMessage(
        dialog: DialogEntity,
        newLastMessage: MessageEntity
    ): OperationResult {
        return try {
            dialogRepository.updateLastMessage(dialog, newLastMessage)
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error("Update last message failed: ${e.message}")
        }
    }

    suspend fun updateLastMessageForThisChat(
        dialog: String,
        newLastMessage: MessageEntity
    ): OperationResult {
        return try {
            dialogRepository.updateLastMessageForThisChat(dialog, newLastMessage)
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error("Update last message failed: ${e.message}")
        }
    }

    suspend fun setNullLastMessage(dialog: String) {
        try {
            dialogRepository.setLastMessageNull(dialog)
        } catch (e:Exception){
            e.printStackTrace()
        }
    }


    suspend fun resetUnreadCount(dialogId: String): OperationResult {
        return try {
            dialogRepository.resetUnreadCount(dialogId)
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error("Reset unread count failed: ${e.message}")
        }
    }
}