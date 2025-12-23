package com.uyscuti.social.circuit.viewmodels.comments

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.network.api.request.comment.CommentRequestBody
import com.uyscuti.social.network.api.request.comment.GifCommentRequestBody
import com.uyscuti.social.network.api.response.comment.Data
import com.uyscuti.social.network.api.response.comment.ShortCommentResponse
import com.uyscuti.social.network.api.response.comment.allcomments.Comment
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

private const val TAG = "ShortCommentsViewModel"

@HiltViewModel
class ShortCommentsViewModel @Inject constructor(private val retrofitInstance: RetrofitInstance) :
    ViewModel() {

    private val commentsMutableLiveData: MutableLiveData<Data> = MutableLiveData()
    private val replyCommentsMutableLiveData: MutableLiveData<com.uyscuti.social.network.api.response.commentreply.Data> =
        MutableLiveData()
    private val shortCommentsMutableLiveData: MutableLiveData<List<Comment>> = MutableLiveData()

    // ADD: LiveData for immediate comment count updates
    private val _commentCountUpdate = MutableLiveData<CommentCountUpdate>()
    val commentCountUpdate = _commentCountUpdate

    // ADD: LiveData for comment submission status
    private val _commentSubmissionStatus = MutableLiveData<CommentSubmissionStatus>()
    val commentSubmissionStatus = _commentSubmissionStatus

    var commentsMutableList = mutableListOf<Comment>()
    var commentsLiveData: MutableLiveData<List<Comment>> = MutableLiveData()

    var commentsReplyMutableList =
        mutableListOf<com.uyscuti.social.network.api.response.commentreply.allreplies.Comment>()

    // Variable to hold a list of comments
    val commentsList = mutableListOf<Comment>()

    // MutableLiveData to observe changes in the comments list
    val commentMutableLiveData = MutableLiveData<List<Comment>>()

    // ADD: Data classes for UI updates
    data class CommentCountUpdate(
        val postId: String,
        val increment: Int, // +1 for add, -1 for delete
        val localUpdateId: String? = null
    )

    sealed class CommentSubmissionStatus {
        object Submitting : CommentSubmissionStatus()
        data class Success(val data: Data, val postId: String) : CommentSubmissionStatus()
        data class Error(val error: String, val postId: String, val localUpdateId: String) : CommentSubmissionStatus()
    }

    fun commentsObserver(): MutableLiveData<Data> {
        return commentsMutableLiveData
    }

    private suspend fun refreshCommentsForPost(postId: String) {
        // Refresh comments list to ensure UI shows latest data
        // This depends on your existing comment loading logic
    }

    fun shortCommentsObserver(): MutableLiveData<List<Comment>> {
        return shortCommentsMutableLiveData
    }

    fun resetLiveData() {
        commentsLiveData = MutableLiveData()
    }

    fun comment(postId: String, content: String, contentType: String, localUpdateId: String, isFeedComment: Boolean) {
        Log.d(TAG, "comment: inside text comment isFeedComment $isFeedComment")

        // ADD: Immediately update UI count (optimistic update)
        _commentCountUpdate.postValue(CommentCountUpdate(postId, 1, localUpdateId))
        _commentSubmissionStatus.postValue(CommentSubmissionStatus.Submitting)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                lateinit var response: retrofit2.Response<ShortCommentResponse>
                val commentRequestBody = CommentRequestBody(content, contentType, localUpdateId)
                response = if(isFeedComment) {
                    retrofitInstance.apiService.addFeedComment(postId, commentRequestBody)
                }else {
                    retrofitInstance.apiService.addComment(postId, commentRequestBody)
                }

                val responseBody = response.body()
                Log.d(TAG, "comment: response $response")
                Log.d(TAG, "comment: response message ${response.message()}")
                Log.d(TAG, "comment: response message error body ${response.errorBody()}")
                Log.d(TAG, "comment: response body $responseBody")
                Log.d(TAG, "comment: response body data ${responseBody?.data}")
                Log.d(TAG, "comment: response body message ${responseBody!!.message}")

                if (response.isSuccessful && responseBody != null) {
                    val data = responseBody.data
                    Log.d(TAG, "text comment data response: $data")
                    commentsMutableLiveData.postValue(data)
                    _commentSubmissionStatus.postValue(CommentSubmissionStatus.Success(data, postId))
                } else {
                    // ADD: Rollback optimistic update on failure
                    _commentCountUpdate.postValue(CommentCountUpdate(postId, -1, localUpdateId))
                    _commentSubmissionStatus.postValue(
                        CommentSubmissionStatus.Error(
                            response.message() ?: "Unknown error",
                            postId,
                            localUpdateId
                        )
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "comment: $e")
                Log.e(TAG, "comment: ${e.message}")
                e.printStackTrace()

                // ADD: Rollback optimistic update on exception
                _commentCountUpdate.postValue(CommentCountUpdate(postId, -1, localUpdateId))
                _commentSubmissionStatus.postValue(
                    CommentSubmissionStatus.Error(
                        e.message ?: "Network error",
                        postId,
                        localUpdateId
                    )
                )
            }
        }
    }

    fun commentAudio(
        postId: String,
        content: String,
        contentType: String,
        audio: MultipartBody.Part,
        video: MultipartBody.Part,
        thumbnail: MultipartBody.Part,
        gif: MultipartBody.Part,
        docs: MultipartBody.Part,
        image: MultipartBody.Part,
        localUpdateId: String,
        fileType: String,
        fileName: String,
        duration: String,
        isFeedComment: Boolean,
    ) {
        // ADD: Immediate UI update for audio comments too
        _commentCountUpdate.postValue(CommentCountUpdate(postId, 1, localUpdateId))
        _commentSubmissionStatus.postValue(CommentSubmissionStatus.Submitting)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                lateinit var response: retrofit2.Response<ShortCommentResponse>

                Log.d("commentAudio", "file name $fileName, file type $fileType, duration $duration")
                val contentPart: RequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
                val fileTypePart: RequestBody = fileType.toRequestBody("text/plain".toMediaTypeOrNull())
                val fileNamePart: RequestBody = fileName.toRequestBody("text/plain".toMediaTypeOrNull())
                val durationPart: RequestBody = duration.toRequestBody("text/plain".toMediaTypeOrNull())
                val contentTypePart: RequestBody = contentType.toRequestBody("text/plain".toMediaTypeOrNull())
                val localUpdateIdPart: RequestBody = localUpdateId.toRequestBody("text/plain".toMediaTypeOrNull())

                if(isFeedComment) {
                    response = retrofitInstance.apiService.addFeedComment(
                        postId, contentPart, contentTypePart, localUpdateId = localUpdateIdPart,
                        audio = audio, duration = durationPart, fileType = fileTypePart, fileName = fileNamePart
                    )
                }else {
                    response = retrofitInstance.apiService.addComment(
                        postId, contentPart, contentTypePart, localUpdateId = localUpdateIdPart,
                        audio = audio, duration = durationPart, fileType = fileTypePart, fileName = fileNamePart
                    )
                }

                val responseBody = response.body()
                Log.d(TAG, "comment: response $response")

                if (response.isSuccessful && responseBody != null) {
                    val data = responseBody.data
                    Log.d(TAG, "audio comment data response: $data")
                    commentsMutableLiveData.postValue(data)
                    _commentSubmissionStatus.postValue(CommentSubmissionStatus.Success(data, postId))
                } else {
                    _commentCountUpdate.postValue(CommentCountUpdate(postId, -1, localUpdateId))
                    _commentSubmissionStatus.postValue(
                        CommentSubmissionStatus.Error(response.message() ?: "Unknown error", postId, localUpdateId)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "commentAudio: $e")
                _commentCountUpdate.postValue(CommentCountUpdate(postId, -1, localUpdateId))
                _commentSubmissionStatus.postValue(
                    CommentSubmissionStatus.Error(e.message ?: "Network error", postId, localUpdateId)
                )
            }
        }
    }

    fun commentImage(
        postId: String,
        content: String,
        contentType: String,
        audio: MultipartBody.Part,
        video: MultipartBody.Part,
        thumbnail: MultipartBody.Part,
        gif: MultipartBody.Part,
        docs: MultipartBody.Part,
        image: MultipartBody.Part,
        localUpdateId: String,
        isFeedComment: Boolean
    ) {
        val TAG = "commentImage"
        Log.d(TAG, "Inside comment image")

        // ADD: Immediate UI update for image comments
        _commentCountUpdate.postValue(CommentCountUpdate(postId, 1, localUpdateId))
        _commentSubmissionStatus.postValue(CommentSubmissionStatus.Submitting)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Inside try block comment image")

                lateinit var response: retrofit2.Response<ShortCommentResponse>
                val contentPart: RequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
                val contentTypePart: RequestBody = contentType.toRequestBody("text/plain".toMediaTypeOrNull())
                val localUpdateIdPart: RequestBody = localUpdateId.toRequestBody("text/plain".toMediaTypeOrNull())

                if(isFeedComment) {
                    response = retrofitInstance.apiService.addImageFeedComment(
                        postId, contentPart, contentTypePart, image = image, localUpdateId = localUpdateIdPart
                    )
                }else {
                    response = retrofitInstance.apiService.addImageComment(
                        postId, contentPart, contentTypePart, image = image, localUpdateId = localUpdateIdPart
                    )
                }

                val responseBody = response.body()
                Log.d(TAG, "comment: response $response")

                if (response.isSuccessful && responseBody != null) {
                    val data = responseBody.data
                    Log.d(TAG, "image comment data response: $data")
                    commentsMutableLiveData.postValue(data)
                    _commentSubmissionStatus.postValue(CommentSubmissionStatus.Success(data, postId))
                } else {
                    _commentCountUpdate.postValue(CommentCountUpdate(postId, -1, localUpdateId))
                    _commentSubmissionStatus.postValue(
                        CommentSubmissionStatus.Error(response.message() ?: "Unknown error", postId, localUpdateId)
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "commentImage: $e")
                _commentCountUpdate.postValue(CommentCountUpdate(postId, -1, localUpdateId))
                _commentSubmissionStatus.postValue(
                    CommentSubmissionStatus.Error(e.message ?: "Network error", postId, localUpdateId)
                )
            }
        }
    }

    fun addComment(
        postId: String,
        content: String,
        contentType: String,
        audio: MultipartBody.Part,
        video: MultipartBody.Part,
        thumbnail: MultipartBody.Part,
        gifs: String,
        docs: MultipartBody.Part,
        image: MultipartBody.Part,
        localUpdateId: String,
        duration: String,
        fileName: String,
        fileType: String,
        fileSize: String,
        numberOfPages: String,
        isFeedComment: Boolean,
        onSuccess: () -> Unit
    ) {
        val TAG = "addComment"
        Log.d(TAG, "Inside addComment")

        // ADD: Immediate UI update for all comment types
        _commentCountUpdate.postValue(CommentCountUpdate(postId, 1, localUpdateId))
        _commentSubmissionStatus.postValue(CommentSubmissionStatus.Submitting)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Inside try block addComment $gifs")
                lateinit var response: retrofit2.Response<ShortCommentResponse>

                // ... rest of your existing addComment logic ...
                val contentPart: RequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
                val gifsPart: RequestBody = gifs.toRequestBody("text/plain".toMediaTypeOrNull())
                val durationPart: RequestBody = duration.toRequestBody("text/plain".toMediaTypeOrNull())
                val contentTypePart: RequestBody = contentType.toRequestBody("text/plain".toMediaTypeOrNull())
                val localUpdateIdPart: RequestBody = localUpdateId.toRequestBody("text/plain".toMediaTypeOrNull())
                val fileNamePart: RequestBody = fileName.toRequestBody("text/plain".toMediaTypeOrNull())
                val fileSizePart: RequestBody = fileSize.toRequestBody("text/plain".toMediaTypeOrNull())
                val fileTypePart: RequestBody = fileType.toRequestBody("text/plain".toMediaTypeOrNull())
                val numberOfPagesPart: RequestBody = numberOfPages.toRequestBody("text/plain".toMediaTypeOrNull())

                when (contentType) {
                    "video" -> {
                        Log.d(TAG, "Content type is video")
                        response = if(isFeedComment) {
                            retrofitInstance.apiService.addVideoFeedComment(
                                postId, contentPart, contentTypePart, video = video,
                                localUpdateId = localUpdateIdPart, thumbnail = thumbnail, duration = durationPart
                            )
                        } else {
                            retrofitInstance.apiService.addVideoComment(
                                postId, contentPart, contentTypePart, video = video,
                                localUpdateId = localUpdateIdPart, thumbnail = thumbnail, duration = durationPart
                            )
                        }

                        handleCommentResponse(response, postId, localUpdateId, onSuccess)
                    }
                    "docs" -> {
                        Log.d(TAG, "file type $fileType, file name $fileName")
                        response = if(isFeedComment) {
                            retrofitInstance.apiService.addDocumentFeedComment(
                                postId, contentPart, contentTypePart, docs = docs,
                                localUpdateId = localUpdateIdPart, fileName = fileNamePart,
                                fileType = fileTypePart, fileSize = fileSizePart, numberOfPages = numberOfPagesPart
                            )
                        } else {
                            retrofitInstance.apiService.addDocumentComment(
                                postId, contentPart, contentTypePart, docs = docs,
                                localUpdateId = localUpdateIdPart, fileName = fileNamePart,
                                fileType = fileTypePart, fileSize = fileSizePart, numberOfPages = numberOfPagesPart
                            )
                        }

                        handleCommentResponse(response, postId, localUpdateId, onSuccess)
                    }
                    "gif" -> {
                        Log.d(TAG, "Content type is gif type $fileType")
                        val gifCommentRequestBody = GifCommentRequestBody(content, contentType, localUpdateId, gifs)
                        response = if(isFeedComment) {
                            retrofitInstance.apiService.addGifFeedComment(postId, gifCommentRequestBody)
                        } else {
                            retrofitInstance.apiService.addGifComment(postId, gifCommentRequestBody)
                        }

                        handleCommentResponse(response, postId, localUpdateId, onSuccess)
                    }
                    else -> {
                        Log.d(TAG, "Content type is not supported")
                        _commentCountUpdate.postValue(CommentCountUpdate(postId, -1, localUpdateId))
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "addComment: $e")
                _commentCountUpdate.postValue(CommentCountUpdate(postId, -1, localUpdateId))
                _commentSubmissionStatus.postValue(
                    CommentSubmissionStatus.Error(e.message ?: "Network error", postId, localUpdateId)
                )
            }
        }
    }

    // ADD: Helper function to handle comment responses
    private fun handleCommentResponse(
        response: retrofit2.Response<ShortCommentResponse>,
        postId: String,
        localUpdateId: String,
        onSuccess: () -> Unit
    ) {
        val responseBody = response.body()
        Log.d(TAG, "addComment: response $response")

        if (response.isSuccessful && responseBody != null) {
            val data = responseBody.data
            Log.d(TAG, "addComment data response: $data")
            commentsMutableLiveData.postValue(data)
            _commentSubmissionStatus.postValue(CommentSubmissionStatus.Success(data, postId))
            onSuccess()
        } else {
            _commentCountUpdate.postValue(CommentCountUpdate(postId, -1, localUpdateId))
            _commentSubmissionStatus.postValue(
                CommentSubmissionStatus.Error(response.message() ?: "Unknown error", postId, localUpdateId)
            )
        }
    }

    fun allShortComments(postId: String, page: Int) {
        val TAG = "allShortComments"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = retrofitInstance.apiService.getShortComments(postId, page.toString())
                val responseBody = response.body()

                val newComments = responseBody?.data?.comments ?: emptyList()
                Log.d(TAG, "allShortComments: new comments size ${newComments.size}")

                val uniqueCommentsList = newComments.distinctBy { it._id }
                val filteredNewItems = uniqueCommentsList.filter { newItem ->
                    commentsList.none { existingItem -> existingItem._id == newItem._id }
                }

                Log.d(TAG, "allShortComments: new filtered items size ${filteredNewItems.size}")
                commentsList.addAll(filteredNewItems)
                Log.d(TAG, "allShortComments: comments list size ${commentsList.size}")

                commentMutableLiveData.postValue(filteredNewItems)

            } catch (e: Exception) {
                Log.e("UserProfileShortsViewModel", "Exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}