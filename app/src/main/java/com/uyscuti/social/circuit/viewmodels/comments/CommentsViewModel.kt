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

    // Add this NEW method to fetch ALL comments from BOTH endpoints
    suspend fun fetchAllComments(postId: String, page: Int): List<Comment> {
        Log.d(TAG, "fetchAllComments: Fetching from BOTH endpoints for postId: $postId")

        return withContext(Dispatchers.IO) {
            try {
                val allComments = mutableListOf<Comment>()

                // Fetch shorts comments
                try {
                    val shortsComments = fetchShortComments(postId, page)
                    allComments.addAll(shortsComments)
                    Log.d(TAG, "fetchAllComments: Got ${shortsComments.size} shorts comments")
                } catch (e: Exception) {
                    Log.e(TAG, "fetchAllComments: Error fetching shorts comments", e)
                }

                // Fetch feed comments
                try {
                    val feedComments = fetchFeedComments(postId, page)
                    allComments.addAll(feedComments)
                    Log.d(TAG, "fetchAllComments: Got ${feedComments.size} feed comments")
                } catch (e: Exception) {
                    Log.e(TAG, "fetchAllComments: Error fetching feed comments", e)
                }

                // Remove duplicates based on comment ID
                val uniqueComments = allComments.distinctBy { it._id }

                // Sort by creation date (newest first)
                val sortedComments = uniqueComments.sortedByDescending { it.createdAt }

                Log.d(TAG, "fetchAllComments: Total unique comments: ${sortedComments.size}")

                sortedComments

            } catch (e: Exception) {
                Log.e(TAG, "fetchAllComments: Fatal error", e)
                emptyList()
            }
        }
    }


}