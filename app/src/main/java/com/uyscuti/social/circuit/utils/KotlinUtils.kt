package com.uyscuti.social.circuit.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.os.Build
import java.io.Serializable

import android.net.ConnectivityManager
import android.net.NetworkCapabilities


import android.text.format.DateUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

import android.media.MediaMetadataRetriever
import android.util.Log
import java.io.IOException

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder


import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.DecimalFormat

import java.util.UUID
import kotlin.math.log10
import kotlin.math.pow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun generateRandomId(): String {
    return UUID.randomUUID().toString()
}
@RequiresApi(Build.VERSION_CODES.O)
fun generateMongoDBTimestamp(): String {
    val timestamp = OffsetDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

    return timestamp.format(formatter)
}

//fun deleteFile(filePath: String): Boolean {
//    val file = File(filePath)
//    return file.delete()
//}
fun getOutputFilePath(startName: String): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    val directory =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
    val absolutePath = File(
        directory,
        "vn"
    ) // Change "YourDirectoryNameHere" to your desired directory name within the Pictures folder

    if (!absolutePath.exists()) {
        absolutePath.mkdirs() // Create the directory if it doesn't exist
    }
    Log.d("LOG_TAG", "getOutputFilePath: ${absolutePath.absolutePath}")

    return "${absolutePath.absolutePath}/${startName}_$timestamp.mp3"
}

private fun getReadableFileSize(size: Long): String {
    if (size <= 0) {
        return "0"
    }
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
}

fun formatDuration(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    // Create a Formatter to format the time
    val formatter = Formatter(Locale.getDefault())
    return formatter.format("%02d:%02d", minutes, seconds).toString()
}

@SuppressLint("DefaultLocale")
fun formatFileSize(sizeBytes: Long): String {
    if (sizeBytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(sizeBytes.toDouble()) / log10(1024.0)).toInt()

    return String.format(
        "%.2f %s",
        sizeBytes / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}

fun isFileSizeGreaterThan2MB(sizeBytes: Long): Boolean {
    // If size is less than or equal to 2MB, return false
    return sizeBytes > 2 * 1024 * 1024
}

fun fileType(fileName: String): String {
    val extension = fileName.substringAfterLast(".", "Unknown")
    return extension
}

fun isFileExists(context: Context, uri: Uri): Boolean {
    return try {
        val documentId = DocumentsContract.getDocumentId(uri)
        val contentUri =
            DocumentsContract.buildDocumentUri("com.android.providers.media.documents", documentId)
        val cursor = context.contentResolver.query(contentUri, null, null, null, null)
        cursor?.use {
            it.moveToFirst()
            it.count > 0 // Check if the cursor contains any data
        } ?: false
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        false
    }
}

fun createMultipartBody(
    context: Context,
    uri: Uri,
    name: String,
    extension: String = "doc"
): MultipartBody.Part? {
    try {
        val file = getFileFromUri(context, uri, name, extension) ?: return null

        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//        return MultipartBody.Part.createFormData("docs", file.name, requestFile)
        return MultipartBody.Part.createFormData(name, file.name, requestFile)

    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        return null
    }
}

@Throws(FileNotFoundException::class)
fun getFileFromUri(context: Context, uri: Uri, fileType: String, extension: String = "doc"): File? {
    return try {
        // Check if we have permission to access this URI
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        val file = if (fileType == "gif") {
            File(context.cacheDir, "temp_file_${System.currentTimeMillis()}.gif")
        } else {
            File(context.cacheDir, "temp_file_${System.currentTimeMillis()}.$extension")
        }

        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (securityException: SecurityException) {
        Log.e("FileUtils", "Permission denied accessing URI: $uri", securityException)
        // Handle the security exception - inform user they need to select file differently
        null
    } catch (e: Exception) {
        Log.e("FileUtils", "Error reading file from URI: $uri", e)
        null
    }
}

object PathUtil {
    fun getPath(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            return it.getString(columnIndex)
        }
        return null
    }
}


suspend fun isURLAccessible(urlString: String): Boolean {
    val TAG = "isURLAccessible"
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000 // Adjust timeout as needed
            val responseCode = connection.responseCode
            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: IOException) {
            Log.e(TAG, "isURLAccessible: url not accessible ${e.message}")
            e.printStackTrace()
            false
        }
    }
}

object AudioDurationHelper {
    /**
     * Retrieves the duration of an audio file from the given URL.
     *
     * @param audioUrl The URL of the audio file.
     * @return The duration of the audio file in milliseconds, or -1 if an error occurred.
     */
    fun getAudioDuration(audioUrl: String): Long {
        val TAG = "getAudioDuration"
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(audioUrl, emptyMap())
            val durationString =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            return durationString?.toLongOrNull() ?: -1L
        } catch (e: IOException) {
            Log.e(TAG, "getAudioDuration: IOException ")
            e.printStackTrace()
        } finally {
//            Log.e(TAG, "getAudioDuration: finally exception ", )
            retriever.release()
        }

