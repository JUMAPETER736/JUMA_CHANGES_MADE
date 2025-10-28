package com.uyscuti.social.circuit.viewmodels.comments

import android.util.Log
import androidx.lifecycle.ViewModel
import com.uyscuti.social.circuit.data.model.Comment
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


private const val TAG = "CommentsViewModel"
@HiltViewModel
class CommentsViewModel @Inject constructor(private val retrofitInstance: RetrofitInstance) : ViewModel() {
    suspend fun fetchShortComments(postId: String, page: Int): List<Comment> {
        Log.d(TAG, "fetchShortComments: inside")
        return withContext(Dispatchers.IO) {
            try {
                // Fetch comments from the API
                val response = retrofitInstance.apiService.getShortComments(postId, page.toString())
                val responseBody = response.body()
                Log.d(TAG, "fetchShortComments: response success?: ${responseBody?.success}")

                val comments = responseBody?.data?.comments
                val commentsWithReplies = comments!!.map { firstComment ->
                    Comment(
                        __v = firstComment.__v,
                        _id = firstComment._id,
                        author = firstComment.author,
                        content = firstComment.content,
                        createdAt = firstComment.createdAt,
                        isLiked = firstComment.isLiked,
                        likes = firstComment.likes,
                        postId = firstComment.postId,
                        updatedAt = firstComment.updatedAt,
                        replyCount = firstComment.replyCount,
                        replies = mutableListOf(),
                        images = firstComment.images,
                        audios = firstComment.audios,
                        docs = firstComment.docs,
                        gifs = firstComment.gifs ?: "",
                        thumbnail = firstComment.thumbnail,
                        videos = firstComment.videos,
                        contentType = firstComment.contentType,
                        localUpdateId = "",
                        duration = firstComment.duration ?: "00:00",
                        fileName = firstComment.fileName ?: "unknown",
                        fileSize = firstComment.fileSize ?: "unknown",
                        fileType = firstComment.fileType ?: "unknown",
                        numberOfPages = firstComment.numberOfPages ?: "0"
                    )
                }

//                responseBody?.data?.comments ?: emptyList()
                commentsWithReplies
            } catch (e: Exception) {
                // Handle errors
                Log.d(TAG, "fetchShortComments: error ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }
    suspend fun fetchFeedComments(postId: String, page: Int): List<Comment> {
        Log.d(TAG, "fetchFeedComments: inside")
        return withContext(Dispatchers.IO) {
            try {
                // Fetch comments from the API
                val response = retrofitInstance.apiService.getFeedComments(postId, page.toString())
                val responseBody = response.body()
                Log.d(TAG, "fetchFeedComments: response success totalComments?: ${responseBody?.data?.totalComments}")

                val comments = responseBody?.data?.comments
                val commentsWithReplies = comments!!.map { firstComment ->
                    Comment(
                        __v = firstComment.__v,
                        _id = firstComment._id,
                        author = firstComment.author,
                        content = firstComment.content,
                        createdAt = firstComment.createdAt,
                        isLiked = firstComment.isLiked,
                        likes = firstComment.likes,
                        postId = firstComment.postId,
                        updatedAt = firstComment.updatedAt,
                        replyCount = firstComment.replyCount,
                        replies = mutableListOf(),
                        images = firstComment.images,
                        audios = firstComment.audios,
                        docs = firstComment.docs,
                        gifs = firstComment.gifs ?: "",
                        thumbnail = firstComment.thumbnail,
                        videos = firstComment.videos,
                        contentType = firstComment.contentType,
                        localUpdateId = "",
                        duration = firstComment.duration ?: "00:00",
                        fileName = firstComment.fileName ?: "unknown",
                        fileSize = firstComment.fileSize ?: "unknown",
                        fileType = firstComment.fileType ?: "unknown",
                        numberOfPages = firstComment.numberOfPages ?: "0"
                    )
                }

//                responseBody?.data?.comments ?: emptyList()
                commentsWithReplies
            } catch (e: Exception) {
                // Handle errors
                Log.d(TAG, "fetchShortComments: error ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }



}