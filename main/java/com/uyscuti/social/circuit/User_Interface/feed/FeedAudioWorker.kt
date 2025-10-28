package com.uyscuti.social.circuit.User_Interface.feed

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.uyscuti.social.circuit.model.ProgressEvent
import com.uyscuti.social.circuit.model.UploadSuccessful
import com.uyscuti.social.circuit.User_Interface.media.ProgressRequestBody
import com.uyscuti.social.circuit.User_Interface.shorts.UniqueIdGenerator
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

private const val TAG = "FeedAudioWorker"

@HiltWorker
class FeedAudioWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    @Assisted val retrofitInstance: RetrofitInstance
) : CoroutineWorker(context, parameters){
    private val uniqueId = UniqueIdGenerator.generateUniqueId()
    companion object {
        const val Progress = "Progress"
        private const val delayDuration = 1L

        // Define keys for input data
        const val EXTRA_FILE_PATH = "extra_file_path"
        const val CAPTION = "caption"
        const val TAGS = "tags"
        const val THUMBNAIL = "thumbnail"
        const val CONTENT_TYPE = "contentType"
        const val DURATION = "duration"
        const val NOP = "numberOfPages"
        const val FILENAME = "fileName"
        const val FILETYPE = "docType"
        const val COMPRESS_PROGRESS = "compress"


    }


    override suspend fun doWork(): Result {
        try {
            Log.d(TAG, "start do work")
            val firstUpdate = workDataOf(FeedUploadWorker.Progress to 0)
            val lastUpdate = workDataOf(FeedUploadWorker.Progress to 100)
            setProgress(firstUpdate)
            setProgressAsync(workDataOf(FeedUploadWorker.Progress to 50))
            delay(delayDuration)
            // Extract input data
            val filePath = inputData.getString(FeedUploadWorker.EXTRA_FILE_PATH)
            val caption = inputData.getString(FeedUploadWorker.CAPTION)
            val contentType = inputData.getString(FeedUploadWorker.CONTENT_TYPE)
            val tags = inputData.getStringArray(FeedUploadWorker.TAGS)
            val duration = inputData.getString(FeedUploadWorker.DURATION)
            val numberOfPages = inputData.getString(FeedUploadWorker.NOP)
            val fileName = inputData.getString(FeedUploadWorker.FILENAME)
            val docType = inputData.getString(FeedUploadWorker.FILETYPE)

            val tagsList: MutableList<String> = mutableListOf()


            tags?.let { subtopics ->
                for (subtopic in subtopics) {
                    tagsList.add(subtopic)
                }
            }

            val thumbnailFilePath = inputData.getString(
                FeedUploadWorker.THUMBNAIL)


            if (filePath.isNullOrEmpty()) {
                Log.d(TAG, "File path Empty")
                return Result.failure()
            }
            if (thumbnailFilePath.isNullOrEmpty()) {
                Log.d(TAG, "Thumbnail path Empty")

            }

            val thumbnailFile = thumbnailFilePath?.let { File(it) }
            Log.d(TAG, " Thumbnail file Path $thumbnailFilePath")

                val file = File(filePath)

                val uploadResult = uploadFeedToMongoDB(
                    file,
                    caption!!,
                    contentType!!,
                    duration!!,
                    tagsList
                ) { bytesRead, totalBytes ->
                    val progress = (bytesRead * 100 / totalBytes).toInt()


                    if(contentType == "audio") {
                        EventBus.getDefault().post(
                            ProgressEvent("workerUniqueIdAudio", progress))
                    }else {
                        EventBus.getDefault().post(
                            ProgressEvent(uniqueId, progress))
                    }

                }
                setProgress(lastUpdate)

                // Check the result of the upload and return success or failure accordingly
                return if (uploadResult) {
                    Log.d(TAG, "Upload successful")
                    EventBus.getDefault().post(UploadSuccessful(success = true))

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

    private suspend fun uploadFeedToMongoDB(
        file: File,
        caption: String,
        contentType: String,
        duration: String,
        tags: MutableList<String>,
        progressCallback: (Long, Long) -> Unit
    ): Boolean {


        Log.d(TAG, "Start uploading function")
        Log.d("uploadFeedToMongoDB", "caption $caption, content type $contentType, tags $tags")
        // Convert file content to bytes
        val fileBytes = file.readBytes()


        val requestFile: RequestBody =
            ProgressRequestBody(file, "files/*".toMediaTypeOrNull()!!, progressCallback)


        val filePart: MultipartBody.Part =
            MultipartBody.Part.createFormData("files", file.name, requestFile)


        var content = caption


        Log.d(TAG, "Tags: $tags")
        val formData = MultipartBody.Builder().setType(MultipartBody.FORM)

        // Add other form data appends
        formData.addPart(filePart)

        // Append tags with index-based keys
        tags.forEachIndexed { index, tag ->
            formData.addFormDataPart("tags[$index]", tag)
        }


        val tagParts = tags.mapIndexed { index, tag ->
            tag.toRequestBody("text/plain".toMediaTypeOrNull())
        }.toTypedArray()

        if (content.isEmpty()) {
            content = ""
        }
        val contentPart: RequestBody = content
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val contentTypePart: RequestBody = contentType
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val durationPart: RequestBody = duration
            .toRequestBody("text/plain".toMediaTypeOrNull())
        return try {
            Log.d("uploadFeedToMongoDB", "uploadFeedToMongoDB: inside try for upload")
            val response = retrofitInstance.apiService.uploadFilesFeed(
                content = contentPart,
                contentType = contentTypePart,
                files = filePart,
                tags = tagParts,
                duration = durationPart
            )
            Log.d("uploadFeedToMongoDB", "response b4 success check: $response")

            if (response.isSuccessful) {
                // Existing code for a successful response...
                Log.i("uploadFeedToMongoDB", "Shorts upload successful")
                Log.i("uploadFeedToMongoDB", "Shorts ${response.body()!!.data}")
                true
            } else {
                Log.i("uploadFeedToMongoDB", "Shorts upload failed ${response.message()}")
                Log.i("uploadFeedToMongoDB", "Shorts upload failed ${response.code()}")
                Log.i("uploadFeedToMongoDB", "Shorts upload failed ${response.body()?.message}")
                Log.i(
                    "uploadFeedToMongoDB",
                    "Shorts error response body: ${response.errorBody()?.string()}"
                )
                false
            }
        } catch (e: Exception) {
            // Handle exceptions, log errors, etc.
            Log.d("uploadFeedToMongoDB", "Failed to upload short because: ${e.message}")
            Log.d("uploadFeedToMongoDB", "Failed to upload short because: $e")
            Log.d("uploadFeedToMongoDB", "Failed to upload short because: ${e.printStackTrace()}")
            false
        }

    }

}