        return -1L
    }

    fun getLocalAudioDuration(filePath: String): Long {
        val file = File(filePath)
        if (!file.exists() || !file.canRead()) {
            Log.e("AudioDurationHelper", "File doesn't exist or can't be read: $filePath")
            return 0L
        }

        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e("AudioDurationHelper", "Error getting audio duration: ${e.message}")
            0L
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e("AudioDurationHelper", "Error releasing MediaMetadataRetriever: ${e.message}")
            }
        }
    }



    fun reverseFormattedDuration(durationString: String): Long {
        try {
            val parts = durationString.split(":")
            if (parts.size != 2) {
                throw IllegalArgumentException("Invalid duration format: $durationString")
            }
            val minutes = parts[0].toIntOrNull()
                ?: throw IllegalArgumentException("Invalid minutes: ${parts[0]}")
            val seconds = parts[1].toIntOrNull()
                ?: throw IllegalArgumentException("Invalid seconds: ${parts[1]}")
            return (minutes * 60 + seconds).toLong() * 1000
        } catch (e: IllegalArgumentException) {
            println("Error: ${e.message}")
            throw e
        }
    }


    @SuppressLint("DefaultLocale")
    fun getFormattedDuration(filePath: String): String {
        val audioDuration = getLocalAudioDuration(filePath)!!
        val minutes = (audioDuration / 1000) / 60
        val seconds = (audioDuration / 1000) % 60

        val durationString = String.format("%02d:%02d", minutes, seconds)
        return durationString
    }

    fun getVideoDuration(filePath: String): Long? {
        val file = File(filePath)
        if (!file.exists()) {
            // File does not exist
            return null
        }

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)

            // Extract video duration in milliseconds
            val durationString =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            return durationString?.toLong()
        } catch (e: Exception) {
            // Handle any exceptions
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return null
    }

}

object TimeUtilsLiveData {
    private const val ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    private val updateIntervalMillis = DateUtils.MINUTE_IN_MILLIS // Update every minute
    private val handler = Handler(Looper.getMainLooper())

    private val _formattedTimestampLiveData = MutableLiveData<String>()
    val formattedTimestampLiveData: LiveData<String>
        get() = _formattedTimestampLiveData

    fun startUpdatingTimestamp(timestamp: String) {
        updateTimestamp(timestamp)
        handler.postDelayed({
            startUpdatingTimestamp(timestamp)
        }, updateIntervalMillis)
    }

    private fun updateTimestamp(timestamp: String) {
        val formattedTimestamp = formatMongoTimestamp(timestamp)
        _formattedTimestampLiveData.value = formattedTimestamp
    }

    private fun formatMongoTimestamp(dateTimeString: String?): String {
        if (dateTimeString.isNullOrBlank()) return "now"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateTimeString)
            val now = Date()
            val diffInMillis = now.time - (date?.time ?: 0)
            val diffInSeconds = diffInMillis / 1000
            val diffInMinutes = diffInSeconds / 60
            val diffInHours = diffInMinutes / 60
            val diffInDays = diffInHours / 24
            val diffInWeeks = diffInDays / 7
            val diffInMonths = diffInDays / 30 // Approximate
            val diffInYears = diffInDays / 365 // Approximate

            when {
                diffInSeconds < 60 -> "now"
                diffInMinutes < 60 -> "${diffInMinutes}m"
                diffInHours < 24 -> "${diffInHours}h"
                diffInDays == 1L -> "1d"
                diffInDays < 7 -> "${diffInDays}d"
                diffInWeeks == 1L -> "1w"
                diffInWeeks < 4 -> "${diffInWeeks}w"
                diffInMonths == 1L -> "a month ago"
                diffInMonths < 12 -> "${diffInMonths}m"
                diffInYears == 1L -> "1y"
                else -> "${diffInYears}y"
            }
        } catch (e: Exception) {
            Log.w("CommentViewHolder", "Failed to format timestamp: $dateTimeString", e)
            "now"
        }
    }


}

object TimeUtils {
    fun formatMongoTimestamp(dateTimeString: String?): String {
        if (dateTimeString.isNullOrBlank()) return "now"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateTimeString)
            val now = Date()
            val diffInMillis = now.time - (date?.time ?: 0)
            val diffInSeconds = diffInMillis / 1000
            val diffInMinutes = diffInSeconds / 60
            val diffInHours = diffInMinutes / 60
            val diffInDays = diffInHours / 24
            val diffInWeeks = diffInDays / 7
            val diffInMonths = diffInDays / 30 // Approximate
            val diffInYears = diffInDays / 365 // Approximate

