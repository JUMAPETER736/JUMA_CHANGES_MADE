package com.uyscuti.social.core.common.data.room.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.uyscuti.social.core.common.data.room.dao.ShortCommentsDao
import com.uyscuti.social.core.common.data.room.entity.ShortCommentEntity

private const val TAG = "ShortCommentsRepository"
class ShortCommentsRepository(private val commentsDao: ShortCommentsDao) {

    val allComments: LiveData<List<ShortCommentEntity>> = commentsDao.getAllComments()

    fun getCommentStatus(postId: String): LiveData<ShortCommentEntity?> {
        return commentsDao.getCommentStatus(postId)
    }

    suspend fun insertComment(comment: ShortCommentEntity) {
        Log.d(TAG, "Inserting or updating follow: $comment")

        commentsDao.insertComment(comment)

        Log.d(TAG, "Follow inserted or updated successfully")

    }

    //    suspend fun deleteFollowById(userId: String) {
//        followDao.deleteFollowById(userId)
//    }
    suspend fun deleteCommentById(postId: String): Boolean {
        val rowsDeleted = commentsDao.deleteCommentById(postId)
        return rowsDeleted > 0
    }
}