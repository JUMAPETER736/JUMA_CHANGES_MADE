package com.uyscuti.sharedmodule.shorts

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.uyscuti.sharedmodule.media.ProgressRequestBody
import com.uyscuti.sharedmodule.model.CancelShortsUpload
import com.uyscuti.sharedmodule.model.ProgressEvent
import com.uyscuti.sharedmodule.model.UploadSuccessful
import com.uyscuti.sharedmodule.utils.deleteFiled
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
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
    val retrofitInstance: RetrofitInstance
) :
    CoroutineWorker(context, parameters) {

    // ADDED: Track cancellation state (kept your existing flag)
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
    }

    val TAG = "ShotsUploadWorker"

    override suspend fun doWork(): Result {
        Log.d(TAG, " WORKER STARTED ")
        Log.d(TAG, "Worker ID: $id")
        Log.d(TAG, "Unique ID: $uniqueId")

        try {
            // CRITICAL: Check if cancelled before starting
            if (isStopped) {
                Log.w(TAG, "✗ Work cancelled")
                cleanupFiles(
                    inputData.getString(EXTRA_FILE_PATH),
                    inputData.getString(THUMBNAIL)
                )
                EventBus.getDefault().post(CancelShortsUpload(cancel = true))
                return Result.failure()
            }

            if (isCancelled) {
                Log.w(TAG, "Work cancelled before starting (isCancelled)")
                return Result.failure()
            }

            Log.d(TAG, "start do work")
            val firstUpdate = workDataOf(Progress to 0)
            val lastUpdate = workDataOf(Progress to 100)
            setProgress(firstUpdate)
            setProgressAsync(workDataOf(Progress to 50))
            delay(delayDuration)

            // Extract input data
            val filePath = inputData.getString(EXTRA_FILE_PATH)
            val caption = inputData.getString(CAPTION)
            val thumbnailFilePath = inputData.getString(THUMBNAIL)
            val tags = inputData.getString(TAGS)
            val fileId = inputData.getString(FILE_ID)
            val feedShortsBusinessId = inputData.getString(FEED_SHORTS_BUSINESS_ID)

            Log.d(TAG, "File path: $filePath")
            Log.d(TAG, "Caption: $caption")
            Log.d(TAG, "Thumbnail path: $thumbnailFilePath")
            Log.d(TAG, "File ID: $fileId")
            Log.d(TAG, "Feed Shorts Business ID: $feedShortsBusinessId")

            // CRITICAL: Check cancellation again after delay
            if (isStopped) {
                Log.w(TAG, "✗ Work cancelled")
                cleanupFiles(
                    inputData.getString(EXTRA_FILE_PATH),
                    inputData.getString(THUMBNAIL)
                )
                EventBus.getDefault().post(CancelShortsUpload(cancel = true))
                return Result.failure()
            }

            // Check if filePath is not null and not empty
            if (filePath.isNullOrEmpty()) {
                Log.e(TAG, "File path empty")
                return Result.failure()
            }
            if (thumbnailFilePath.isNullOrEmpty()) {
                Log.e(TAG, "Thumbnail path empty")
                return Result.failure()
            }

            val thumbnailFile = File(thumbnailFilePath)
            Log.d(TAG, "Thumbnail file path: $thumbnailFilePath")

            // ADDED: Validate files exist
            if (!thumbnailFile.exists()) {
                Log.e(TAG, "Thumbnail file does not exist: $thumbnailFilePath")
                return Result.failure()
            }

            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "Video file does not exist: $filePath")
                return Result.failure()
            }

            Log.d(TAG, "File size: ${getFileSize(file.length())}")

            // CRITICAL: Check cancellation before upload
            if (isStopped) {
                Log.w(TAG, "✗ Work cancelled")
                cleanupFiles(
                    inputData.getString(EXTRA_FILE_PATH),
                    inputData.getString(THUMBNAIL)
                )
                EventBus.getDefault().post(CancelShortsUpload(cancel = true))
                return Result.failure()
            }

            // MODIFIED: Upload with cancellation checks
            val uploadResult = uploadShortToMongoDB(
                file,
                caption!!,
                fileId!!,
                feedShortsBusinessId!!,
                thumbnailFile
            ) { bytesRead, totalBytes ->
                // CRITICAL: Check cancellation during upload
                if (isStopped || isCancelled) {
                    Log.w(TAG, "Upload cancelled during progress")
                    throw CancellationException("Upload cancelled by user")
                }

                val progress = (bytesRead * 100 / totalBytes).toInt()

                // Update progress
                setProgressAsync(workDataOf(Progress to progress))

                // Post to both Feed and Shorts fragments
                EventBus.getDefault().post(ProgressEvent(uniqueId, progress))
                EventBus.getDefault().post(ProgressEvent("workerUniqueIdShorts", progress))

                Log.d(TAG, "Upload progress: $progress% (${getFileSize(bytesRead)}/${getFileSize(totalBytes)})")
            }

            setProgress(lastUpdate)

            // CRITICAL: Final cancellation check
            if (isStopped) {
                Log.w(TAG, "✗ Work cancelled")
                cleanupFiles(
                    inputData.getString(EXTRA_FILE_PATH),
                    inputData.getString(THUMBNAIL)
                )
                EventBus.getDefault().post(CancelShortsUpload(cancel = true))
                return Result.failure()
            }

            // Check the result of the upload and return success or failure accordingly
            return if (uploadResult) {
                Log.d(TAG, "Shorts Upload successful")

                // Post success events
                EventBus.getDefault().post(UploadSuccessful(success = true))

                // Cleanup files
                cleanupFiles(filePath, thumbnailFilePath)

                Log.d(TAG, " WORKER COMPLETED SUCCESSFULLY ")
                Result.success()

            } else {
                Log.e(TAG, "Failed to upload short")
                EventBus.getDefault().post(UploadSuccessful(success = false))
                Result.failure()
            }

        } catch (e: CancellationException) {
            // ADDED: Handle cancellation exception
            Log.w(TAG, "Upload cancelled via exception", e)
            val filePath = inputData.getString(EXTRA_FILE_PATH)
            val thumbnailFilePath = inputData.getString(THUMBNAIL)
            cleanupFiles(filePath, thumbnailFilePath)
            EventBus.getDefault().post(CancelShortsUpload(cancel = true))
            return Result.failure()

        } catch (e: Exception) {
            // Handle exceptions, log errors, etc.
            Log.e(TAG, "✗ doWork exception", e)
            Log.e(TAG, "doWork: ${e.message}")
            Log.e(TAG, "doWork: ${e.printStackTrace()}")
            Log.e(TAG, "doWork: $e")
            return Result.failure()
        }
    }

    //Your existing upload function with cancellation checks
    private suspend fun uploadShortToMongoDB(
        file: File,
        caption: String,
        fileId: String,
        feedShortsBusinessId: String,
        thumbnail: File,
        progressCallback: (Long, Long) -> Unit
    ): Boolean {

        Log.d(TAG, "Start uploading function")

        // CRITICAL: Check cancellation at start
        if (isStopped || isCancelled) {
            Log.w(TAG, "✗ Upload cancelled at start")
            throw CancellationException("Upload cancelled")
        }

        // Convert file content to bytes
        val fileBytes = file.readBytes()

        // Keep using your existing ProgressRequestBody
        // The cancellation will be checked in the progress callback
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

        val tags = if (tagsString.isNotEmpty()) {
            tagsString.split(",").map { it.trim() }
        } else {
            emptyList()
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

        val tagParts = tags.mapIndexed { index, tag ->
            tag.toRequestBody("text/plain".toMediaTypeOrNull())
        }.toTypedArray()

        val contentPart: RequestBody = content
            .toRequestBody("text/plain".toMediaTypeOrNull())

        val feedShortsBusinessIdPart: RequestBody = feedShortsBusinessId
            .toRequestBody("text/plain".toMediaTypeOrNull())

        val fileIdPart: RequestBody = fileId
            .toRequestBody("text/plain".toMediaTypeOrNull())

        Log.d("Shorts", "content: $contentPart")
        Log.d("Shorts", "content: $tagParts")

        return try {
            // CRITICAL: Check cancellation before API call
            if (isStopped || isCancelled) {
                Log.w(TAG, "✗ Upload cancelled before API call")
                throw CancellationException("Upload cancelled")
            }

            val response = retrofitInstance.apiService.uploadShort(
                content = contentPart,
                images = filePart,
                thumbnail = thumbnailFilePart,
                tags = tagParts,
                fileId = fileIdPart,
                feedShortsBusinessId = feedShortsBusinessIdPart
            )

            Log.d("Shorts", "response b4 success check: $response")

            // CRITICAL: Check cancellation after API call
            if (isStopped || isCancelled) {
                Log.w(TAG, "✗ Upload cancelled after API call")
                throw CancellationException("Upload cancelled")
            }

            if (response.isSuccessful) {
                // Existing code for a successful response...
                Log.i("Shorts", "✓ Shorts upload successful")
                Log.i("Shorts", "Shorts ${response.body()!!.data}")
                true
            } else {
                Log.e("Shorts", "✗ Shorts upload failed ${response.message()}")
                Log.e("Shorts", "Shorts upload failed ${response.code()}")
                Log.e("Shorts", "Shorts upload failed ${response.body()?.message}")
                Log.e("Shorts", "Shorts error response body: ${response.errorBody()?.string()}")
                false
            }
        } catch (e: CancellationException) {
            Log.w(TAG, "✗ Upload cancelled", e)
            throw e
        } catch (e: Exception) {
            // Handle exceptions, log errors, etc.
            Log.e(TAG, "✗ Failed to upload short because: ${e.message}")
            Log.e(TAG, "Failed to upload short because: $e")
            Log.e(TAG, "Failed to upload short because: ${e.printStackTrace()}")
            false
        }
    }


    //Cleanup files helper
    private fun cleanupFiles(filePath: String?, thumbnailPath: String?) {
        try {
            filePath?.let {
                val deleted = deleteFiled(it)
                Log.d(TAG, if (deleted) "✓ Video file deleted: $it" else "⚠ Failed to delete video file: $it")
            }

            thumbnailPath?.let {
                val thumbnailFile = File(it)
                if (thumbnailFile.exists()) {
                    val deleted = thumbnailFile.delete()
                    Log.d(TAG, if (deleted) "✓ Thumbnail file deleted: $it" else "⚠ Failed to delete thumbnail: $it")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up files", e)
        }
    }

    // ADDED: File size formatting helper
    private fun getFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            else -> String.format("%.2f KB", kb)
        }
    }
}