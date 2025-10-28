
package com.uyscuti.social.circuit.viewmodels.feed

import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedFilesClass
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.social.circuit.User_Interface.media.ProgressRequestBody
import com.uyscuti.social.network.api.request.feed.FeedTextUploadRequestBody
import com.uyscuti.social.network.api.response.feed.Data
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject

private const val TAG = "FeedUploadViewModel"

@HiltViewModel
class FeedUploadViewModel @Inject constructor(internal val retrofitInstance: RetrofitInstance) :
    ViewModel() {

    private val feedMutableLiveData: MutableLiveData<Data> = MutableLiveData()

    private val _displayText = MutableLiveData<String>()
    private var uploadProgress = 0
    var mixedFilesCount = 0

    // Changed to MutableLiveData
    private val _mixedFeedUploadDataClass = MutableLiveData<MutableList<MixedFeedUploadDataClass>>(mutableListOf())
    val mixedFeedUploadDataClassList: LiveData<MutableList<MixedFeedUploadDataClass>> get() = _mixedFeedUploadDataClass

    // Document thumbnail preservation map - storing file paths as strings
    private val documentThumbnailCache: MutableMap<String, String> = mutableMapOf()

    fun addMixedFeedUploadDataClass(feedUploadDataClass: MixedFeedUploadDataClass) {
        // Preserve document thumbnail before adding
        feedUploadDataClass.documents?.let { doc ->
            if (doc.documentThumbnailFilePath != null) {
                documentThumbnailCache[doc.filename] = doc.filename // Store filename as reference
                Log.d(TAG, "Cached thumbnail for document: ${doc.filename}")
            }
        }

        val currentList = getMixedFeedUploadDataClass()
        currentList.add(feedUploadDataClass)
        _mixedFeedUploadDataClass.value = currentList // Notify observers
        Log.d(TAG, "addMixedFeedUploadDataClass: size: ${currentList.size}")
    }

    // Function to remove an item at the specified position
    fun removeMixedFeedUploadDataClass(position: Int) {
        val currentList = getMixedFeedUploadDataClass()
        if (position >= 0 && position < currentList.size) {
            val removedItem = currentList.removeAt(position)
            _mixedFeedUploadDataClass.value = currentList // Notify observers
            // Remove thumbnail from cache if it exists
            removedItem.documents?.filename?.let { filename ->
                documentThumbnailCache.remove(filename)
                Log.d(TAG, "Removed thumbnail cache for: $filename")
            }
            Log.d(TAG, "Removed item at position $position, new size: ${currentList.size}")
        } else {
            Log.w(TAG, "Invalid position $position for list size ${currentList.size}")
        }
    }

    fun getMixedFeedUploadDataClass(): MutableList<MixedFeedUploadDataClass> {
        // Restore document thumbnails before returning
        restoreDocumentThumbnails()
        Log.d(TAG, "getMixedFeedUploadDataClass: size: ${_mixedFeedUploadDataClass.value?.size ?: 0}")
        return _mixedFeedUploadDataClass.value ?: mutableListOf()
    }

    fun updateMixedFeedUploadDataClass(position: Int, mixedFeedUploadDataClass: MixedFeedUploadDataClass) {
        val currentList = getMixedFeedUploadDataClass()
        if (position in currentList.indices) {
            // Preserve existing thumbnail if new one is missing
            val existingData = currentList[position]
            mixedFeedUploadDataClass.documents?.let { newDoc ->
                existingData.documents?.let { existingDoc ->
                    if (newDoc.documentThumbnailFilePath == null && existingDoc.documentThumbnailFilePath != null) {
                        newDoc.documentThumbnailFilePath = existingDoc.documentThumbnailFilePath
                        Log.d(TAG, "Restored thumbnail for document: ${newDoc.filename}")
                    }
                    // Also update cache
                    if (newDoc.documentThumbnailFilePath != null) {
                        documentThumbnailCache[newDoc.filename] = newDoc.filename
                    }
                }
            }
            currentList[position] = mixedFeedUploadDataClass
            _mixedFeedUploadDataClass.value = currentList // Notify observers
            Log.d(TAG, "Updated data at position: $position")
        }
    }

    fun getUploadProgress(): Int {
        return uploadProgress
    }

    fun setUploadProgress(progress: Int) {
        uploadProgress = progress
    }

    val displayText: LiveData<String>
        get() = _displayText

    fun setText(text: String) {
        _displayText.value = text
    }

    // Method to restore document thumbnails from cache
    private fun restoreDocumentThumbnails() {
        val currentList = _mixedFeedUploadDataClass.value ?: return
        currentList.forEach { mixedData ->
            mixedData.documents?.let { doc ->
                if (doc.documentThumbnailFilePath == null) {
                    documentThumbnailCache[doc.filename]?.let { cachedThumbnailPath ->
                        // Convert file path back to Bitmap
                        val bitmap = BitmapFactory.decodeFile(cachedThumbnailPath)
                        doc.documentThumbnailFilePath = bitmap
                        Log.d(TAG, "Restored cached thumbnail for: ${doc.filename}")
                    }
                }
            }
        }
    }

    // Method to manually preserve thumbnails
    fun preserveDocumentThumbnails() {
        val currentList = _mixedFeedUploadDataClass.value ?: return
        currentList.forEach { mixedData ->
            mixedData.documents?.let { doc ->
                if (doc.documentThumbnailFilePath != null) {
                    documentThumbnailCache[doc.filename] = doc.filename // Store filename as reference
                    Log.d(TAG, "Preserved thumbnail for: ${doc.filename}")
                }
            }
        }
    }

    // Method to verify all document thumbnails are present
    fun verifyDocumentThumbnails(): Boolean {
        var allThumbnailsPresent = true
        _mixedFeedUploadDataClass.value?.forEachIndexed { index, mixedData ->
            mixedData.documents?.let { doc ->
                if (doc.documentThumbnailFilePath == null) {
                    Log.w(TAG, "Missing thumbnail for document at position $index: ${doc.filename}")
                    allThumbnailsPresent = false
                    documentThumbnailCache[doc.filename]?.let { cachedThumbnailPath ->
                        val bitmap = BitmapFactory.decodeFile(cachedThumbnailPath)
                        doc.documentThumbnailFilePath = bitmap
                        Log.d(TAG, "Restored thumbnail from cache for: ${doc.filename}")
                        allThumbnailsPresent = true
                    }
                } else {
                    Log.d(TAG, "Document at position $index has thumbnail: ${doc.filename}")
                }
            }
        }
        return allThumbnailsPresent
    }

    // Extension function for clearing data while preserving thumbnails
    fun clearMixedFeedUploadDataClass() {
        // First preserve all thumbnails
        preserveDocumentThumbnails()

        // Clear the list
        _mixedFeedUploadDataClass.value = mutableListOf()
        mixedFilesCount = 0

        Log.d(TAG, "Cleared mixed feed upload data, preserved ${documentThumbnailCache.size} document thumbnails")
    }

    // Method to clear thumbnail cache if needed
    fun clearThumbnailCache() {
        documentThumbnailCache.clear()
        Log.d(TAG, "Cleared document thumbnail cache")
    }

    // Method to get thumbnail for specific document
    fun getDocumentThumbnail(filename: String): String? {
        return documentThumbnailCache[filename]
    }

    // Rest of your existing methods remain the same...
    fun uploadTextFeed(
        content: String, contentType: String, tags: MutableList<String>,
        onSuccess: (Data) -> Unit, onError: (String) -> Unit
    ) {
        Log.d(TAG, "feed: inside text feed content $content, contentType: $contentType, tags $tags")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val feedRequestBody = FeedTextUploadRequestBody(content, contentType, tags)
                val response = retrofitInstance.apiService.uploadTextFeed(feedRequestBody)
                val responseBody = response.body()
                Log.d(TAG, "feed: response $response")
                Log.d(TAG, "feed: response message ${response.message()}")
                Log.d(TAG, "feed: response message error body ${response.errorBody()}")
                Log.d(TAG, "feed: response body $responseBody")
                Log.d(TAG, "feed: response body data ${responseBody?.data}")
                Log.d(TAG, "feed: response body message ${responseBody!!.message}")

                val data = responseBody.data
                onSuccess(data)
                Log.d(TAG, "text comment data response: $data")
                feedMutableLiveData.postValue(data)
            } catch (e: Exception) {
                Log.e(TAG, "comment: $e")
                Log.e(TAG, "comment: ${e.message}")
                e.message?.let { onError(it) }
                e.printStackTrace()
            }
        }
    }

    fun uploadFileFeed(filePath: String, content: String, contentType: String, tags: MutableList<String>) {
        Log.d(TAG, "feed: inside text feed content $content, contentType: $contentType, tags $tags")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(filePath)
                val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                val image = MultipartBody.Part.createFormData("files", file.name, requestFile)
                val contentPart: RequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
                val contentTypePart: RequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
                val tagsPart: RequestBody = tags.joinToString(",").toRequestBody("text/plain".toMediaTypeOrNull())

                val feedRequestBody = FeedTextUploadRequestBody(content, contentType, tags)
                val tagsRequestBody = tags.map { it.toRequestBody("text/plain".toMediaTypeOrNull()) }.toTypedArray()

            } catch (e: Exception) {
                Log.e(TAG, "comment: $e")
                Log.e(TAG, "comment: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    suspend fun uploadFeedToMongoDB(
        file: File,
        caption: String,
        thumbnail: File,
        tags: MutableList<String>,
        duration: String,
        contentType: String,
        progressCallback: (Long, Long) -> Unit
    ): Boolean {
        Log.d(TAG, "Start uploading function")
        val fileBytes = file.readBytes()

        val requestFile: RequestBody = ProgressRequestBody(file, "files/*".toMediaTypeOrNull()!!, progressCallback)
        val requestFileThumbnail: RequestBody = ProgressRequestBody(thumbnail, "files/*".toMediaTypeOrNull()!!, progressCallback)
        val thumbnailFilePart: MultipartBody.Part = MultipartBody.Part.createFormData("feed_thumbnail", thumbnail.name, requestFileThumbnail)
        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData("files", file.name, requestFile)
        val content = caption

        Log.d(TAG, "Tags: $tags")

        val formData = MultipartBody.Builder().setType(MultipartBody.FORM)
        formData.addPart(filePart)
        formData.addPart(thumbnailFilePart)

        tags.forEachIndexed { index, tag ->
            formData.addFormDataPart("tags[$index]", tag)
        }

        val tagParts = tags.mapIndexed { index, tag ->
            tag.toRequestBody("text/plain".toMediaTypeOrNull())
        }.toTypedArray()

        val contentPart: RequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentTypePart: RequestBody = contentType.toRequestBody("text/plain".toMediaTypeOrNull())
        val durationPart: RequestBody = duration.toRequestBody("text/plain".toMediaTypeOrNull())

        return try {
            Log.d(TAG, "try send Content type $contentType duration $duration thumbnail ${thumbnail.path}")
            val response = retrofitInstance.apiService.uploadFileFeed(
                content = contentPart,
                contentType = contentTypePart,
                duration = durationPart,
                files = filePart,
                tags = tagParts,
                thumbnail = thumbnailFilePart
            )
            Log.d("Shorts", "response b4 success check: $response")

            if (response.isSuccessful) {
                Log.i("Shorts", "Shorts upload successful")
                Log.i("Shorts", "Shorts ${response.body()!!.data}")
                true
            } else {
                Log.i("Shorts", "Shorts upload failed ${response.message()}")
                Log.i("Shorts", "Shorts upload failed ${response.code()}")
                Log.i("Shorts", "Shorts upload failed ${response.body()?.message}")
                Log.i("Shorts", "Shorts error response body: ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.d(TAG, "Failed to upload short because: ${e.message}")
            Log.d(TAG, "Failed to upload short because: $e")
            Log.d(TAG, "Failed to upload short because: ${e.printStackTrace()}")
            false
        }
    }

    suspend fun likeUnLikeFeed(shortOwnerId: String) {
        val TAG = "likeUnLikeFeed"
        try {
            val response = retrofitInstance.apiService.likeUnLikeFeed(shortOwnerId)
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(TAG, "likeUnLikeFeed ${responseBody?.data!!.isLiked}")
            } else {
                Log.d(TAG, "Error: ${response.message()}")
            }
        } catch (e: HttpException) {
            Log.d(TAG, "Http Exception ${e.message}")
        } catch (e: IOException) {
            Log.d(TAG, "IOException ${e.message}")
        }
    }

    suspend fun favoriteFeed(shortOwnerId: String) {
        val TAG = "favoriteFeed"
        try {
            val response = retrofitInstance.apiService.favoriteFeed(shortOwnerId)
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(TAG, "isBookmarked ${responseBody?.data!!.isBookmarked}")
            } else {
                Log.d(TAG, "Error: ${response.message()}")
            }
        } catch (e: HttpException) {
            Log.d(TAG, "Http Exception ${e.message}")
        } catch (e: IOException) {
            Log.d(TAG, "IOException ${e.message}")
        }
    }

    var mixedFeedFilesClass: MutableList<MixedFeedFilesClass> = mutableListOf()
}
