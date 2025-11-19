package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uyscuti.social.core.common.data.room.entity.ShortCommentReply

@Dao
interface ShortCommentReplyDao {

    @Query("SELECT * FROM comments_reply_table")
    fun getAllCommentReplies(): LiveData<List<ShortCommentReply>>
//
//    @Query("SELECT * FROM follow_table WHERE userId = :userId")
//    fun getFollowUnFollowById(userId: String): LiveData<FollowUnFollowEntity?>

    @Query("SELECT * FROM comments_reply_table WHERE commentId = :commentId")
    fun getCommentReplyStatus(commentId: String): LiveData<ShortCommentReply?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCommentReply(follow: ShortCommentReply)

    @Query("DELETE FROM comments_reply_table WHERE commentId = :commentId")
    suspend fun deleteCommentReplyById(commentId: String): Int
}