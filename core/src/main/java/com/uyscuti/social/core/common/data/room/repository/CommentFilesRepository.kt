package com.uyscuti.social.core.common.data.room.repository

import androidx.lifecycle.LiveData
import com.uyscuti.social.core.common.data.room.dao.CommentFilesDao
import com.uyscuti.social.core.common.data.room.entity.CommentsFilesEntity


private const val TAG = "CommentFilesRepository"
class CommentFilesRepository(private val commentFilesDao: CommentFilesDao) {

    val allCommentFiles: LiveData<List<CommentsFilesEntity>> = commentFilesDao.getAllCommentFiles()
    val allCommentReplyFiles: LiveData<List<CommentsFilesEntity>> = commentFilesDao.getAllCommentReplyFiles()

    fun getCommentFilesStatus(postId: String): LiveData<CommentsFilesEntity?> {
        return commentFilesDao.getCommentFilesStatus(postId)
    }

    suspend fun insertCommentFile(comment: CommentsFilesEntity) {


        commentFilesDao.insertCommentFile(comment)

    }

    suspend fun deleteCommentFileById(postId: String): Boolean {
        val rowsDeleted = commentFilesDao.deleteCommentFileById(postId)
        return rowsDeleted > 0
    }
}
