package com.uyscuti.social.circuit.User_Interface.feed

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.uyscuti.social.circuit.eventbus.FeedUploadResponseEvent
import com.uyscuti.social.circuit.model.FeedUploadSuccessful
import com.uyscuti.social.circuit.model.ProgressEvent
import com.uyscuti.social.circuit.model.UploadSuccessful
import com.uyscuti.social.circuit.model.feed.Duration
import com.uyscuti.social.circuit.model.feed.FileName
import com.uyscuti.social.circuit.model.feed.FileSize
import com.uyscuti.social.circuit.model.feed.FileType
import com.uyscuti.social.circuit.model.feed.NumberOfPages
import com.uyscuti.social.circuit.model.feed.ThumbnailWithString
import com.uyscuti.social.circuit.User_Interface.media.ProgressRequestBody
import com.uyscuti.social.circuit.User_Interface.shorts.UniqueIdGenerator
import com.uyscuti.social.circuit.utils.commaSeparatedStringToList
import com.uyscuti.social.circuit.utils.createRequestBodies
import com.uyscuti.social.circuit.utils.feedutils.createTempFileFromFile
import com.uyscuti.social.circuit.utils.feedutils.deleteTemporaryFiles
import com.uyscuti.social.circuit.utils.feedutils.deserializeFeedUploadDataList
import com.uyscuti.social.circuit.utils.feedutils.getFileExtension
import com.uyscuti.social.circuit.utils.uriToFile2
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@HiltWorker
class FeedUploadWorker @AssistedInject constructor(
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
        const val TAGS = "tags"
        const val THUMBNAIL = "thumbnail"
        const val CONTENT_TYPE = "contentType"
        const val DURATION = "duration"
        const val NOP = "numberOfPages"
        const val FILENAME = "fileName"
        const val FILETYPE = "docType"


        const val MULTIPLE_IMAGES = "multiple_images"
        const val MULTIPLE_AUDIOS = "multiple_audios"
        const val MULTIPLE_VIDEOS = "multiple_videos"
        const val MULTIPLE_THUMBNAILS = "multiple_thumbnails"
        const val MULTIPLE_DOCS = "multiple_docs"
        const val FEED_SHORTS_BUSINESS_ID = "feed_short_business_id"


    }


    val TAG = "FeedUploadWorker"
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
            val contentType = inputData.getString(CONTENT_TYPE)
            val tags = inputData.getStringArray(TAGS)
            val duration = inputData.getString(DURATION)
            val numberOfPages = inputData.getString(NOP)
            val fileName = inputData.getString(FILENAME)
            val docType = inputData.getString(FILETYPE)

            val multipleImages = inputData.getStringArray(MULTIPLE_IMAGES)
            val multipleAudios = inputData.getStringArray(MULTIPLE_AUDIOS)
            val multipleVideos = inputData.getStringArray(MULTIPLE_VIDEOS)
            val multipleThumbnails = inputData.getStringArray(MULTIPLE_THUMBNAILS)
            val multipleDocuments = inputData.getStringArray(MULTIPLE_DOCS)
            val tagsList: MutableList<String> = mutableListOf()
            val feedShortsBusinessId = inputData.getString(FEED_SHORTS_BUSINESS_ID)

            tags?.let { subtopics ->
                for (subtopic in subtopics) {
                    tagsList.add(subtopic)
                }
            }

            val thumbnailFilePath = inputData.getString(THUMBNAIL)

            if (isCancelled) {
                Log.d(TAG, "Work cancelled")
                return Result.failure()
            }



            if (filePath.isNullOrEmpty()) {
                Log.d(TAG, "File path empty")

            }
            if (thumbnailFilePath.isNullOrEmpty()) {
                Log.d(TAG, "Thumbnail path empty")

            }

            val thumbnailFile = thumbnailFilePath?.let { File(it) }
            Log.d(TAG, " Thumbnail file path $thumbnailFilePath")


            if (contentType == "mixed_files") {

                Log.d(TAG, "doWork: upload mixed_files")

                val gson = Gson()

                // Get the JSON string from input data
                val uploadDataJson = inputData.getString("upload_data")


                if (uploadDataJson != null) {
                    val uploadResult = uploadMixedFiles(
                        uploadDataJson, caption!!,
                        contentType, feedShortsBusinessId!!, tagsList,
                        responseData = { response ->
                            Log.i("UploadResponse", "Received response: $response")


                            EventBus.getDefault().post(FeedUploadResponseEvent(response))


                        },
                    ) { bytesRead, totalBytes ->
                        val progress = (bytesRead * 100 / totalBytes).toInt()
                        Log.d("UploadProgress", "Progress: $progress%")


                        EventBus.getDefault().post(
                            ProgressEvent("mixed_files", progress))


                    }

                    setProgress(lastUpdate)

                    // Check the result of the upload and return success or failure accordingly

                    return if (uploadResult) {
                        Log.d(TAG, "Feed Uploaded successfully")
                        EventBus.getDefault().post(UploadSuccessful(success = true))
                        Result.success()

                    } else {
                        Log.d(TAG, "Failed to upload Feed")
                        EventBus.getDefault().post(UploadSuccessful(success = true))
                        Result.failure()
                    }
                }


                return Result.success()
            } else if (contentType == "docs") {


                val thumbnailFiles: List<File> = multipleThumbnails!!.map { filePaths ->
                    File(filePaths)
                }

                val multipleDocument: List<String> = multipleDocuments?.toList() ?: emptyList()
                val uploadResult = uploadFeedDocToMongoDB(

                    multipleDocument,
                    thumbnailFiles,
                    caption!!,
                    contentType,
                    numberOfPages!!,
                    fileName!!,
                    docType!!,
                    tagsList
                )
                { bytesRead, totalBytes ->
                    val progress = (bytesRead * 100 / totalBytes).toInt()
                    Log.d("UploadProgress", "Progress: $progress%")


                    EventBus.getDefault().post(ProgressEvent(uniqueId, progress))

                }

                setProgress(lastUpdate)

                // Check the result of the upload and return success or failure
                // accordingly
                return if (uploadResult) {
                    Log.d(TAG, "Feed Uploaded successfully")
                    EventBus.getDefault().post(UploadSuccessful(success = true))
                    Result.success()

                } else {
                    Log.d(TAG, "Failed to upload Feed")
                    Result.failure()
                }

            } else if (contentType == "video") {
                Log.d(TAG, "doWork: content type is multiple videos")
                val files: List<File> = multipleVideos!!.map { filePaths ->
                    File(filePaths)
                }
                val thumbnailFiles: List<File> = multipleThumbnails!!.map { filePaths ->
                    File(filePaths)
                }
                val uploadResult = uploadMultipleVideosFeedToMongoDB(
                    files,
                    thumbnailFiles,
                    caption!!,
                    contentType,
                    duration!!,
                    tagsList
                ) { bytesRead, totalBytes ->
                    val progress = (bytesRead * 100 / totalBytes).toInt()
                    EventBus.getDefault().post(ProgressEvent("workerUniqueIdVideo", progress))

                }
                setProgress(lastUpdate)

                // Check the result of the upload and return success or failure
                // accordingly
                return if (uploadResult) {
                    Log.d(TAG, "Upload successful")
                    EventBus.getDefault()
                        .post(
                            FeedUploadSuccessful(
                                success = true,
                                filesToDelete = multipleVideos.toMutableList()
                            )
                        )

                    Result.success()

                } else {
                    Log.d(TAG, "Failed to upload Feed")
                    Result.failure()
                }

            } else if (thumbnailFilePath?.isNotEmpty() == true) {
                val file = filePath?.let { File(it) }

                Log.d(TAG, "thumbnail not empty Content type $contentType")
                val uploadResult = uploadFeedToMongoDB(
                    file!!,
                    caption!!,
                    thumbnailFile!!,
                    tagsList,
                    duration = duration!!,
                    contentType = contentType!!
                ) { bytesRead, totalBytes ->
                    val progress = (bytesRead * 100 / totalBytes).toInt()


                    EventBus.getDefault().post(
                        ProgressEvent("workerUniqueIdVideo", progress))

                }
                setProgress(lastUpdate)

                // Check the result of the upload and return success or failure
                // accordingly
                return if (uploadResult) {
                    Log.d(TAG, "Upload successful")

                    EventBus.getDefault().post(UploadSuccessful(success = true))
                    Result.success()

                } else {
                    Log.d(TAG, "Failed to upload Feed")
                    Result.failure()
                }
            } else if (contentType == "multiple_images") {

                Log.d(TAG, "doWork: content type is multiple images")
                val files: List<File> = multipleImages!!.map { filePaths ->
                    File(filePaths)
                }
                val uploadResult = uploadMultipleImagesFeedToMongoDB(
                    files,
                    caption!!,
                    contentType,
                    duration!!,
                    tagsList
                ) { bytesRead, totalBytes ->
                    val progress = (bytesRead * 100 / totalBytes).toInt()

                    EventBus.getDefault().post(ProgressEvent(uniqueId, progress))

                }
                setProgress(lastUpdate)

                // Check the result of the upload and return success or failure
                // accordingly
                return if (uploadResult) {
                    Log.d(TAG, "Upload successful")
                    EventBus.getDefault().post(UploadSuccessful(success = true))

                    Result.success()

                } else {
                    Log.d(TAG, "Failed to upload Feed")
                    Result.failure()
                }
            } else if (contentType == "audio") {

                Log.d(TAG, "Do Work: content type is audio")
                val files: List<File> = multipleAudios!!.map { filePaths ->
                    File(filePaths)
                }
                val uploadResult = uploadMultipleImagesFeedToMongoDB(
                    files,
                    caption!!,
                    contentType,
                    duration!!,
                    tagsList
                ) { bytesRead, totalBytes ->
                    val progress = (bytesRead * 100 / totalBytes).toInt()

                    EventBus.getDefault().post(ProgressEvent("workerUniqueIdAudio", progress))
                }
                setProgress(lastUpdate)

                // Check the result of the upload and return success or failure
                // accordingly
                return if (uploadResult) {
                    Log.d(TAG, "Upload successful")
                    EventBus.getDefault().post(UploadSuccessful(success = true))

                    Result.success()

                } else {
                    Log.d(TAG, "Failed to upload Feed")
                    Result.failure()
                }

            } else if (contentType == "vn") {

                Log.d(TAG, "Do Work: content type is Voice Note ")

                if (multipleAudios.isNullOrEmpty()) {
                    Log.e(TAG, "Voice note files array is null or empty")
                    return Result.failure(workDataOf("error_message" to "No voice note file provided"))
                }

                val files: List<File> = multipleAudios.mapNotNull { filePath ->
                    val file = File(filePath)
                    if (file.exists()) {
                        Log.d(
                            TAG,
                            "VN File found: ${file.absolutePath}, Size: ${file.length()} bytes"
                        )
                        file
                    } else {
                        Log.e(TAG, "VN File not found: $filePath")
                        null
                    }
                }

                if (files.isEmpty()) {
                    Log.e(TAG, "No valid voice note files found")
                    return Result.failure(workDataOf("error_message" to "Voice note file does not exist"))
                }

                Log.d(TAG, "Starting VN upload with ${files.size} file(s)")
                Log.d(TAG, "Duration: $duration")
                Log.d(TAG, "FileName: $fileName")
                Log.d(TAG, "FileType: $docType")


                val uploadResult = uploadMultipleImagesFeedToMongoDB(
                    files,
                    caption ?: "",
                    contentType,
                    duration ?: "",
                    tagsList
                ) { bytesRead, totalBytes ->
                    val progress = (bytesRead * 100 / totalBytes).toInt()
                    Log.d(TAG, "VN Upload Progress: $progress%")
                    EventBus.getDefault().post(ProgressEvent("workerUniqueIdVN", progress))
                }

                setProgress(lastUpdate)

                return if (uploadResult) {
                    Log.d(TAG, "Voice note upload successful")
                    EventBus.getDefault().post(UploadSuccessful(success = true))
                    Result.success()
                } else {
                    Log.e(TAG, "Failed to upload voice note")
                    Result.failure(workDataOf("error_message" to "Upload function returned failure"))
                }
            }

            else {
                val file = filePath?.let { File(it) }

                val uploadResult = uploadFeedToMongoDB(
                    file!!,
                    caption!!,
                    contentType!!,
                    duration!!,
                    tagsList
                ) { bytesRead, totalBytes ->
                    val progress = (bytesRead * 100 / totalBytes).toInt()

                    EventBus.getDefault().post(ProgressEvent(uniqueId, progress))

                }
                setProgress(lastUpdate)

                return if (uploadResult) {
                    Log.d(TAG, "Upload successful")
                    EventBus.getDefault().post(UploadSuccessful(success = true))

                    Result.success()

                } else {
                    Log.d(TAG, "Failed to upload Feed")
                    Result.failure()
                }
            }


        } catch (e: Exception) {
            // Handle exceptions, log errors, etc.
            Log.d(TAG, "doWork: ${e.message}")
            Log.d(TAG, "doWork: ${e.printStackTrace()}")
            Log.d(TAG, "doWork: $e")
            return Result.failure()
        }
    }


    fun createDurationRequestBody(durations: List<Duration>): RequestBody {
        val gson = Gson()
        val json = gson.toJson(durations)
        return json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }

    fun createFileIdsRequestBody(fileIds: List<String>): RequestBody {
        val gson = Gson()
        val json = gson.toJson(fileIds)
        return json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }

    fun createMultipartPart(key: String, data: Any): MultipartBody.Part {
        val requestBody = createJsonRequestBody(data)
        return MultipartBody.Part.createFormData(key, null, requestBody)
    }

    fun createJsonRequestBody(data: Any): RequestBody {
        val gson = Gson()
        val json = gson.toJson(data)
        return json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }

    fun saveBitmapToFile(bitmap: Bitmap, context: Context, fileName: String): File {
        // Create a directory for the thumbnails if it doesn't already exist
        val fileDir = File(context.filesDir, "thumbnails")
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }

        // Create a File object with the specified file name
        val file = File(fileDir, fileName)

        try {
            // Write the bitmap to the file
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun uploadMixedFiles(
        uploadDataJson: String,
        caption: String,
        contentType: String,
        feedShortsBusinessId: String,
        tags: MutableList<String>,
        responseData: (String) -> Unit,
        progressCallback: (Long, Long) -> Unit
    ): Boolean {


        Log.d(TAG, "uploadMixedFiles: inside upload mixed files function")
        val dataList = deserializeFeedUploadDataList(uploadDataJson) ?: mutableListOf()

        val fileNames: MutableList<FileName> = mutableListOf()
        val durations: MutableList<Duration> = mutableListOf()
        val numberOfPage: MutableList<NumberOfPages> = mutableListOf()
        val fileTypes: MutableList<FileType> = mutableListOf()
        val fileSizes: MutableList<FileSize> = mutableListOf()
        val filePaths: MutableList<String> = mutableListOf()
        val documentFilePaths: MutableList<String> = mutableListOf()
        val documentPdfFilePaths: MutableList<String> = mutableListOf()
        val uriToFiles: MutableList<File> = mutableListOf()
        val fileIds: MutableList<String> = mutableListOf()
        val thumbnail: MutableList<ThumbnailWithString> = mutableListOf()
        val thumbnailFilePaths: ArrayList<String> = arrayListOf()

// Temporary file paths storage
        val tempFilePaths = mutableListOf<String>()



        // Process the dataList
        dataList.forEach {

            Log.d("dataList", "uploadMixedFiles: $it")
            fileIds.add(it.fileId)
            fileTypes.add(FileType(it.fileId, it.fileTypes))

            if (it.videos != null) {


                val originalFile = File(it.videos!!.videoPath)


                val tempFile: File? = try {
                    // Create a temporary file with a meaningful name based on the
                    // original file name
                    createTempFileFromFile(
                        originalFile,
                        it.fileId,
                        getFileExtension(it.videos!!.videoPath)
                    ).also { temp ->
                        // Store the temporary file path
                        tempFilePaths.add(temp.path)

                    }
                } catch (e: IOException) {
                    // Handle the exception (e.g., log the error)
                    e.printStackTrace()
                    null
                }

                if (tempFile != null) {
                    Log.d("tempFile", "uploadMixedFiles: temp file path ${tempFile.absolutePath}")
                    fileNames.add(FileName(it.fileId, it.videos!!.fileName))
                    durations.add(Duration(it.fileId, it.videos!!.videoDuration))
                    filePaths.add(tempFile.path) // Use the path of the temporary file
                    it.videos!!.thumbnail?.let { it1 ->
                        thumbnail.add(
                            ThumbnailWithString(
                                it1,
                                "${it.fileId}.png"
                            )
                        )
                    }
                }

            } else if (it.documents != null) {


                val originalFile = File(it.documents!!.pdfFilePath)

                val tempFile: File? = try {
                    // Create a temporary file with a meaningful name based on the original file name
                    createTempFileFromFile(
                        originalFile,
                        it.fileId,
                        getFileExtension(it.documents!!.pdfFilePath)
                    ).also { temp ->
                        // Store the temporary file path
                        tempFilePaths.add(temp.path)
                        // Ensure the temporary file is deleted when the JVM exits

                    }
                } catch (e: IOException) {
                    // Handle the exception (e.g., log the error)
                    e.printStackTrace()
                    null
                }

                if (tempFile != null) {
                    Log.d("tempFile", "uploadMixedFiles: temp file path ${tempFile.absolutePath}")
                    it.documents!!.documentThumbnailFilePath.let { it1 ->
                        if (it1 != null) {
                            thumbnail.add(ThumbnailWithString(it1, "${it.fileId}.png"))
                        }
                    }
//                    filePaths.add(tempFile.path) // Use the path of the temporary file
                    tempFile.path.let { it1 -> documentFilePaths.add(it1) }
                    fileSizes.add(FileSize(it.fileId, it.documents!!.fileSize))
                    fileNames.add(FileName(it.fileId, it.documents!!.filename))
                    numberOfPage.add(NumberOfPages(it.fileId, it.documents!!.numberOfPages))
                }
            } else if (it.audios != null) {
                fileNames.add(FileName(it.fileId, it.audios!!.filename))
                durations.add(Duration(it.fileId, it.audios!!.duration))
//                filePaths.add(it.audios!!.audioPath)

                val originalFile = File(it.audios!!.audioPath)

                val tempFile: File? = try {
                    // Create a temporary file with a meaningful name based on the original file name
                    createTempFileFromFile(
                        originalFile,
                        it.fileId,
                        getFileExtension(it.audios!!.audioPath)
                    ).also { temp ->
                        // Store the temporary file path
                        tempFilePaths.add(temp.path)
                        // Ensure the temporary file is deleted when the JVM exits

                    }
                } catch (e: IOException) {
                    // Handle the exception (e.g., log the error)
                    e.printStackTrace()
                    null
                }

                if (tempFile != null) {
                    filePaths.add(tempFile.path)
                }
            } else if (it.images != null) {



                val originalFile = File(it.images.compressedImagePath)

                val tempFile: File? = try {
                    // Create a temporary file with a meaningful name based on the original file name
                    createTempFileFromFile(
                        originalFile,
                        it.fileId,
                        getFileExtension(it.images.compressedImagePath)
                    ).also { temp ->
                        // Store the temporary file path
                        tempFilePaths.add(temp.path)
                        // Ensure the temporary file is deleted when the JVM exits
//                        temp.deleteOnExit()
                    }
                } catch (e: IOException) {

                    e.printStackTrace()
                    null
                }

                if (tempFile != null) {
                    filePaths.add(tempFile.path)
                }
            }
        }

        for (i in thumbnail) {
            Log.i("uploadVideoFeedThumbnails", "uploadVideoFeed: thumbnails: $i")
            val thumbnailFile = saveBitmapToFile(i.bitmap, applicationContext, i.fileName)
            val thumbnailFilePath = thumbnailFile.absolutePath

            Log.i(
                "uploadVideoFeedThumbnails",
                "thumbnailFilePath: thumbnailFilePath: $thumbnailFilePath"
            )
            thumbnailFilePaths.add(thumbnailFilePath)
        }

        val files: List<File> = filePaths.map { filePath ->
            File(filePath)
        }

        val fileParts = mutableListOf<MultipartBody.Part>()
        val thumbnailFileParts = mutableListOf<MultipartBody.Part>()

        if (documentFilePaths.isNotEmpty()) {
            for (index in documentFilePaths.indices) {
                val fileString = documentFilePaths[index]
                val documentTypeString = fileTypes[index].fileType
                Log.d(
                    "pdfFile",
                    "uploadFeedDocToMongoDB: documentTypeString $documentTypeString :: fileString :: $fileString"
                )
                val file = File(fileString)

                if (file.isFile) {
                    Log.d("pdfFile", "uploadMixedFiles: pdf file path ${file.absoluteFile}")
                    uriToFiles.add(file)

                } else {
                    Log.d("pdfFile", "uploadMixedFiles: pdf file path not file")
                    return false
                }
            }
        }

        // Iterate through each file and create corresponding MultipartBody.Part
        files.forEach { file ->
            val fileBytes = file.readBytes()
            val requestFile: RequestBody =
                ProgressRequestBody(file, "files/*".toMediaTypeOrNull()!!, progressCallback)
            val filePart: MultipartBody.Part =
                MultipartBody.Part.createFormData("files", file.name, requestFile)
            fileParts.add(filePart)
        }

        if (uriToFiles.isNotEmpty()) {

        }
        uriToFiles.forEach { file ->
            val fileBytes = file.readBytes()
            val requestFile: RequestBody =
                ProgressRequestBody(file, "files/*".toMediaTypeOrNull()!!, progressCallback)
            val filePart: MultipartBody.Part =
                MultipartBody.Part.createFormData("files", file.name, requestFile)
            fileParts.add(filePart)
        }
        val thumbnails: List<File> = thumbnailFilePaths.map { filePath ->
            File(filePath)
        }
        thumbnails.forEach { file ->
            val fileBytes = file.readBytes()
            val requestFile: RequestBody =
                ProgressRequestBody(file, "files/*".toMediaTypeOrNull()!!, progressCallback)
            val filePart: MultipartBody.Part =
                MultipartBody.Part.createFormData("feed_thumbnail", file.name, requestFile)
            thumbnailFileParts.add(filePart)
        }



        val gson = Gson()
        val durationRequestBodies = durations.map { duration ->
            val json = gson.toJson(duration)
            Log.d("durationRequestBody", "uploadMixedFiles json: $json")
            json.toRequestBody("application/json".toMediaType())
        }

        val fileTypesRequestBodies = fileTypes.map { filetype ->
            val json = gson.toJson(filetype)
            Log.d("fileTypesRequestBodies", "uploadMixedFiles json: $json")
            json.toRequestBody("application/json".toMediaType())
        }

        val filenamesRequestBodies = createRequestBodies(gson, fileNames)
        val fileSizeRequestBodies = createRequestBodies(gson, fileSizes)
        val numberOfPagesRequestBodies = createRequestBodies(gson, numberOfPage)

        val tagParts = tags.mapIndexed { index, tag ->
            tag.toRequestBody("text/plain".toMediaTypeOrNull())
        }.toTypedArray()

        val contentPart: RequestBody = caption.toRequestBody("text/plain".toMediaTypeOrNull())
        val feedShortsBusinessIdPart: RequestBody = feedShortsBusinessId.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentTypePart: RequestBody =
            contentType.toRequestBody("text/plain".toMediaTypeOrNull())

        val fileIdsRequestBody = createFileIdsRequestBody(fileIds)

        Log.d(
            "durationRequestBody",
            "uploadMixedFiles durationRequestBodies: $durationRequestBodies"
        )
        return try {


            val response = retrofitInstance.apiService.uploadMixedFilesFeed(
                contentPart,
                contentTypePart,
                feedShortsBusinessIdPart,
                durationRequestBodies.toTypedArray(),
                fileTypesRequestBodies.toTypedArray(),
                numberOfPagesRequestBodies.toTypedArray(),
                filenamesRequestBodies.toTypedArray(),
                fileSizeRequestBodies.toTypedArray(),
                fileIdsRequestBody,
                fileParts,
                thumbnailFileParts,
                tagParts,
            )
            deleteTemporaryFiles(tempFilePaths)
            if (response.isSuccessful) {
                // Handle successful response
                Log.i("uploadFeedToMongoDB", "Files upload successful")
                Log.i("uploadFeedToMongoDB", "Response: ${response.body()?.data}")
                response.body()?.data?.let { responseData(it._id) }
                true
            } else {
                // Handle unsuccessful response

                Log.i("uploadFeedToMongoDB", "Files upload failed ${response.message()}")
                Log.i("uploadFeedToMongoDB", "Files upload failed ${response.code()}")
                Log.i("uploadFeedToMongoDB", "Files upload failed ${response.body()?.message}")
                Log.i(
                    "uploadFeedToMongoDB",
                    "Error response body: ${response.errorBody()?.string()}"
                )
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadMixedFiles: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private suspend fun uploadFeedDocToMongoDB(
        filePath: List<String>,
        thumbnails: List<File>,
        caption: String,
        contentType: String,
        numberOfPages: String,
        fileName: String,
        docType: String,
        tags: MutableList<String>,
        progressCallback: (Long, Long) -> Unit
    ): Boolean {


        Log.d(TAG, "Start uploading function")
        Log.d("uploadFeedToMongoDB", "docType $docType, content type $contentType, tags $tags")

        val uriToFiles: MutableList<File> = mutableListOf()
        val documentTypeList = commaSeparatedStringToList(docType)
        for (index in filePath.indices) {
            val fileString = filePath[index]
            val documentTypeString = documentTypeList[index]
            Log.d(
                "uploadFeedDocToMongoDB",
                "uploadFeedDocToMongoDB: documentTypeString $documentTypeString :: fileString :: $fileString"
            )
            val files = uriToFile2(applicationContext, fileString.toUri(), documentTypeString)
            if (files != null) {
                uriToFiles.add(files)
            }
        }

        val fileParts = mutableListOf<MultipartBody.Part>()
        val thumbnailFileParts = mutableListOf<MultipartBody.Part>()
        uriToFiles.forEach { file ->
            val fileBytes = file.readBytes()
            val requestFile: RequestBody =
                ProgressRequestBody(file, "files/*".toMediaTypeOrNull()!!, progressCallback)
            val filePart: MultipartBody.Part =
                MultipartBody.Part.createFormData("files", file.name, requestFile)
            fileParts.add(filePart)
        }

        thumbnails.forEach { file ->
            val fileBytes = file.readBytes()
            val requestFile: RequestBody =
                ProgressRequestBody(file, "files/*".toMediaTypeOrNull()!!, progressCallback)
            val filePart: MultipartBody.Part =
                MultipartBody.Part.createFormData("feed_thumbnail", file.name, requestFile)
            thumbnailFileParts.add(filePart)
        }

        var content = caption


        Log.d(TAG, "Tags: $tags")
        val formData = MultipartBody.Builder().setType(MultipartBody.FORM)


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
        val numberOfPagesPart: RequestBody = numberOfPages
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val fileNamePart: RequestBody = fileName
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val docTypePart: RequestBody = docType
            .toRequestBody("text/plain".toMediaTypeOrNull())
        return try {
            Log.d("uploadFeedToMongoDB", "uploadFeedToMongoDB: inside try for upload")
            val response = retrofitInstance.apiService.uploadFeedDoc(
                content = contentPart,
                contentType = contentTypePart,
                numberOfPages = numberOfPagesPart,
                fileName = fileNamePart,
                docType = docTypePart,
                files = fileParts,
                thumbnail = thumbnailFileParts,
                tags = tagParts,
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



    private suspend fun uploadMultipleVideosFeedToMongoDB(
        files: List<File>, // List of files to upload
        thumbnails: List<File>,
        caption: String,
        contentType: String,
        duration: String,
        tags: MutableList<String>,
        progressCallback: (Long, Long) -> Unit
    ): Boolean {
        Log.d(TAG, "Start uploading function")
        Log.d("uploadFeedToMongoDB", "caption $caption, content type $contentType, tags $tags")

        // Prepare list to hold MultipartBody.Part for files
        val fileParts = mutableListOf<MultipartBody.Part>()
        val thumbnailFileParts = mutableListOf<MultipartBody.Part>()

        // Iterate through each file and create corresponding MultipartBody.Part
        files.forEach { file ->
            val fileBytes = file.readBytes()
            val requestFile: RequestBody =
                ProgressRequestBody(file, "files/*".toMediaTypeOrNull()!!, progressCallback)
            val filePart: MultipartBody.Part =
                MultipartBody.Part.createFormData("files", file.name, requestFile)
            fileParts.add(filePart)
        }

        thumbnails.forEach { file ->
            val fileBytes = file.readBytes()
            val requestFile: RequestBody =
                ProgressRequestBody(file, "files/*".toMediaTypeOrNull()!!, progressCallback)
            val filePart: MultipartBody.Part =
                MultipartBody.Part.createFormData("feed_thumbnail", file.name, requestFile)
            thumbnailFileParts.add(filePart)
        }


        // Prepare tags as RequestBody parts
        val tagParts = tags.mapIndexed { index, tag ->
            tag.toRequestBody("text/plain".toMediaTypeOrNull())
        }.toTypedArray()

        // Prepare other RequestBody parts
        val contentPart: RequestBody = caption.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentTypePart: RequestBody =
            contentType.toRequestBody("text/plain".toMediaTypeOrNull())
        val durationPart: RequestBody = duration.toRequestBody("text/plain".toMediaTypeOrNull())

        return try {
            // Perform the API call using Retrofit
            val response = retrofitInstance.apiService.uploadMultipleVideoFilesFeed(
                contentPart,
                contentTypePart,
                durationPart,
                fileParts,
                thumbnailFileParts,
                tagParts,
            )

            if (response.isSuccessful) {
                // Handle successful response
                Log.i("uploadFeedToMongoDB", "Files upload successful")
                Log.i("uploadFeedToMongoDB", "Response: ${response.body()?.data}")
                true
            } else {
                // Handle unsuccessful response
                Log.i("uploadFeedToMongoDB", "Files upload failed ${response.message()}")
                Log.i("uploadFeedToMongoDB", "Files upload failed ${response.code()}")
                Log.i("uploadFeedToMongoDB", "Files upload failed ${response.body()?.message}")
                Log.i(
                    "uploadFeedToMongoDB",
                    "Error response body: ${response.errorBody()?.string()}"
                )
                false
            }
        } catch (e: Exception) {
            // Handle exceptions
            Log.d("uploadFeedToMongoDB", "Failed to upload files because: ${e.message}")
            false
        }
    }

    private suspend fun uploadFeedToMongoDB(
        file: File,
        caption: String,
        thumbnail: File,
        tags: MutableList<String>,
        duration: String,
        contentType: String,
        progressCallback: (Long, Long) -> Unit
    ): Boolean {


        Log.d(TAG, "Start uploading function")
        // Convert file content to bytes
        val fileBytes = file.readBytes()

        val requestFile: RequestBody =
            ProgressRequestBody(file, "files/*".toMediaTypeOrNull()!!, progressCallback)

        val requestFileThumbnail: RequestBody =
            ProgressRequestBody(
                thumbnail, "files/*".toMediaTypeOrNull()!!,
                progressCallback)

        val thumbnailFilePart: MultipartBody.Part =
            MultipartBody.Part.createFormData(
                "feed_thumbnail",
                thumbnail.name,
                requestFileThumbnail
            )

        val filePart: MultipartBody.Part =
            MultipartBody.Part.createFormData("files", file.name, requestFile)


        val content = caption


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
        val contentTypePart: RequestBody = contentType
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val durationPart: RequestBody = duration
            .toRequestBody("text/plain".toMediaTypeOrNull())

        return try {

            Log.d(
                TAG,
                "try send Content type $contentType duration $duration thumbnail ${thumbnail.path}"
            )
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

//        val requestFile: RequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), fileBytes)
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


    private suspend fun uploadMultipleImagesFeedToMongoDB(
        files: List<File>, // List of files to upload
        caption: String,
        contentType: String,
        duration: String,
        tags: MutableList<String>,
        progressCallback: (Long, Long) -> Unit
    ): Boolean {
        Log.d(TAG, "Start uploading function")
        Log.d("uploadFeedToMongoDB", "caption $caption, content type $contentType, tags $tags")

        // Prepare list to hold MultipartBody.Part for files
        val fileParts = mutableListOf<MultipartBody.Part>()

        // Iterate through each file and create corresponding MultipartBody.Part
        files.forEach { file ->
            val fileBytes = file.readBytes()
            val requestFile: RequestBody =
                ProgressRequestBody(file, "files/*".toMediaTypeOrNull()!!, progressCallback)
            val filePart: MultipartBody.Part =
                MultipartBody.Part.createFormData("files", file.name, requestFile)
            fileParts.add(filePart)
        }

        // Prepare tags as RequestBody parts
        val tagParts = tags.mapIndexed { index, tag ->
            tag.toRequestBody("text/plain".toMediaTypeOrNull())
        }.toTypedArray()

        // Prepare other RequestBody parts
        val contentPart: RequestBody = caption.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentTypePart: RequestBody =
            contentType.toRequestBody("text/plain".toMediaTypeOrNull())
        val durationPart: RequestBody = duration.toRequestBody("text/plain".toMediaTypeOrNull())

        return try {
            // Perform the API call using Retrofit
            val response = retrofitInstance.apiService.uploadMultipleFilesFeed(
                contentPart,
                contentTypePart,
                durationPart,
                fileParts,
                tagParts,
            )

            if (response.isSuccessful) {
                // Handle successful response
                Log.i("uploadFeedToMongoDB", "Files upload successful")
                Log.i("uploadFeedToMongoDB", "Response: ${response.body()?.data}")
                true
            } else {
                // Handle unsuccessful response
                Log.i("uploadFeedToMongoDB", "Files upload failed ${response.message()}")
                Log.i("uploadFeedToMongoDB", "Files upload failed ${response.code()}")
                Log.i("uploadFeedToMongoDB", "Files upload failed ${response.body()?.message}")
                Log.i(
                    "uploadFeedToMongoDB",
                    "Error response body: ${response.errorBody()?.string()}"
                )
                false
            }
        } catch (e: Exception) {
            // Handle exceptions
            Log.d("uploadFeedToMongoDB", "Failed to upload files because: ${e.message}")
            false
        }
    }

}
