package com.uyscuti.social.circuit.viewmodels.comments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.core.common.data.room.entity.ShortCommentReply
import com.uyscuti.social.core.common.data.room.repository.ShortCommentReplyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "RoomCommentReplyViewModel"
@HiltViewModel
class RoomCommentReplyViewModel @Inject constructor(private val repository: ShortCommentReplyRepository) : ViewModel() {

    val allCommentReplies: LiveData<List<ShortCommentReply>> = repository.allCommentReplies

    fun getCommentReplyStatus(postId: String): LiveData<ShortCommentReply?> {
        return repository.getCommentReplyStatus(postId)
    }

    fun insertCommentReply(comment: ShortCommentReply) {
        viewModelScope.launch {
//            Log.d(TAG, "Inserting comment: $comment")
            repository.insertCommentReply(comment)
//            Log.d(TAG, "Comm inserted or updated successfully")
        }
    }

    suspend fun deleteCommentReplyById(postId: String): Boolean {
        return repository.deleteCommentReplyById(postId)
    }

}
