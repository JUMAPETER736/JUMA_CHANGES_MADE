package com.uyscuti.social.circuit.viewmodels.comments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.core.common.data.room.entity.ShortCommentEntity
import com.uyscuti.social.core.common.data.room.repository.ShortCommentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val TAG = "RoomCommentsViewModel"
@HiltViewModel
class RoomCommentsViewModel @Inject constructor(private val repository: ShortCommentsRepository) : ViewModel() {

    val allComments: LiveData<List<ShortCommentEntity>> = repository.allComments

    fun getCommentStatus(postId: String): LiveData<ShortCommentEntity?> {
        return repository.getCommentStatus(postId)
    }

    fun insertComment(comment: ShortCommentEntity) {
        viewModelScope.launch {
//            Log.d(TAG, "Inserting comment: $comment")
            repository.insertComment(comment)
//            Log.d(TAG, "Comm inserted or updated successfully")
        }
    }

    suspend fun deleteCommentById(postId: String): Boolean {
        return repository.deleteCommentById(postId)
    }

}
