package com.uyscuti.sharedmodule.viewmodels.comments

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


    var commentsMutableList = mutableListOf<Comment>()
    var commentsLiveData: MutableLiveData<List<Comment>> = MutableLiveData()

    var commentsReplyMutableList =
        mutableListOf<com.uyscuti.social.network.api.response.commentreply.allreplies.Comment>()

    // Variable to hold a list of comments
    val commentsList = mutableListOf<Comment>()

    // MutableLiveData to observe changes in the comments list
    val commentMutableLiveData = MutableLiveData<List<Comment>>()


    fun commentsObserver(): MutableLiveData<Data> {
        return commentsMutableLiveData
    }

    fun shortCommentsObserver(): MutableLiveData<List<Comment>> {
        return shortCommentsMutableLiveData
    }

    fun resetLiveData() {
        commentsLiveData = MutableLiveData()
    }

    fun comment(postId: String, content: String, contentType: String, localUpdateId: String, isFeedComment: Boolean) {
        Log.d(TAG, "comment: inside text comment isFeedComment $isFeedComment")
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

                val data = responseBody.data

                Log.d(TAG, "text comment data response: $data")
                commentsMutableLiveData.postValue(data)

            } catch (e: Exception) {
                Log.e(TAG, "comment: $e")
                Log.e(TAG, "comment: ${e.message}")
                e.printStackTrace()
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
//        onSuccess: (com.uyscut.network.api.response.comment.Data) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                lateinit var response: retrofit2.Response<ShortCommentResponse>

                Log.d(
                    "commentAudio",
                    "file name $fileName, file type $fileType, duration $duration"
                )
                val contentPart: RequestBody = content
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val fileTypePart: RequestBody = fileType
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val fileNamePart: RequestBody = fileName
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val durationPart: RequestBody = duration
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val contentTypePart: RequestBody = contentType
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val localUpdateIdPart: RequestBody = localUpdateId
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                if(isFeedComment) {
                    response = retrofitInstance.apiService. addFeedComment(
                        postId,
                        contentPart,
                        contentTypePart,
                        localUpdateId = localUpdateIdPart,
                        audio = audio,
                        duration = durationPart,
                        fileType = fileTypePart,
                        fileName = fileNamePart
                    )
                }else {
                    response = retrofitInstance.apiService.addComment(
                        postId,
                        contentPart,
                        contentTypePart,
                        localUpdateId = localUpdateIdPart,
                        audio = audio,
                        duration = durationPart,
                        fileType = fileTypePart,
                        fileName = fileNamePart
                    )
                }

                val responseBody = response.body()
                Log.d(TAG, "comment: response $response")
                Log.d(TAG, "comment: response message ${response.message()}")
                Log.d(TAG, "comment: response message error body ${response.errorBody()}")
                Log.d(TAG, "comment: response body $responseBody")
                Log.d(TAG, "comment: response body data ${responseBody?.data}")
                Log.d(TAG, "comment: response body message ${responseBody!!.message}")
                val data = responseBody.data

                Log.d(TAG, "audio comment data response: $data")
                commentsMutableLiveData.postValue(data)
//                onSuccess(data)
            } catch (e: Exception) {
                Log.e(TAG, "commentAudio: $e")
                Log.e(TAG, "commentAudio: ${e.message}")
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Inside try block comment image")

                lateinit var response: retrofit2.Response<ShortCommentResponse>
                val contentPart: RequestBody = content
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val contentTypePart: RequestBody = contentType
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val localUpdateIdPart: RequestBody = localUpdateId
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                if(isFeedComment) {
                    response = retrofitInstance.apiService.addImageFeedComment(
                        postId,
                        contentPart,
                        contentTypePart,
                        image = image,
                        localUpdateId = localUpdateIdPart
                    )
                }else {
                    response = retrofitInstance.apiService.addImageComment(
                        postId,
                        contentPart,
                        contentTypePart,
                        image = image,
                        localUpdateId = localUpdateIdPart
                    )
                }

                val responseBody = response.body()
                Log.d(TAG, "comment: response $response")
                Log.d(TAG, "comment: response message ${response.message()}")
                Log.d(TAG, "comment: response message error body ${response.errorBody()}")
                Log.d(TAG, "comment: response body $responseBody")
                Log.d(TAG, "comment: response body data ${responseBody?.data}")
                Log.d(TAG, "comment: response body message ${responseBody!!.message}")
                val data = responseBody.data

                Log.d(TAG, "image comment data response: $data")
                commentsMutableLiveData.postValue(data)

            } catch (e: Exception) {
                Log.e(TAG, "commentImage: $e")
                Log.e(TAG, "commentImage: ${e.message}")
                e.printStackTrace()
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Inside try block addComment $gifs")
                lateinit var response: retrofit2.Response<ShortCommentResponse>
                val contentPart: RequestBody = content
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val gifsPart: RequestBody = gifs
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                val durationPart: RequestBody = duration
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val contentTypePart: RequestBody = contentType
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                val localUpdateIdPart: RequestBody = localUpdateId
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                val fileNamePart: RequestBody = fileName
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val fileSizePart: RequestBody = fileSize
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val fileTypePart: RequestBody = fileType
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val numberOfPagesPart: RequestBody = numberOfPages
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                when (contentType) {
                    "video" -> {
                        Log.d(TAG, "Content type is video")

                        if(isFeedComment) {
                            response = retrofitInstance.apiService.addVideoFeedComment(
                                postId,
                                contentPart,
                                contentTypePart,
                                video = video,
                                localUpdateId = localUpdateIdPart,
                                thumbnail = thumbnail,
                                duration = durationPart
                            )
                        }else {
                            response = retrofitInstance.apiService.addVideoComment(
                                postId,
                                contentPart,
                                contentTypePart,
                                video = video,
                                localUpdateId = localUpdateIdPart,
                                thumbnail = thumbnail,
                                duration = durationPart
                            )
                        }

                        val responseBody = response.body()
                        Log.d(TAG, "addComment: response $response")
                        Log.d(TAG, "addComment: response message ${response.message()}")
                        Log.d(
                            TAG,
                            "addComment: response message error body ${response.errorBody()}"
                        )
                        Log.d(TAG, "addComment: response body $responseBody")
                        Log.d(TAG, "addComment: response body data ${responseBody?.data}")
                        Log.d(TAG, "addComment: response body message ${responseBody!!.message}")
                        val data = responseBody.data

                        Log.d(TAG, " addComment data response: $data")
                        commentsMutableLiveData.postValue(data)
                        onSuccess()
                    }

                    "docs" -> {

                        Log.d(TAG, "file type $fileType, file name $fileName")
                        if(isFeedComment) {
                            response = retrofitInstance.apiService.addDocumentFeedComment(
                                postId,
                                contentPart,
                                contentTypePart,
                                docs = docs,
                                localUpdateId = localUpdateIdPart,
                                fileName = fileNamePart,
                                fileType = fileTypePart,
                                fileSize = fileSizePart,
                                numberOfPages = numberOfPagesPart
                            )
                        }else {
                            response = retrofitInstance.apiService.addDocumentComment(
                                postId,
                                contentPart,
                                contentTypePart,
                                docs = docs,
                                localUpdateId = localUpdateIdPart,
                                fileName = fileNamePart,
                                fileType = fileTypePart,
                                fileSize = fileSizePart,
                                numberOfPages = numberOfPagesPart
                            )
                        }

                        val responseBody = response.body()
                        Log.d(TAG, "addComment: response $response")
                        Log.d(TAG, "addComment: response message ${response.message()}")
                        Log.d(
                            TAG,
                            "addComment: response message error body ${response.errorBody()}"
                        )
                        Log.d(TAG, "addComment: response body $responseBody")
                        Log.d(TAG, "addComment: response body data ${responseBody?.data}")
                        Log.d(TAG, "addComment: response body message ${responseBody!!.message}")
                        val data = responseBody.data

                        Log.d(TAG, " addComment data response: $data")
                        commentsMutableLiveData.postValue(data)
                        Log.d(TAG, "Content type is document - ready to make an upload")
                    }

                    "gif" -> {
                        Log.d(TAG, "Content type is  gif type $fileType")

                        val gifCommentRequestBody =
                            GifCommentRequestBody(content, contentType, localUpdateId, gifs)
//                        val response = retrofitInstance.apiService.addComment(postId, commentRequestBody)

                        response = if(isFeedComment) {
                            retrofitInstance.apiService.addGifFeedComment(
                                postId,
                                gifCommentRequestBody
                            )
                        }else {
                            retrofitInstance.apiService.addGifComment(
                                postId,
                                gifCommentRequestBody
                            )
                        }

                        val responseBody = response.body()
                        Log.d(TAG, "addComment: response $response")
                        Log.d(TAG, "addComment: response message ${response.message()}")
                        Log.d(
                            TAG,
                            "addComment: response message error body ${response.errorBody()}"
                        )
                        Log.d(TAG, "addComment: response body $responseBody")
                        Log.d(TAG, "addComment: response body data ${responseBody?.data}")
                        Log.d(TAG, "addComment: response body message ${responseBody!!.message}")
                        val data = responseBody.data

                        Log.d(TAG, " addComment data response: $data")
                        commentsMutableLiveData.postValue(data)
//                        Log.d(TAG, "Content type is document - ready to make an upload")

                    }

                    else -> {
                        Log.d(TAG, "Content type is not video")
                    }
                }


            } catch (e: Exception) {
                Log.e(TAG, "addComment: $e")
                Log.e(TAG, "addComment: ${e.message}")
                e.printStackTrace()
            }
        }

    }


    fun allShortComments(postId: String, page: Int) {


        val TAG = "allShortComments"
        viewModelScope.launch(Dispatchers.IO) {

            try {
                val response =
                    retrofitInstance.apiService.getShortComments(postId, page.toString())
                val responseBody = response.body()
//                val shortsEntity = responseBody?.data?.bookmarkedPosts?.let { serverResponseToUserEntity(it) }

                val newComments = responseBody?.data?.comments ?: emptyList()

                Log.d(TAG, "allShortComments: new comments size ${newComments.size}")
                val uniqueCommentsList = newComments.distinctBy { it._id }

                val filteredNewItems = uniqueCommentsList.filter { newItem ->
                    commentsList.none { existingItem -> existingItem._id == newItem._id }
                }

                Log.d(TAG, "allShortComments: new filtered items size ${filteredNewItems.size}")

                // Add new comments to the existing list
                commentsList.addAll(filteredNewItems)
                Log.d(TAG, "allShortComments: comments list size ${commentsList.size}")

                // Update the LiveData with the updated comments list
                commentMutableLiveData.postValue(filteredNewItems)
//                val comments = responseBody!!.data.comments
//
//                val uniqueCommentsList = comments.distinctBy { it._id }
//
//                shortCommentsMutableLiveData.postValue(uniqueCommentsList)
//
//                withContext(Dispatchers.Main) {
//                }

            } catch (e: Exception) {
                Log.e("UserProfileShortsViewModel", "Exception: ${e.message}")
                e.printStackTrace()
            }

        }
    }


}
