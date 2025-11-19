package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uyscuti.social.core.common.data.room.entity.ShortCommentEntity

@Dao
interface ShortCommentsDao {

    @Query("SELECT * FROM comments_table")
    fun getAllComments(): LiveData<List<ShortCommentEntity>>
//
//    @Query("SELECT * FROM follow_table WHERE userId = :userId")
//    fun getFollowUnFollowById(userId: String): LiveData<FollowUnFollowEntity?>

    @Query("SELECT * FROM comments_table WHERE postId = :postId")
    fun getCommentStatus(postId: String): LiveData<ShortCommentEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertComment(follow: ShortCommentEntity)

    @Query("DELETE FROM comments_table WHERE postId = :postId")
    suspend fun deleteCommentById(postId: String): Int
}