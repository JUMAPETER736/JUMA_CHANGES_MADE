package com.uyscuti.social.business.viewmodel.business

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.uyscuti.social.network.api.models.Comment
import com.uyscuti.sharedmodule.utils.AudioDurationHelper.getFormattedDuration
import com.uyscuti.sharedmodule.utils.generateMongoDBTimestamp
import com.uyscuti.sharedmodule.utils.generateRandomId
import com.uyscuti.sharedmodule.utils.getFileNameFromLocalPath
import com.uyscuti.social.network.api.response.comment.CommentLocationData
import com.uyscuti.social.network.api.response.comment.allcomments.Account
import com.uyscuti.social.network.api.response.comment.allcomments.Author
import com.uyscuti.social.network.api.response.comment.allcomments.Avatar
import com.uyscuti.social.network.api.response.comment.allcomments.CommentFiles
import com.uyscuti.social.network.api.response.commentreply.allreplies.AllCommentReplies
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
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
class BusinessPostsViewModel @Inject constructor(
    private val retrofitInstance: RetrofitInstance,
    private val localStorage: LocalStorage,
    @ApplicationContext private val context: Context
): ViewModel() {
    val TAG = "BusinessCommentsViewModel"

    private val _commentsMutableLiveData: MutableLiveData<CommentState> = MutableLiveData()

    private val settings =  context.getSharedPreferences("LocalSettings", MODE_PRIVATE)

    val commentLiveData = _commentsMutableLiveData

    fun likeUnlikeBusinessComment(commentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            retrofitInstance.apiService.likeAndUnlikeBusinessComment(commentId)
        }
    }

    suspend fun likeUnlikeBusinessPost(businessPostId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = retrofitInstance.apiService.likeAndUnlikeBusinessPost(businessPostId.toString())
                response.isSuccessful
            } catch (e: Exception){
                false
            }
        }
    }

    suspend fun followUnfollowBusinessPostOwner(userToBeFollowed: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = retrofitInstance.apiService.followUnFollowBusinessOwner(userToBeFollowed)
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    fun bookmarkUnBookmarkBusinessPost(businessPostId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            retrofitInstance.apiService.bookmarkBusinessPost(businessPostId)
        }
    }

    fun likeUnlikeBusinessCommentReplies(commentReplyId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            retrofitInstance.apiService.likeAndUnlikeBusinessCommentReply(commentReplyId)
        }
    }

    fun addCommentReply(
        businessPostId: String,
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
                        businessPostId,
                        content!!,
                        contentType,
                        localUpdateId,
                        isReply
                    )
                } else if(contentType == "image") {
                    imageComment(
                        businessPostId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId,
                        isReply
                    )
                } else if(contentType == "video") {
                    videoComment(
                        businessPostId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId,
                        isReply
                    )
                } else if(contentType == "audio") {
                    audioComment(
                        businessPostId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId,
                        isReply,
                        fileType!!
                    )
                } else if (contentType == "docs") {
                    documentComment(
                        businessPostId,
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
                        businessPostId,
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
        businessPostId: String,
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
                        businessPostId,
                        content!!,
                        contentType,
                        localUpdateId,
                        isReply
                    )
                } else if(contentType == "image") {
                    imageComment(
                        businessPostId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId,
                        isReply
                    )
                } else if(contentType == "video") {
                    videoComment(
                        businessPostId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId
                    )
                } else if(contentType == "audio") {
                    audioComment(
                        businessPostId,
                        content!!,
                        file!!,
                        contentType,
                        localUpdateId,
                        isReply,
                        fileType!!
                    )
                } else if (contentType == "docs") {
                    documentComment(
                        businessPostId,
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
                        businessPostId,
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
        businessPostId: String,
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
            businessPostId,
            generateMongoDBTimestamp(),
            0,
            mutableListOf(),
            contentType = contentType,
            localUpdateId = localUpdateId
        )

        _commentsMutableLiveData.postValue(CommentState(isReply, comment))

        if(isReply) {
            retrofitInstance.apiService.addReplyBusinessComment(
                businessPostId,
                contentBody,
                contentTypeBody,
                localUpdateIdBody
            )

        } else {
            retrofitInstance.apiService.addBusinessComment(
                businessPostId,
                contentBody,
                contentTypeBody,
                localUpdateIdBody
            )
        }

    }


    private suspend fun imageComment(
        businessPostId: String,
        content: String,
        file: File,
        contentType: String,
        localUpdateId: String,
        isReply: Boolean = false
    ) {

        val mongoDbTimeStamp = generateMongoDBTimestamp()

        Log.d("uploadImageComment", "File exists, creating comment.......")
        val profilePic2 = settings.getString("profile_pic", "").toString()
        val avatar = Avatar("", "", url = profilePic2)
        val account =
            Account(_id = "", avatar = avatar, "", localStorage.getUsername())
        val author =
            Author(_id = "12", account = account, firstName = "", lastName = "", avatar = null)

        val imageFile = CommentFiles(
            _id = localUpdateId,
            url = file.absolutePath,
            localPath = file.absolutePath
        )

        val comment = Comment(
            __v = 1,
            _id = "",
            author = author,
            content = content,
            createdAt = mongoDbTimeStamp,
            isLiked = false,
            likes = 0,
            postId = businessPostId,
            updatedAt = mongoDbTimeStamp,
            replyCount = 0,
            images = mutableListOf(imageFile),
            audios = mutableListOf(),
            docs = mutableListOf(),
            gifs = "",
            thumbnail = mutableListOf(),
            videos = mutableListOf(),
            contentType = "image",
            localUpdateId = localUpdateId
        )

        _commentsMutableLiveData.postValue(CommentState(isReply, comment))

        val imageParts = file.let {
            val requestFile = it.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestFile)
        }

        // Create RequestBody for required fields
        val contentTypeBody = contentType.toRequestBody("image/*".toMediaTypeOrNull())
        val localUpdateIdBody = localUpdateId.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())

        if (isReply) {
            retrofitInstance.apiService.addReplyBusinessComment(
                businessPostId,
                content = contentBody,
                contentType = contentTypeBody,
                localUpdateId = localUpdateIdBody,
                image = listOf(imageParts)
            )
        } else {
            retrofitInstance.apiService.addBusinessComment(
                businessPostId,
                content = contentBody,
                contentType = contentTypeBody,
                localUpdateId = localUpdateIdBody,
                image = listOf(imageParts)
            )
        }

    }

    private suspend fun videoComment(
        businessPostId: String,
        content: String,
        file: File,
        contentType: String,
        localUpdateId: String,
        isReply: Boolean = false
    ) {

        val mongoDbTimeStamp = generateMongoDBTimestamp()

        val profilePic2 = settings.getString("profile_pic", "").toString()
        val avatar = Avatar("", "", url = profilePic2)
        val account =
            Account(_id = "", avatar = avatar, "", localStorage.getUsername())
        val author =
            Author(_id = "12", account = account, firstName = "", lastName = "", avatar = null)

        val videoFile = CommentFiles(
            _id = localUpdateId,
            url = file.absolutePath,
            localPath = file.absolutePath
        )

        val durationString = getFormattedDuration(file.absolutePath)

        val fileSizeInBytes = file.length()
        val fileSizeInKB = fileSizeInBytes / 1024
        val fileSizeInMB = fileSizeInKB / 1024

        val fileSizeString = fileSizeInMB.toString() + "MB"

        val comment = Comment(
            __v = 1,
            _id ="",
            author = author,
            content = content,
            createdAt = mongoDbTimeStamp,
            isLiked = false,
            likes = 0,
            postId = businessPostId,
            updatedAt = mongoDbTimeStamp,
            replyCount = 0,
            images = mutableListOf(),
            audios = mutableListOf(),
            docs = mutableListOf(),
            gifs = "",
            thumbnail = mutableListOf(),
            videos = mutableListOf(videoFile),
            contentType = "video",
            localUpdateId = localUpdateId,
            duration = durationString,
            fileSize = fileSizeString
        )

        _commentsMutableLiveData.postValue(CommentState(isReply, comment))

        val videoParts = file.let {
            val requestFile = it.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("video", it.name, requestFile)
        }

        // Create RequestBody for required fields
        val contentTypeBody = contentType.toRequestBody("video/*".toMediaTypeOrNull())
        val localUpdateIdBody = localUpdateId.toRequestBody("text/plain".toMediaTypeOrNull())
        val durationBody = durationString.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileTypeBody = file.extension.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())


        val fileSizeBody = fileSizeString.toRequestBody("text/plain".toMediaTypeOrNull())

        if (isReply) {
            retrofitInstance.apiService.addReplyBusinessComment(
                businessPostId,
                content = contentBody,
                contentType = contentTypeBody,
                localUpdateId = localUpdateIdBody,
                video = listOf(videoParts),
                duration = durationBody,
                fileType = fileTypeBody,
                fileSize = fileSizeBody
            )

        } else {
            retrofitInstance.apiService.addBusinessComment(
                businessPostId,
                content = contentBody,
                contentType = contentTypeBody,
                localUpdateId = localUpdateIdBody,
                video = listOf(videoParts),
                duration = durationBody,
                fileType = fileTypeBody,
                fileSize = fileSizeBody
            )
        }


    }

    private suspend fun audioComment(
        businessPostId: String,
        content: String,
        file: File,
        contentType: String,
        localUpdateId: String,
        isReply: Boolean = false,
        fileType: String
    ) {

        val mongoDbTimeStamp = generateMongoDBTimestamp()
        val uploadId = generateRandomId()

        val profilePic2 = settings.getString("profile_pic", "").toString()
        val avatar = Avatar("", "", url = profilePic2)
        val account =
            Account(_id = "", avatar = avatar, "", localStorage.getUsername())
        val author =
            Author(_id = "12", account = account, firstName = "", lastName = "", avatar = null)
        val vnFile = CommentFiles(_id = localUpdateId, url = file.absolutePath, localPath = file.absolutePath)

        val durationString = getFormattedDuration(file.absolutePath)
        val fileName = getFileNameFromLocalPath(file.absolutePath)
        val fileType = fileType

        val fileSizeInBytes = file.length()
        val fileSizeInKB = fileSizeInBytes / 1024
        val fileSizeInMB = fileSizeInKB / 1024

        val fileSizeString = fileSizeInMB.toString() + "MB"

        val comment = Comment(
            __v = 1,
            _id = "",
            author = author,
            content = content,
            createdAt = mongoDbTimeStamp,
            isLiked = false,
            likes = 0,
            postId = businessPostId,
            updatedAt = mongoDbTimeStamp,
            replyCount = 0,
            images = mutableListOf(),
            audios = mutableListOf(vnFile),
            docs = mutableListOf(),
            gifs = "",
            thumbnail = mutableListOf(),
            videos = mutableListOf(),
            contentType = contentType,
            localUpdateId = localUpdateId,
            fileName = fileName,
            duration = durationString,
            fileType = fileType,
            uploadId = uploadId,
            fileSize = fileSizeString
        )

        _commentsMutableLiveData.postValue(CommentState(isReply, comment))

        val audioParts = file.let {
            val requestFile = it.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("audio", it.name, requestFile)
        }

        // Create RequestBody for required fields
        val contentTypeBody = contentType.toRequestBody("audio/*".toMediaTypeOrNull())
        val localUpdateIdBody = localUpdateId.toRequestBody("text/plain".toMediaTypeOrNull())
        val durationRequestDody = durationString.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileNameBody = fileName.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileTypeBody = fileType.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileSizeBody = fileSizeString.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())


        if (isReply) {
            retrofitInstance.apiService.addReplyBusinessComment(
                businessPostId,
                content = contentBody,
                contentType = contentTypeBody,
                localUpdateId = localUpdateIdBody,
                audio = listOf(audioParts),
                duration = durationRequestDody,
                fileName = fileNameBody,
                fileType =fileTypeBody,
                fileSize = fileSizeBody
            )
        } else {
            retrofitInstance.apiService.addBusinessComment(
                businessPostId,
                content = contentBody,
                contentType = contentTypeBody,
                localUpdateId = localUpdateIdBody,
                audio = listOf(audioParts),
                duration = durationRequestDody,
                fileName = fileNameBody,
                fileType =fileTypeBody,
                fileSize = fileSizeBody
            )
        }


    }

    private suspend fun documentComment(
        businessPostId: String,
        content: String,
        file: File,
        contentType: String,
        localUpdateId: String,
        numberOfPages: Int,
        fileSize: String,
        fileType: String,
        fileName: String,
        isReply: Boolean = false
    ) {
        val mongoDbTimeStamp = generateMongoDBTimestamp()

        val profilePic2 = settings.getString("profile_pic", "").toString()
        val avatar = Avatar("", "", url = profilePic2)
        val account =
            Account(_id = "", avatar = avatar, "", localStorage.getUsername())
        val author =
            Author(_id = "12", account = account, firstName = "", lastName = "", avatar = null)

        val documentFile = CommentFiles(
            _id = localUpdateId,
            url = file.absolutePath,
            localPath = file.absolutePath
        )

        val comment = Comment(
            __v = 1,
            _id = "",
            author = author,
            content = content,
            createdAt = mongoDbTimeStamp,
            isLiked = false,
            likes = 0,
            postId = businessPostId,
            updatedAt = mongoDbTimeStamp,
            replyCount = 0,
            images = mutableListOf(),
            audios = mutableListOf(),
            docs = mutableListOf(documentFile),
            gifs = "",
            thumbnail = mutableListOf(),
            videos = mutableListOf(),
            contentType = contentType,
            localUpdateId = localUpdateId,
            numberOfPages = numberOfPages.toString(),
            fileSize = fileSize,
            fileType = fileType,
            fileName = fileName
        )

        _commentsMutableLiveData.postValue(CommentState(isReply, comment))

        val docsParts = file.let {
            val requestFile = it.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("docs", it.name, requestFile)
        }

        // Create RequestBody for required fields
        val contentTypeBody = contentType.toRequestBody("text/plain".toMediaTypeOrNull())
        val localUpdateIdBody = localUpdateId.toRequestBody("text/plain".toMediaTypeOrNull())
        val numberOfPagesBody = numberOfPages.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val fileNameBody = fileName.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileSizeBody = fileSize.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileTypeBody = fileType.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())


        if (isReply) {
            retrofitInstance.apiService.addReplyBusinessComment(
                businessPostId,
                content = contentBody,
                contentType = contentTypeBody,
                localUpdateId = localUpdateIdBody,
                docs = listOf(docsParts),
                fileName = fileNameBody,
                fileSize = fileSizeBody,
                fileType = fileTypeBody,
                numberOfPages = numberOfPagesBody
            )
        } else {
            retrofitInstance.apiService.addBusinessComment(
                businessPostId,
                content = contentBody,
                contentType = contentTypeBody,
                localUpdateId = localUpdateIdBody,
                docs = listOf(docsParts),
                fileName = fileNameBody,
                fileSize = fileSizeBody,
                fileType = fileTypeBody,
                numberOfPages = numberOfPagesBody
            )
        }

    }

    private suspend fun gifComment(
        businessPostId: String,
        contentType: String,
        localUpdateId: String,
        gifUrl: String,
        isReply: Boolean = false
    ) {

        val mongoDbTimeStamp = generateMongoDBTimestamp()

        val profilePic2 = settings.getString("profile_pic", "").toString()
        val avatar = Avatar("", "", url = profilePic2)
        val account =
            Account(_id = "", avatar = avatar, "", localStorage.getUsername())
        val author =
            Author(_id = "12", account = account, firstName = "", lastName = "", avatar = null)

        val comment = Comment(
            __v = 1,
            _id = localUpdateId,
            author = author,
            content = "",
            createdAt = mongoDbTimeStamp,
            isLiked = false,
            likes = 0,
            postId = businessPostId,
            updatedAt = mongoDbTimeStamp,
            replyCount = 0,
            images = mutableListOf(),
            audios = mutableListOf(),
            docs = mutableListOf(),
            gifs = gifUrl,
            thumbnail = mutableListOf(),
            videos = mutableListOf(),
            contentType = "gif",
            localUpdateId = localUpdateId
        )

        _commentsMutableLiveData.postValue(CommentState(isReply, comment))

        val contentTypeBody = contentType.toRequestBody("text/plain".toMediaTypeOrNull())
        val localUpdateIdBody = localUpdateId.toRequestBody("text/plain".toMediaTypeOrNull())
        val gifBody = gifUrl.toRequestBody("text/plain".toMediaTypeOrNull())

        if (isReply) {
            retrofitInstance.apiService.addReplyBusinessComment(
                businessPostId,
                contentType = contentTypeBody,
                localUpdateId = localUpdateIdBody,
                gif = gifBody
            )
        } else {
            retrofitInstance.apiService.addBusinessComment(
                businessPostId,
                contentType = contentTypeBody,
                localUpdateId = localUpdateIdBody,
                gif = gifBody
            )
        }

    }


    suspend fun getBusinessPostComments(postId: String, page: Int): List<Comment> {
        return withContext(Dispatchers.IO) {
            try {

                val response = retrofitInstance.apiService.getBusinessPostComment(postId, page.toString())
                val responseBody = response.body()!!

                val comments = responseBody.data.comments

                val commentWithReplies = comments.map { firstComment ->
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
                        replies = firstComment.replies,
                        images = firstComment.images ,
                        audios = firstComment.audios ,
                        docs = firstComment.docs ,
                        thumbnail = firstComment.thumbnail,
                        videos = firstComment.videos,
                        contentType = firstComment.contentType,
                        localUpdateId = firstComment.localUpdateId,
                        fileType = firstComment.fileType ?: "Unknown",
                        fileName = firstComment.fileName ?: "Unknown",
                        fileSize = firstComment.fileSize ?: "0B",
                        numberOfPages = firstComment.numberOfPages ?: "0",
                        duration = firstComment.duration ?: "00:00",
                        gifs = firstComment.gifs ?: ""
                    )
                }
                commentWithReplies
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun getBusinessCommentReplies(postId: String, page: Int): AllCommentReplies{
        return withContext(Dispatchers.IO) {
            try {

                val response = retrofitInstance.apiService.getBusinessCommentReplies(postId, page.toString())

                val responseBody = response.body()!!

                Log.d(TAG, "GetCommentsReplies: response success totalCommentReplies?: ${responseBody.data.comments.size}")

                responseBody
            } catch (e: Exception) {
                e.printStackTrace()
                AllCommentReplies(
                    null!!,
                    "",
                    404,
                    false
                )
            }
        }
    }

    suspend fun locateBusinessComment(
        postId: String,
        commentId: String
    ): CommentLocationData? {
        val response = retrofitInstance.apiService.locateBusinessComment(postId,commentId)
        if (response.isSuccessful) {
            return response.body()!!.data
        }
        return null
    }
}

data class CommentState(
    val isReply: Boolean,
    val comment: Comment
)