            when {
                diffInSeconds < 60 -> "now"
                diffInMinutes < 60 -> "${diffInMinutes}m"
                diffInHours < 24 -> "${diffInHours}h"
                diffInDays == 1L -> "1d"
                diffInDays < 7 -> "${diffInDays}d"
                diffInWeeks == 1L -> "1w"
                diffInWeeks < 4 -> "${diffInWeeks}w"
                diffInMonths == 1L -> "a month ago"
                diffInMonths < 12 -> "${diffInMonths}m"
                diffInYears == 1L -> "1y"
                else -> "${diffInYears}y"
            }
        } catch (e: Exception) {
            Log.w("TimeUtils", "Failed to format timestamp: $dateTimeString", e)
            "now"
        }
    }
}


fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkCapabilities = connectivityManager.activeNetwork ?: return false
    val activeNetwork =
        connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        // For other device how are able to connect with Ethernet
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

fun <T : Serializable> Activity.getSerializable(name: String, clazz: Class<T>): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getSerializableExtra(name, clazz)!!
    } else {
        intent.getSerializableExtra(name) as T
    }
}

inline fun <reified T : Serializable> Activity.getSerializableList(
    name: String,
    clazz: Class<T>
): ArrayList<T> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getSerializableExtra(name)?.let { serializable ->
            if (serializable is ArrayList<*> && serializable.isNotEmpty() && serializable[0] is T) {
                @Suppress("UNCHECKED_CAST")
                serializable as ArrayList<T>
            } else {
                throw ClassCastException("Invalid type for ArrayList<$clazz>")
            }
        } ?: ArrayList()
    } else {
        @Suppress("UNCHECKED_CAST")
        intent.getSerializableExtra(name) as? ArrayList<T> ?: ArrayList()
    }
}

fun removeDuplicateFollowers(list: List<ShortsEntityFollowList>): List<ShortsEntityFollowList> {
    val uniqueFollowersIds = HashSet<String>()
    val filteredList = mutableListOf<ShortsEntityFollowList>()

    for (item in list) {
        if (uniqueFollowersIds.add(item.followersId)) {
            filteredList.add(item)
        }
    }

    return filteredList
}

fun deleteFiled(filePath: String): Boolean {
    val file = File(filePath)
    return file.delete()
}

fun deleteFiles(filePaths: List<String>): Boolean {
    var allDeleted = true
    for (filePath in filePaths) {
        val file = File(filePath)
        val isDeleted = file.delete()
        if (!isDeleted) {
            allDeleted = false
            // Handle failure to delete file if needed
        }
    }
    return allDeleted
}

fun extractFileNameFromUrl(url: String): String {
    // Assuming the URL format is consistent and contains the filename at the end
    val parts = url.split("/")
    val encodedFileName = parts.last()

    // Decode the URL-encoded filename

    return URLDecoder.decode(encodedFileName, "UTF-8")
}

fun getFileNameFromLocalPath(filePath: String): String {
    val file = File(filePath)
    return file.name
}

fun deleteExistingFiles(filePaths: List<String>): Boolean {
    var allDeleted = true
    for (filePath in filePaths) {
        val file = File(filePath)
        if (file.exists()) {
            val isDeleted = file.delete()
            if (!isDeleted) {
                allDeleted = false
                // Handle failure to delete file if needed
            }
        }
    }
    return allDeleted
}

fun getFileDuration(filePath: String): Long? {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(filePath)
        val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationString?.toLong()
    } catch (e: Exception) {
        // Handle any exceptions, such as invalid file path or unsupported format
        e.printStackTrace()
    } finally {
        retriever.release()
    }
    return null
}


fun extractThumbnailFromVideo(filePath: String): Pair<Boolean, Bitmap?> {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(filePath)
        // Extracting the thumbnail at the first frame
        val bitmap = retriever.getFrameAtTime(0)
        return Pair(true, bitmap)
    } catch (e: Exception) {
        // Handle any exceptions, such as invalid file path or unsupported format
        e.printStackTrace()
    } finally {
        retriever.release()
    }
    return Pair(false, null)
}


fun uriToFile(context: Context, uri: Uri, extension: String): File? {
    val contentResolver: ContentResolver = context.contentResolver

    // Create a temporary file to copy the data
    val tempFile = createTempFile(context, extension)

    try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

    return tempFile
}


