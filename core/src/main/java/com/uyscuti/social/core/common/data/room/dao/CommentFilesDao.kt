package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uyscuti.social.core.common.data.room.entity.CommentsFilesEntity

@Dao
interface CommentFilesDao {

    @Query("SELECT * FROM comment_files_table WHERE isReply = 0")
    fun getAllCommentFiles(): LiveData<List<CommentsFilesEntity>>

    @Query("SELECT * FROM comment_files_table WHERE isReply = 1")
    fun getAllCommentReplyFiles(): LiveData<List<CommentsFilesEntity>>
//
//    @Query("SELECT * FROM follow_table WHERE userId = :userId")
//    fun getFollowUnFollowById(userId: String): LiveData<FollowUnFollowEntity?>

    @Query("SELECT * FROM comment_files_table WHERE id = :postId")
    fun getCommentFilesStatus(postId: String): LiveData<CommentsFilesEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCommentFile(follow: CommentsFilesEntity)

    @Query("DELETE FROM comment_files_table WHERE id = :postId")
    suspend fun deleteCommentFileById(postId: String): Int
}