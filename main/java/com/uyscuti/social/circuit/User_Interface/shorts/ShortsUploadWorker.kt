package com.uyscuti.social.circuit.User_Interface.shorts

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.uyscuti.social.circuit.model.ProgressEvent
import com.uyscuti.social.circuit.model.UploadSuccessful
import com.uyscuti.social.circuit.User_Interface.media.ProgressRequestBody
import com.uyscuti.social.circuit.utils.deleteFiled
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import java.io.File

@HiltWorker
class ShortsUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    @Assisted val retrofitInstance: RetrofitInstance
) :
    CoroutineWorker(context, parameters) {

    // Inside ShortsUploadWorker class
    private var isCancelled = false
    private val uniqueId = UniqueIdGenerator.generateUniqueId()

    companion object {
        const val Progress = "Progress"
        private const val delayDuration = 1L

        // Define keys for input data
        const val EXTRA_FILE_PATH = "extra_file_path"
        const val CAPTION = "caption"
        const val THUMBNAIL = "thumbnail"
        const val FILE_ID = "fileId"
        const val FEED_SHORTS_BUSINESS_ID = "fileIds"
        const val COMPRESS_PROGRESS = "compress"

        const val TAGS = "tags"
//        var THUMBNAIL: Bitmap? = null
    }

    // Inside ShortsUploadWorker class
    fun cancelWork() {
        isCancelled = true
    }


    val TAG = "Worker"
    override suspend fun doWork(): Result {
        try {
            Log.d(TAG, "start do work")
            val firstUpdate = workDataOf(Progress to 0)
            val lastUpdate = workDataOf(Progress to 100)
            setProgress(firstUpdate)
            setProgressAsync(workDataOf(Progress to 50))
            delay(delayDuration)
            // Extract input data
            val filePath = inputData.getString(EXTRA_FILE_PATH)
            val caption = inputData.getString(CAPTION)
//            val thumbNail = inputData.getString(CAPTION)
            val thumbnailFilePath = inputData.getString(THUMBNAIL)
            val tags = inputData.getString(TAGS)
            val fileId = inputData.getString(FILE_ID)
            val feedShortsBusinessId = inputData.getString(FEED_SHORTS_BUSINESS_ID)

            if (isCancelled) {
                Log.d(TAG, "Work cancelled")
                return Result.failure()
            }

//            Log.d(TAG, " Caption $caption")

            // Check if filePath is not null and not empty
            if (filePath.isNullOrEmpty()) {
                Log.d(TAG, "File path empty")
                return Result.failure()
            }
            if (thumbnailFilePath.isNullOrEmpty()) {
                Log.d(TAG, "Thumbnail path empty")
                return Result.failure()
            }

            val thumbnailFile = File(thumbnailFilePath)
            Log.d(TAG, " Thumbnail file path $thumbnailFilePath")

            val file = File(filePath)


            // Perform your file upload logic here
//            val uploadResult = uploadShortToMongoDB(file, caption!!)
            // Example usage
//            Log.d("progress id", uniqueId)
//            if(!tags.isNullOrEmpty()) {
//                val result = removeOuterBrackets(tags)
//                val tagList = listOf(result)
//                val uploadResult = uploadShortToMongoDBFromFeed(file, caption!!, tagList, thumbnailFile) { bytesRead, totalBytes ->
//                    val progress = (bytesRead * 100 / totalBytes).toInt()
////                    Log.d("UploadProgress", "Progress: $progress%")
////                Log.d("progress id", uniqueId)
//
//                    EventBus.getDefault().post(ProgressEvent(uniqueId, progress))
//
//                }
//
//
//                setProgress(lastUpdate)
//
//                // Check the result of the upload and return success or failure accordingly
//                return if (uploadResult) {
//                    Log.d(TAG, "Upload successful")
//                    EventBus.getDefault().post(UploadSuccessful(success = true))
//
//                    deleteFiled(filePath)
//                    Result.success()
//
//                } else {
//                    Log.d(TAG, "Failed to upload short")
//                    Result.failure()
//                }
//            }else {
//
//            }
//            if (feedShortsBusinessId != null) {
//                Log.d(TAG, "doWork(in short worker): failure feedShortsBusinessId $feedShortsBusinessId")
//                if(feedShortsBusinessId.isNotEmpty()) {
//                    return Result.failure()
//                }
//            }
            val uploadResult = uploadShortToMongoDB(file, caption!!,fileId!!, feedShortsBusinessId!!, thumbnailFile) { bytesRead, totalBytes ->
                val progress = (bytesRead * 100 / totalBytes).toInt()
//                    Log.d("UploadProgress", "Progress: $progress%")
//                Log.d("progress id", uniqueId)

                EventBus.getDefault().post(ProgressEvent(uniqueId, progress))

            }


            setProgress(lastUpdate)

            // Check the result of the upload and return success or failure accordingly
            return if (uploadResult) {
                Log.d(TAG, "Upload successful")
                EventBus.getDefault().post(UploadSuccessful(success = true))

                deleteFiled(filePath)
                Result.success()

            } else {
                Log.d(TAG, "Failed to upload short")
                Result.failure()
            }
        } catch (e: Exception) {
            // Handle exceptions, log errors, etc.
            Log.d(TAG, "doWork: ${e.message}")
            Log.d(TAG, "doWork: ${e.printStackTrace()}")
            Log.d(TAG, "doWork: $e")
            return Result.failure()
        }
    }
    // Convert Bitmap to File
//    private fun bitmapToFile(bitmap: Bitmap): File {
//        // Implementation depends on your specific use case
//        // This is just a basic example, consider saving the file in the app's cache directory
//        val file = File(context.cacheDir, "thumbnail.jpg")
//
//        file.outputStream().use {
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
//            it.flush()
//        }
//
//        return file
//    }

//    private suspend fun uploadShortToMongoDBFromFeed(
//        file: File,
//        caption: String,
//        tags: List<String>, // Updated to accept a list of tags
//        thumbnail: File,
//        progressCallback: (Long, Long) -> Unit
//    ): Boolean {
//
//        val requestFile: RequestBody =
//            ProgressRequestBody(file, "image/*".toMediaTypeOrNull()!!, progressCallback)
//
//        val requestFileThumbnail: RequestBody =
//            ProgressRequestBody(thumbnail, "image/*".toMediaTypeOrNull()!!, progressCallback)
//        val thumbnailFilePart: MultipartBody.Part =
//            MultipartBody.Part.createFormData("thumbnail", thumbnail.name, requestFileThumbnail)
//
//        val filePart: MultipartBody.Part =
//            MultipartBody.Part.createFormData("images", file.name, requestFile)
//
//        // Process tags
//        // Use the provided list of tags directly
//        val processedTags = tags.ifEmpty {
//            // Default tags if none are provided
//            listOf("defaultTag1", "defaultTag2")
//        }
//
//        // Log tags for debugging
//        Log.d(TAG, "Processed Tags: $processedTags")
//        Log.d(TAG, "Tags data type: ${processedTags::class.simpleName}")
//
//        val formData = MultipartBody.Builder().setType(MultipartBody.FORM)
//
//        // Add file and thumbnail parts
//        formData.addPart(filePart)
//        formData.addPart(thumbnailFilePart)
//
//        // Add content part
//        val contentPart: RequestBody = caption
//            .toRequestBody("text/plain".toMediaTypeOrNull())
//        formData.addPart(MultipartBody.Part.createFormData("content", null, contentPart))
//
//        // Append tags with index-based keys
//        processedTags.forEachIndexed { index, tag ->
//            formData.addFormDataPart("tags[$index]", tag)
//        }
//
//        // Convert form data to RequestBody
//        val requestBody = formData.build()
//
//        return try {
//            val response = retrofitInstance.apiService.uploadShort(
//                content = contentPart,
//                images = filePart,
//                tags = processedTags.mapIndexed { index, tag ->
//                    tag.toRequestBody("text/plain".toMediaTypeOrNull())
//                }.toTypedArray(),
//                thumbnail = thumbnailFilePart
//            )
//            Log.d("Shorts", "response b4 success check: $response")
//
//            if (response.isSuccessful) {
//                Log.i("Shorts", "Shorts upload successful")
//                Log.i("Shorts", "Shorts ${response.body()!!.data}")
//                true
//            } else {
//                Log.i("Shorts", "Shorts upload failed ${response.message()}")
//                Log.i("Shorts", "Shorts upload failed ${response.code()}")
//                Log.i("Shorts", "Shorts upload failed ${response.body()?.message}")
//                Log.i("Shorts", "Shorts error response body: ${response.errorBody()?.string()}")
//                false
//            }
//        } catch (e: Exception) {
//            Log.d(TAG, "Failed to upload short because: ${e.message}")
//            Log.d(TAG, "Failed to upload short because: $e")
//            Log.d(TAG, "Failed to upload short because: ${e.printStackTrace()}")
//            false
//        }
//    }

//
//    private suspend fun uploadShortToMongoDBFromFeed(
//        file: File,
//        caption: String,
//        tags: String,
//        thumbnail: File,
//        progressCallback: (Long, Long) -> Unit
//    ): Boolean {
//
//        val requestFile: RequestBody =
//            ProgressRequestBody(file, "image/*".toMediaTypeOrNull()!!, progressCallback)
//
//        val requestFileThumbnail: RequestBody =
//            ProgressRequestBody(thumbnail, "image/*".toMediaTypeOrNull()!!, progressCallback)
//        val thumbnailFilePart: MultipartBody.Part =
//            MultipartBody.Part.createFormData("thumbnail", thumbnail.name, requestFileThumbnail)
//
//        val filePart: MultipartBody.Part =
//            MultipartBody.Part.createFormData("images", file.name, requestFile)
//
//        val formData = MultipartBody.Builder().setType(MultipartBody.FORM)
//
//
//        val contentPart: RequestBody = caption
//            .toRequestBody("text/plain".toMediaTypeOrNull())
//
//        val tagsPart:RequestBody = tags.toRequestBody("text/plain".toMediaTypeOrNull())
//        val result = removeOuterBrackets(tags)
//        val tagList = listOf(result)
//
//        Log.d(TAG, "uploadShortToMongoDBFromFeed: tags list $tagList")
//        // Add other form data appends
//        formData.addPart(filePart)
//        formData.addPart(thumbnailFilePart)
//
//        // Append tags with index-based keys
//        tagList.forEachIndexed { index, tag ->
//            formData.addFormDataPart("tags[$index]", tag)
//        }
//
//        val tagParts = tagList.mapIndexed { index, tag ->
//            tag.toRequestBody("text/plain".toMediaTypeOrNull())
//        }.toTypedArray()
//
//
//        return try {
//            val response = retrofitInstance.apiService.uploadShort(
//                content = contentPart,
//                images = filePart,
//                thumbnail = thumbnailFilePart,
//                tags = tagParts,
//            )
//            Log.d("Shorts", "response b4 success check: $response")
//
//            if (response.isSuccessful) {
//                // Existing code for a successful response...
//                Log.i("Shorts", "Shorts upload successful")
//                Log.i("Shorts", "Shorts ${response.body()!!.data}")
//                true
//            } else {
//                Log.i("Shorts", "Shorts upload failed ${response.message()}")
//                Log.i("Shorts", "Shorts upload failed ${response.code()}")
//                Log.i("Shorts", "Shorts upload failed ${response.body()?.message}")
//                Log.i("Shorts", "Shorts error response body: ${response.errorBody()?.string()}")
//                false
//            }
//        } catch (e: Exception) {
//            // Handle exceptions, log errors, etc.
//            Log.d(TAG, "Failed to upload short because: ${e.message}")
//            Log.d(TAG, "Failed to upload short because: $e")
//            Log.d(TAG, "Failed to upload short because: ${e.printStackTrace()}")
//            false
//        }
//
//    }


    private suspend fun uploadShortToMongoDB(
        file: File,
        caption: String,
        fileId: String,
        feedShortsBusinessId: String,
        thumbnail: File,
        progressCallback: (Long, Long) -> Unit
    ): Boolean {


        Log.d(TAG, "Start uploading function")
        // Convert file content to bytes
        val fileBytes = file.readBytes()

//        val requestFile: RequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), fileBytes)
        val requestFile: RequestBody =
            ProgressRequestBody(file, "image/*".toMediaTypeOrNull()!!, progressCallback)

        val requestFileThumbnail: RequestBody =
            ProgressRequestBody(thumbnail, "image/*".toMediaTypeOrNull()!!, progressCallback)
        val thumbnailFilePart: MultipartBody.Part =
            MultipartBody.Part.createFormData("thumbnail", thumbnail.name, requestFileThumbnail)

        val filePart: MultipartBody.Part =
            MultipartBody.Part.createFormData("images", file.name, requestFile)

        val taggs = listOf("empty", "empty") // Replace with your actual list of tags

        val words = caption.split("\\s+".toRegex()) // Split the string into words

        val topics = mutableListOf<String>()
        val nonTags = mutableListOf<String>()

        for (word in words) {
            if (word.startsWith("#")) {
                // It's a tag
                topics.add(word.substring(1)) // Remove the '#' and add to tags
            } else {
                // It's non-tag text
                nonTags.add(word)
            }
        }

        val content = nonTags.joinToString(" ")
        val tagsString = topics.joinToString(", ")
//        val tags = tagsString.split(",").map { it.trim() }
        val tags = if (tagsString.isNotEmpty()) {
            tagsString.split(",").map { it.trim() }
        } else {
            emptyList()
//            tags
        }

        Log.d(TAG, "Tags: $tags $caption")
        Log.d(TAG, "Tags data type: ${tags::class.simpleName}")
        Log.d(TAG, "Tags: $tags")
        val formData = MultipartBody.Builder().setType(MultipartBody.FORM)

        // Add other form data appends
        formData.addPart(filePart)
        formData.addPart(thumbnailFilePart)

        // Append tags with index-based keys
        tags.forEachIndexed { index, tag ->
            formData.addFormDataPart("tags[$index]", tag)
        }

//        if (tags.isNotEmpty()) {
//            tags.forEachIndexed { index, tag ->
//                formData.addFormDataPart("tags[$index]", tag)
//            }
//        }

        val tagParts = tags.mapIndexed { index, tag ->
            tag.toRequestBody("text/plain".toMediaTypeOrNull())
        }.toTypedArray()

        if (content.isEmpty()) {
//            content = "caption was empty"
        }
        val contentPart: RequestBody = content
            .toRequestBody("text/plain".toMediaTypeOrNull())

        val feedShortsBusinessIdPart: RequestBody = feedShortsBusinessId
            .toRequestBody("text/plain".toMediaTypeOrNull())

        val fileIdPart: RequestBody = fileId
            .toRequestBody("text/plain".toMediaTypeOrNull())
//            val response = retrofitIns.apiService.uploadShort(filePart, contentPart, tagsRequestBody)
        Log.d("Shorts", "content: $contentPart")
        Log.d("Shorts", "content: $tagParts")
        return try {
            val response = retrofitInstance.apiService.uploadShort(
                content = contentPart,
                images = filePart,
                thumbnail = thumbnailFilePart,
                tags = tagParts,
                fileId = fileIdPart,
                feedShortsBusinessId = feedShortsBusinessIdPart
            )
            Log.d("Shorts", "response b4 success check: $response")

            if (response.isSuccessful) {
                // Existing code for a successful response...
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
            // Handle exceptions, log errors, etc.
            Log.d(TAG, "Failed to upload short because: ${e.message}")
            Log.d(TAG, "Failed to upload short because: $e")
            Log.d(TAG, "Failed to upload short because: ${e.printStackTrace()}")
            false
        }

    }
}