private fun createTempFile(context: Context, extension: String): File {
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    return File.createTempFile(
        generateRandomFileDocName(),
        ".${extension}",
        storageDir
    )

//    return File.createTempFile(
//        "temp_doc_file",
//        ".${extension}",
//        storageDir
//    )
}

fun listToCommaSeparatedString(list: List<String>): String {
    return list.joinToString(separator = ",")
}

fun commaSeparatedStringToList(string: String): List<String> {
    return string.split(",")
}

fun containsSubstring(list: List<String>, substring: String): Boolean {
    return list.any { it.contains(substring) }
}

fun filterStringsContainingSubstring(list: List<String>, substring: String): List<String> {
    return list.filter { it.contains(substring) }
}

fun generateRandomFileName(extension: String = "png"): String {
    val uuid = UUID.randomUUID().toString()
    return "$uuid.$extension"
}

fun generateRandomFileDocName(): String {
    val uuid = UUID.randomUUID().toString()
    return uuid
}

class KotlinUtils {

    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return file.delete()
    }
}

const val TAG = "uriToFile2"

fun uriToFile3(context: Context, uri: Uri, extension: String): File? {
    Log.d(TAG, "uriToFile2: extension $extension uri $uri")
    val contentResolver: ContentResolver = context.contentResolver

    // Create a temporary file to copy the data
    val tempFile = createTempFile2(context, extension)

    try {
        // Use ContentResolver to open an input stream for the URI
        contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: run {
            Log.e(TAG, "Failed to open input stream for URI: $uri")
            return null
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error while converting URI to file: ${e.message}")
        e.printStackTrace()
        return null
    }

    return tempFile
}

fun logUriDetails(uri: Uri) {
    val scheme = uri.scheme
    val path = uri.path
    Log.d("URI_Details", "Scheme: $scheme, Path:  $path")
}

fun uriToFile2(context: Context, uri: Uri, extension: String): File? {
    Log.d(TAG, "uriToFile2: extension $extension uri $uri")
    val contentResolver: ContentResolver = context.contentResolver
    logUriDetails(uri)
    // Create a temporary file to copy the data
    val tempFile = createTempFile2(context, extension)

    if(tempFile.isFile) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: run {
                Log.e(TAG, "Failed to open input stream for URI: $uri")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while converting URI to file: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    return tempFile
}
fun convertPdfUriToFile(context: Context, uri: Uri): File? {
    val contentResolver: ContentResolver = context.contentResolver
    val tempFile = createPdfTempFile(context)

    try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                copyStream(inputStream, outputStream)
            }
        } ?: run {
            Log.e(TAG, "Failed to open input stream for URI: $uri")
            return null
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error while converting URI to file: ${e.message}")
        e.printStackTrace()
        return null
    }

    return tempFile
}
private fun createPdfTempFile(context: Context): File {
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        ?: context.filesDir // Fallback to internal storage if external directory is not available

    // Ensure the directory exists
    if (!storageDir.exists()) {
        storageDir.mkdirs()
    }

    return File.createTempFile(
        "temp_pdf_file", // Prefix for the file name
        ".pdf", // Suffix for the file name
        storageDir // Directory where the file will be created
    )
}

private fun copyStream(inputStream: InputStream, outputStream: OutputStream) {
    val buffer = ByteArray(1024)
    var bytesRead: Int
    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        outputStream.write(buffer, 0, bytesRead)
    }
}

private fun createTempFile2(context: Context, extension: String): File {
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        ?: context.filesDir // Fallback to internal storage if external directory is not available

    // Ensure the directory exists
    if (!storageDir.exists()) {
        storageDir.mkdirs()
    }

    return File.createTempFile(
        generateRandomFileDocName2(),
        ".${extension}",
        storageDir
    )
}

private fun generateRandomFileDocName2(): String {
    return "temp_${System.currentTimeMillis()}"
}



fun <T> createRequestBodies(
    gson: Gson,
    items: List<T>,
    logTag: String = "createRequestBodies"
): List<RequestBody> {
    return items.map { item ->
        val json = gson.toJson(item)
        Log.d(logTag, "createRequestBodies json: $json")
        json.toRequestBody("application/json".toMediaType())
    }
}


fun convertPdfUriToFileCopilot(context: Context, pdfUri: Uri): File {
    val inputStream: InputStream? = context.contentResolver.openInputStream(pdfUri)
    val file = File(context.cacheDir, "tempFile.pdf")
    inputStream?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return file
}

fun getFileSizeFromUri(context: Context, uri: Uri): Long? {
    val contentResolver: ContentResolver = context.contentResolver
    var fileSize: Long? = null

    try {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1 && cursor.moveToFirst()) {
                fileSize = cursor.getLong(sizeIndex)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return fileSize
}