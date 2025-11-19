package com.uyscuti.social.circuit.viewmodels.comments

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.network.api.request.comment.CommentRequestBody
import com.uyscuti.social.network.api.request.comment.GifCommentRequestBody
import com.uyscuti.social.network.api.response.commentreply.CommentReplyResponse
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject

private const val TAG = "ShortCommentReplyViewModel"

@HiltViewModel
class ShortCommentReplyViewModel @Inject constructor(private val retrofitInstance: RetrofitInstance) :
    ViewModel() {
    private val replyCommentsMutableLiveData: MutableLiveData<com.uyscuti.social.network.api.response.commentreply.Data> =
        MutableLiveData()
    var commentsReplyMutableList =
        mutableListOf<com.uyscuti.social.network.api.response.commentreply.allreplies.Comment>()

    fun commentReply(commentId: String, content: String, localUpdateId: String, isFeedCommentReply: Boolean) {
        Log.d("addCommentReply", "commentReply: Comment reply id $commentId and content $content")
        viewModelScope.launch(Dispatchers.IO) {
            try {

                lateinit var response: Response<CommentReplyResponse>
                Log.d("addCommentReply", "commentReply: inside try catch")
                val commentRequestBody = CommentRequestBody(content, "text", localUpdateId)

                response = if (isFeedCommentReply) {
                    retrofitInstance.apiService.addFeedCommentReply(commentId, commentRequestBody)
                }else {
                    retrofitInstance.apiService.addCommentReply(commentId, commentRequestBody)
                }

                Log.d(TAG, "commentReply: response ${response.body()}")
                Log.d(TAG, "commentReply: response ${response.message()}")
                Log.d(TAG, "commentReply: response ${response.errorBody()}")
                val responseBody = response.body()
                Log.d("addCommentReply", "commentReply: response body $responseBody")
                Log.d("addCommentReply", "commentReply: response body ${responseBody?.message}")
//                Log.d(TAG, "commentReply: response body ${responseBody.data.}")
                if (responseBody != null) {
                    val data = responseBody.data

                    Log.d("addCommentReply", "reply comment: $data")
                    replyCommentsMutableLiveData.postValue(data)

                } else {
                    Log.d("addCommentReply", "commentReply: response body is null")
                }
            } catch (e: Exception) {

                e.printStackTrace()
                Log.e(TAG, "commentReply: error:${e.message}")
                Log.e(TAG, "commentReply: error:${e.printStackTrace()}")
            }
        }

    }

    fun getReplyCommentsLiveData(): MutableLiveData<com.uyscuti.social.network.api.response.commentreply.Data> {
        return replyCommentsMutableLiveData
    }

    fun commentReply(
        commentId: String,
        content: String,
        contentType: String,
        audio: MultipartBody.Part,
        video: MultipartBody.Part,
        thumbnail: MultipartBody.Part,
        gif: String,
        docs: MultipartBody.Part,
        image: MultipartBody.Part,
        localUpdateId: String,
        duration: String,
        fileName: String,
        fileType: String,
        fileSize: String,
        numberOfPages: String,
        isFeedCommentReply: Boolean,
        onSuccess: (com.uyscuti.social.network.api.response.commentreply.Data) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                lateinit var response: Response<CommentReplyResponse>
                val contentPart: RequestBody = content
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val contentTypePart: RequestBody = contentType
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                val durationTypePart: RequestBody = duration
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val localUpdateIdPart: RequestBody = localUpdateId
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val fileNamePart: RequestBody = fileName
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val fileTypePart: RequestBody = fileType
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val fileSizePart: RequestBody = fileSize
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val numberOfPagesPart: RequestBody = numberOfPages
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                when (contentType) {
                    "image" -> {
                        Log.d("commentReplyAudio", "content type is image")
                        if(isFeedCommentReply) {
                            response = retrofitInstance.apiService.addFeedImageCommentReply(
                                commentId,
                                contentPart,
                                contentTypePart,
                                image = image,
                                localUpdateId = localUpdateIdPart
                            )
                        }else {
                            response = retrofitInstance.apiService.addImageCommentReply(
                                commentId,
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

                        Log.d(TAG, "audio comment data response: $data")
                        replyCommentsMutableLiveData.postValue(data)
                    }

                    "video" -> {
                        Log.d("commentReplyVideo", "content type is video ")
                        if(isFeedCommentReply) {
                            response = retrofitInstance.apiService.addFeedVideoCommentReply(
                                commentId,
                                contentPart,
                                contentTypePart,
                                video = video,
                                localUpdateId = localUpdateIdPart,
                                thumbnail = thumbnail,
                                duration = durationTypePart
                            )
                        }else {
                            response = retrofitInstance.apiService.addVideoCommentReply(
                                commentId,
                                contentPart,
                                contentTypePart,
                                video = video,
                                localUpdateId = localUpdateIdPart,
                                thumbnail = thumbnail,
                                duration = durationTypePart
                            )
                        }

                        val responseBody = response.body()
                        Log.d("commentReplyVideo", "comment: response $response")
                        Log.d(
                            "commentReplyVideo",
                            "comment: response message ${response.message()}"
                        )
                        Log.d(
                            "commentReplyVideo",
                            "comment: response message error body ${response.errorBody()}"
                        )
                        Log.d("commentReplyVideo", "comment: response body $responseBody")
                        Log.d(
                            "commentReplyVideo",
                            "comment: response body data ${responseBody?.data}"
                        )
                        Log.d(
                            "commentReplyVideo",
                            "comment: response body message ${responseBody!!.message}"
                        )
                        val data = responseBody.data

                        Log.d(TAG, "audio comment data response: $data")
                        replyCommentsMutableLiveData.postValue(data)
                        onSuccess(data)
                    }

                    "docs" -> {
                        Log.d(TAG, "reply docs are ready to be sent to server - db")
                        Log.d(
                            TAG,
                            "reply docs details size $fileSize, name $fileName, type $fileType, num of pages $numberOfPages"
                        )

                        if(isFeedCommentReply) {
                            response = retrofitInstance.apiService.addFeedDocumentCommentReply(
                                commentId,
                                contentPart,
                                contentTypePart,
                                docs = docs,
                                localUpdateId = localUpdateIdPart,
                                fileType = fileTypePart,
                                fileSize = fileSizePart,
                                fileName = fileNamePart,
                                numberOfPages = numberOfPagesPart
                            )
                        }else {
                            response = retrofitInstance.apiService.addDocumentCommentReply(
                                commentId,
                                contentPart,
                                contentTypePart,
                                docs = docs,
                                localUpdateId = localUpdateIdPart,
                                fileType = fileTypePart,
                                fileSize = fileSizePart,
                                fileName = fileNamePart,
                                numberOfPages = numberOfPagesPart
                            )
                        }


                        val responseBody = response.body()
                        Log.d("commentReplyVideo", "comment: response $response")
                        Log.d(
                            "commentReplyVideo",
                            "comment: response message ${response.message()}"
                        )
                        Log.d(
                            "commentReplyVideo",
                            "comment: response message error body ${response.errorBody()}"
                        )
                        Log.d("commentReplyVideo", "comment: response body $responseBody")
                        Log.d(
                            "commentReplyVideo",
                            "comment: response body data ${responseBody?.data}"
                        )
                        Log.d(
                            "commentReplyVideo",
                            "comment: response body message ${responseBody!!.message}"
                        )
                        val data = responseBody.data

                        Log.d(TAG, "audio comment data response: $data")
                        replyCommentsMutableLiveData.postValue(data)
                    }
                    "gif" -> {
                        Log.d("commentReplyGif", "is feed comment reply $isFeedCommentReply")
                        val gifCommentRequestBody = GifCommentRequestBody(
                            content = content, contentType = contentType, localUpdateId = localUpdateId,
                            gifs = gif
                        )
                        response = if(isFeedCommentReply) {
                            retrofitInstance.apiService.addFeedGifCommentReply(
                                commentId,
                                gifCommentRequestBody
                            )
                        }else {
                            retrofitInstance.apiService.addGifCommentReply(
                                commentId,
                                gifCommentRequestBody
                            )
                        }

                        val responseBody = response.body()
                        Log.d("commentReplyGif", "comment: response $response")
                        Log.d(
                            "commentReplyGif",
                            "comment: response message ${response.message()}"
                        )
                        Log.d(
                            "commentReplyGif",
                            "comment: response message error body ${response.errorBody()}"
                        )
                        Log.d("commentReplyGif", "comment: response body $responseBody")
                        Log.d(
                            "commentReplyGif",
                            "comment: response body data ${responseBody?.data}"
                        )
                        Log.d(
                            "commentReplyGif",
                            "comment: response body message ${responseBody!!.message}"
                        )
                        val data = responseBody.data

                        Log.d("commentReplyGif", "gif comment data response: $data")
                        replyCommentsMutableLiveData.postValue(data)
                    }
                    else -> {
                        Log.d("commentReplyAudio", "content type is audio")

                        if(isFeedCommentReply) {
                            response = retrofitInstance.apiService.addFeedCommentReply(
                                commentId,
                                contentPart,
                                contentTypePart,
                                audio = audio,
                                localUpdateId = localUpdateIdPart,
                                fileType = fileTypePart,
                                fileName = fileNamePart,
                                duration = durationTypePart
                            )
                        }else {
                            response = retrofitInstance.apiService.addCommentReply(
                                commentId,
                                contentPart,
                                contentTypePart,
                                audio = audio,
                                localUpdateId = localUpdateIdPart,
                                fileType = fileTypePart,
                                fileName = fileNamePart,
                                duration = durationTypePart
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
                        replyCommentsMutableLiveData.postValue(data)
                        onSuccess(data)
                    }
                }


            } catch (e: Exception) {
                Log.e(TAG, "comment: $e")
                Log.e(TAG, "comment: ${e.message}")
            }
        }

    }

    fun commentReply(commentId: String, content: String, localUpdateId: String) {
            TODO("Not yet implemented")
    }
//    fun commentReplyAudio(
//        commentId: String, content:String, contentType: String, audio: MultipartBody.Part,
//        video: MultipartBody.Part, thumbnail: MultipartBody.Part, gif: MultipartBody.Part, docs: MultipartBody.Part,
//        image: MultipartBody.Part, localUpdateId: String, duration: String,
//    ) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try{
//                val contentPart: RequestBody = RequestBody.create(
//                    "text/plain".toMediaTypeOrNull(),
//                    content
//                )
//                val contentTypePart: RequestBody = RequestBody.create(
//                    "text/plain".toMediaTypeOrNull(),
//                    contentType
//                )
//                val durationTypePart: RequestBody = RequestBody.create(
//                    "text/plain".toMediaTypeOrNull(),
//                    duration
//                )
//                val localUpdateIdPart: RequestBody = RequestBody.create(
//                    "text/plain".toMediaTypeOrNull(),
//                    localUpdateId
//                )
//                when (contentType) {
//                    "image" -> {
//                        Log.d("commentReplyAudio", "content type is image")
//                        val response = retrofitInstance.apiService.addImageCommentReply(
//                            commentId, contentPart, contentTypePart, image=image, localUpdateId =  localUpdateIdPart
//                        )
//                        val responseBody = response.body()
//                        Log.d(TAG, "comment: response $response")
//                        Log.d(TAG, "comment: response message ${response.message()}")
//                        Log.d(TAG, "comment: response message error body ${response.errorBody()}")
//                        Log.d(TAG, "comment: response body $responseBody")
//                        Log.d(TAG, "comment: response body data ${responseBody?.data}")
//                        Log.d(TAG, "comment: response body message ${responseBody!!.message}")
//                        val data = responseBody.data
//
//                        Log.d(TAG, "audio comment data response: $data")
//                        replyCommentsMutableLiveData.postValue(data)
//                    }
//                    "video" -> {
//                        Log.d("commentReplyVideo", "content type is video")
//                        val response = retrofitInstance.apiService.addVideoCommentReply(
//                            commentId, contentPart, contentTypePart, video=video, localUpdateId =  localUpdateIdPart,
//                            thumbnail = thumbnail, duration = durationTypePart
//                        )
//                        val responseBody = response.body()
//                        Log.d("commentReplyVideo", "comment: response $response")
//                        Log.d("commentReplyVideo", "comment: response message ${response.message()}")
//                        Log.d("commentReplyVideo", "comment: response message error body ${response.errorBody()}")
//                        Log.d("commentReplyVideo", "comment: response body $responseBody")
//                        Log.d("commentReplyVideo", "comment: response body data ${responseBody?.data}")
//                        Log.d("commentReplyVideo", "comment: response body message ${responseBody!!.message}")
//                        val data = responseBody.data
//
//                        Log.d(TAG, "audio comment data response: $data")
//                        replyCommentsMutableLiveData.postValue(data)
//                    }
//                    else -> {
//                        Log.d("commentReplyAudio", "content type is audio")
//
//                        val response = retrofitInstance.apiService.addCommentReply(
//                            commentId, contentPart, contentTypePart, audio=audio, localUpdateId = localUpdateIdPart
//                        )
//                        val responseBody = response.body()
//                        Log.d(TAG, "comment: response $response")
//                        Log.d(TAG, "comment: response message ${response.message()}")
//                        Log.d(TAG, "comment: response message error body ${response.errorBody()}")
//                        Log.d(TAG, "comment: response body $responseBody")
//                        Log.d(TAG, "comment: response body data ${responseBody?.data}")
//                        Log.d(TAG, "comment: response body message ${responseBody!!.message}")
//                        val data = responseBody.data
//
//                        Log.d(TAG, "audio comment data response: $data")
//                        replyCommentsMutableLiveData.postValue(data)
//                    }
//                }
//
//
//
//            }catch (e: Exception) {
//                Log.e(TAG, "comment: $e")
//                Log.e(TAG, "comment: ${e.message}")
//            }
//        }
//
//    }

}
