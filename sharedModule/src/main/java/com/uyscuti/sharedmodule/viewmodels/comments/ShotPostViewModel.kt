package com.uyscuti.sharedmodule.viewmodels.comments

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.uyscuti.sharedmodule.utils.AudioDurationHelper.getFormattedDuration
import com.uyscuti.sharedmodule.utils.generateMongoDBTimestamp
import com.uyscuti.sharedmodule.utils.generateRandomId
import com.uyscuti.sharedmodule.utils.getFileNameFromLocalPath
import com.uyscuti.social.network.api.models.Comment
import com.uyscuti.social.network.api.response.comment.CommentLocationData
import com.uyscuti.social.network.api.response.comment.allcomments.Account
import com.uyscuti.social.network.api.response.comment.allcomments.Author
import com.uyscuti.social.network.api.response.comment.allcomments.Avatar
import com.uyscuti.social.network.api.response.comment.allcomments.CommentFiles
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import com.uyscuti.social.network.api.response.post.Data
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ShotPostViewModel@Inject constructor(
    private val retrofitInstance: RetrofitInstance,
    private val localStorage: LocalStorage,
    @ApplicationContext private val context: Context
): ViewModel() {

    val TAG = "ShotPostViewModel"

    private val _commentsMutableLiveData: MutableLiveData<CommentState> = MutableLiveData()

    private val settings =  context.getSharedPreferences("LocalSettings", MODE_PRIVATE)

    val commentLiveData = _commentsMutableLiveData

    fun likeUnlikeShotComment(commentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            retrofitInstance.apiService.likeUnLikeComment(commentId)
        }
    }

    fun likeUnlikeShotCommentReplies(commentReplyId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            retrofitInstance.apiService.likeUnLikeCommentReply(commentReplyId)
        }
    }


    suspend fun getShotPost(postId: String): Data? {
        return withContext(Dispatchers.IO) {
            try {
                val response = retrofitInstance.apiService.getPostById(postId)
                if (response.isSuccessful) {
                    response.body()!!.data
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }


    fun addCommentReply(
        postId: String,
        content: String? = null,
        contentType: String,
        localUpdateId: String,
        file: File? = null,
        numberOfPages: Int? = null,
        fileSize: String? = null,
        fileType: String? = null,
        fileName: String? = null,
        gif: String? = null,
        isReply: Boolean,
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            try {

                if (contentType == "text" ) {
                    textComment(
                        postId,
                        content!!,
                        contentType,
                        localUpdateId,
                        isReply
                    )
                } else if(contentType == "image") {
                    imageComment(
                        postId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId,
                        isReply
                    )
                } else if(contentType == "video") {
                    videoComment(
                        postId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId,
                        isReply
                    )
                } else if(contentType == "audio") {
                    audioComment(
                        postId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId,
                        isReply,
                        fileType!!
                    )
                } else if (contentType == "docs") {
                    documentComment(
                        postId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId,
                        numberOfPages!!,
                        fileSize!!,
                        fileType!!,
                        fileName!!,
                        isReply
                    )
                } else if(contentType == "gif") {
                    gifComment(
                        postId,
                        contentType,
                        localUpdateId,
                        gif!!,
                        isReply
                    )
                }



            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    fun addComment(
        postId: String,
        content: String? = null,
        contentType: String,
        localUpdateId: String,
        file: File? = null,
        numberOfPages: Int? = null,
        fileSize: String? = null,
        fileType: String? = null,
        fileName: String? = null,
        gif: String? = null,
        isReply: Boolean = false
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            try {

                if (contentType == "text" ) {
                    textComment(
                        postId,
                        content!!,
                        contentType,
                        localUpdateId,
                        isReply
                    )
                } else if(contentType == "image") {
                    imageComment(
                        postId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId,
                        isReply
                    )
                } else if(contentType == "video") {
                    videoComment(
                        postId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId
                    )
                } else if(contentType == "audio") {
                    audioComment(
                        postId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId,
                        isReply,
                        fileType!!
                    )
                } else if (contentType == "docs") {
                    documentComment(
                        postId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId,
                        numberOfPages!!,
                        fileSize!!,
                        fileType!!,
                        fileName!!,
                        isReply
                    )
                } else if(contentType == "gif") {
                    gifComment(
                        postId,
                        contentType,
                        localUpdateId,
                        gif!!,
                        isReply
                    )
                }



            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    private suspend fun textComment(
        postId: String,
        content: String,
        contentType: String,
        localUpdateId: String,
        isReply: Boolean = false
    ) {

        // Create RequestBody for required fields
        val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentTypeBody = contentType.toRequestBody("text/plain".toMediaTypeOrNull())
        val localUpdateIdBody = localUpdateId.toRequestBody("text/plain".toMediaTypeOrNull())

        val profilePic2 = settings.getString("profile_pic", "").toString()
        val avatar = Avatar("", "", url = profilePic2)


        val account =
            Account(_id = "", avatar = avatar, "", localStorage.getUsername())
        val author = Author(
            _id = "", account = account, firstName = "", lastName = "",
            avatar = null
        )

        val comment = Comment(
            0,
            "",
            author,
            content,
            generateMongoDBTimestamp(),
            false,
            0,
            postId,
            generateMongoDBTimestamp(),
            0,
            mutableListOf(),
            contentType = contentType,
            localUpdateId = localUpdateId
        )

        _commentsMutableLiveData.postValue(CommentState(isReply, comment))

        if(isReply) {
            retrofitInstance.apiService.addShotReplyComment(
                postId,
                contentBody,
                contentTypeBody,
                localUpdateIdBody
            )

        } else {
            retrofitInstance.apiService.addShotComment(
                postId,
                contentBody,
                contentTypeBody,
                localUpdateIdBody
            )
        }

    }
