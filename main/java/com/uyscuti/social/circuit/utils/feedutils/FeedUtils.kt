package com.uyscuti.social.circuit.utils.feedutils

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.uyscuti.social.circuit.adapter.feed.multiple_files.UriTypeAdapter
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption


fun removeOuterBrackets(input: String): String {
    // Ensure the string starts with '[' and ends with ']'
    return if (input.startsWith("[") && input.endsWith("]")) {
        // Remove the first and last characters (i.e., '[' and ']')
        input.substring(1, input.length - 1).trim()
    } else {
        // Return the original string if it doesn't have the expected format
        input
    }
}

fun feedDownloadPdf(url: String, file: File) {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    client.newCall(request).execute().use { response ->
        response.body?.byteStream()?.let { inputStream ->
            FileOutputStream(file).use { outputStream ->
                copyStream(inputStream, outputStream)
            }
        }
    }
}

fun copyStream(input: InputStream, output: OutputStream) {
    val buffer = ByteArray(1024)
    var bytesRead: Int
    while (input.read(buffer).also { bytesRead = it } != -1) {
        output.write(buffer, 0, bytesRead)
    }
}

fun renderFirstPage(pdfFile: File): Bitmap? {
    val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
    val pdfRenderer = PdfRenderer(fileDescriptor)
    val pageCount = pdfRenderer.pageCount

    if (pageCount > 0) {
        val page = pdfRenderer.openPage(0) // Open the first page
        val width = page.width
        val height = page.height

        // Create a bitmap with the dimensions of the page
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        pdfRenderer.close()
        fileDescriptor.close()

        return bitmap
    }
    return null
}

fun feedDisplayPdfFirstPage(imageView: ImageView, pdfFile: File) {
    // Render the first page of the PDF to a Bitmap
    val bitmap = renderFirstPage(pdfFile)

    // Use Glide to load the Bitmap into the ImageView
    bitmap?.let {
        Glide.with(imageView.context)
            .load(it)
            .diskCacheStrategy(DiskCacheStrategy.ALL)// Load the Bitmap
            .into(imageView)  // Set it into the ImageView
    }
}
//fun feedDisplayPdfFirstPage(imageView: ImageView, pdfFile: File) {
//    val bitmap = renderFirstPage(pdfFile)
//    bitmap?.let {
//        imageView.setImageBitmap(it)
//    }
//}

fun feedRemoveTextStartingWithHash(inputText: String): String {
    // Define a regex pattern to match text starting with #
    val cleanedText = inputText.replace(Regex("#\\S+"), "")

    // Handle extra spaces or new lines
    return cleanedText
        .replace("\\s+".toRegex(), " ") // Replace multiple spaces/new lines with a single space
        .trim() // Remove leading/trailing spaces
}

fun isVideoUrl(url: String): Boolean {
    val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".avi", ".mov")
    return videoExtensions.any { url.endsWith(it, ignoreCase = true) }
}

private fun createGson(): Gson {
    return GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
        .create()
}
fun deserializeFeedUploadDataList(json: String): List<MixedFeedUploadDataClass>? {

    Log.d("deserializeFeedUploadDataList", "deserializeFeedUploadDataList: inside deserialize function")

    try {
        val gson = createGson()
        val type = object : TypeToken<List<MixedFeedUploadDataClass>>() {}.type
        return gson.fromJson(json, type)
    }catch (e: Exception) {
        Log.e("deserializeFeedUploadDataList", "deserializeFeedUploadDataList: ${e.message}", )
        return null
    }

}

fun getFileExtension(filePath: String): String {
    return filePath.substringAfterLast('.', "")
}

// Function to create a temporary file from an existing file
//@Throws(IOException::class)
//fun createTempFileFromFile(originalFile: File, fileName:String, fileExtension: String): File {
//    // Generate a temporary file with a prefix and suffix based on the original file's name
//    val tempFileName = fileName
//
//    Log.d("createTempFileFromFile", "createTempFileFromFile: file name $fileName....file ext $fileExtension")
//    val sanitizedFileExtension = fileExtension.takeIf { it.startsWith(".") } ?: ".$fileExtension"
//
//    val tempFile = File.createTempFile(tempFileName, sanitizedFileExtension)
//    Files.copy(originalFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
//    return tempFile
//}


//@Throws(IOException::class)
//fun createTempFileFromFile(originalFile: File, fileName: String, fileExtension: String): File {
//    // Ensure file extension starts with a dot
//    val sanitizedFileExtension =
//        if (fileExtension.startsWith(".")) fileExtension else ".$fileExtension"
//
//    // Create a temporary directory
//    val tempDir = File.createTempFile("tempDir", "").apply { delete(); mkdir() }
//
//    // Construct the temporary file path
//    val tempFile = File(tempDir, "$fileName$sanitizedFileExtension")
//
//    // Copy content from the original file to the temporary file
//    Files.copy(originalFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
//
//    return tempFile
//
//}

@Throws(IOException::class)
fun createTempFileFromFile(originalFile: File, fileName: String, fileExtension: String): File {
    // Ensure file extension starts with a dot
    val sanitizedFileExtension = if (fileExtension.startsWith(".")) fileExtension else ".$fileExtension"

    // Create a temporary directory
    val tempDir: Path = Files.createTempDirectory("tempDir")

    // Construct the temporary file path
    val tempFile = File(tempDir.toFile(), "$fileName$sanitizedFileExtension")

    // Copy content from the original file to the temporary file
    Files.copy(originalFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

    return tempFile
}

// Later, you can manually delete the temporary files if needed
//fun deleteTemporaryFiles(tempFilePaths: List<String>) {
//    tempFilePaths.forEach { path ->
//        File(path).delete()
//    }
//}

fun deleteTemporaryFiles(tempFilePaths: List<String>) {
    tempFilePaths.forEach { path ->
        val file = File(path)
        if (file.delete()) {
            println("Successfully deleted file: $path")
        } else {
            println("Failed to delete file: $path")
        }
    }
}
class FeedUtils {


}