package com.uyscuti.social.circuit.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

import com.uyscuti.social.circuit.presentation.OperationResult
import com.uyscuti.social.core.common.data.room.entity.GroupDialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.repository.GroupDialogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class GroupDialogViewModel @Inject constructor(
    private val groupDialogRepository: GroupDialogRepository
) : ViewModel() {

    val allGroupDialogs: LiveData<List<GroupDialogEntity>> = groupDialogRepository.allGroupDialogs
    val allUnreadGroupDialogsCount: LiveData<Int> = groupDialogRepository.allUnreadGroupDialogsCount


    suspend fun insertGroupDialog(dialog: GroupDialogEntity): OperationResult {
        return try {
            groupDialogRepository.insertGroupDialog(dialog)
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error("Insert failed: ${e.message}")
        }
    }

    fun getGroupDialog(dialogId: String): GroupDialogEntity {
        return groupDialogRepository.getDialog(dialogId)
    }

    suspend fun deleteGroups(ids: List<String>){
        groupDialogRepository.deleteGroups(ids)
    }

    suspend fun clearAll(){
        groupDialogRepository.clearAll()
    }


    suspend fun updateGroupDialog(dialog: GroupDialogEntity): OperationResult {
        return try {
            groupDialogRepository.updateDialog(dialog)
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error("Update failed: ${e.message}")
        }
    }


    suspend fun updateLastMessageForThisGroup(
        dialog: String,
        newLastMessage: MessageEntity
    ): OperationResult {
        return try {
            groupDialogRepository.updateLastMessageForThisChat(dialog, newLastMessage)
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error("Update last message failed: ${e.message}")
        }
    }


}