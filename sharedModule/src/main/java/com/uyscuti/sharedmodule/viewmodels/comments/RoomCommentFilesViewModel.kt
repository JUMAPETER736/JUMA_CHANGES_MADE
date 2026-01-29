package com.uyscuti.sharedmodule.viewmodels.comments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.core.common.data.room.entity.CommentsFilesEntity
import com.uyscuti.social.core.common.data.room.repository.CommentFilesRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val TAG = "RoomCommentFilesViewModel"
@HiltViewModel
class RoomCommentFilesViewModel @Inject constructor(private val repository: CommentFilesRepository) : ViewModel() {

    val allCommentFiles: LiveData<List<CommentsFilesEntity>> = repository.allCommentFiles
    val allCommentReplyFiles: LiveData<List<CommentsFilesEntity>> = repository.allCommentReplyFiles

    fun getCommentFilesStatus(postId: String): LiveData<CommentsFilesEntity?> {
        return repository.getCommentFilesStatus(postId)
    }

    fun insertCommentFile(comment: CommentsFilesEntity) {
        viewModelScope.launch {
            Log.d(TAG, "Inserting comment: $comment")
            repository.insertCommentFile(comment)

        }
    }

    suspend fun deleteCommentById(postId: String): Boolean {
        return repository.deleteCommentFileById(postId)
    }

}