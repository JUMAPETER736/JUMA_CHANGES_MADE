package com.uyscuti.social.core.common.data.room.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.uyscuti.social.core.common.data.room.dao.ShortCommentReplyDao
import com.uyscuti.social.core.common.data.room.entity.ShortCommentReply

private const val TAG = "ShortCommentReplyRepository"
class ShortCommentReplyRepository(private val commentReplyDao: ShortCommentReplyDao) {

    val allCommentReplies: LiveData<List<ShortCommentReply>> = commentReplyDao.getAllCommentReplies()

    fun getCommentReplyStatus(postId: String): LiveData<ShortCommentReply?> {
        return commentReplyDao.getCommentReplyStatus(postId)
    }

    suspend fun insertCommentReply(comment: ShortCommentReply) {
        Log.d(TAG, "Inserting or updating follow: $comment")

        commentReplyDao.insertCommentReply(comment)

        Log.d(TAG, "Follow inserted or updated successfully")

    }

    //    suspend fun deleteFollowById(userId: String) {
//        followDao.deleteFollowById(userId)
//    }
    suspend fun deleteCommentReplyById(commentReplyId: String): Boolean {
        val rowsDeleted = commentReplyDao.deleteCommentReplyById(commentReplyId)
        return rowsDeleted > 0
    }
}