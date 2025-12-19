package com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.sharedmodule.model.feed.multiple_files.FeedMultipleDocumentsDataClass
import com.uyscuti.sharedmodule.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.UploadFeedActivity
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream

class FeedVideoThumbnailAdapter(
    private val thumbnails: List<Bitmap>,
    private val clickListener: ThumbnailClickListener
) : RecyclerView.Adapter<FeedVideoThumbnailAdapter.VideoThumbnailViewHolder>() {

    private var videoDetails: FeedMultipleVideos? = null
    private var itemWidth: Int = 0


    fun setVideoDetails(videoDetails: FeedMultipleVideos) {
        this.videoDetails = videoDetails
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItemWidth(width: Int) {
        this.itemWidth = width
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_thumbnail, parent, false)
        return VideoThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoThumbnailViewHolder, position: Int) {
        val thumbnail = thumbnails[position]
        holder.thumbnailImageView.setImageBitmap(thumbnail)
        holder.itemView.setOnClickListener {
            videoDetails?.let {
                clickListener.onThumbnailClick(thumbnail, it)
            }
        }
    }

    // Save bitmap to cache directory
    fun saveBitmapToCache(
        context: Context,
        bitmap: Bitmap): String? {
        return try {
            val cacheDir = context.cacheDir
            val file = File(cacheDir, "thumbnail_${System.currentTimeMillis()}.png")

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            file.absolutePath
        } catch (e: Exception) {
            Log.e("FeedVideoThumbnailAdapter", "Error saving bitmap to cache: ${e.message}")
            null
        }
    }

    // Get number of pages from DOC file
    fun getNumberOfPagesFromUriForDoc(
        context: Context,
        uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = HWPFDocument(inputStream)
                val range = document.range
                // Estimate pages based on text length (rough approximation)
                val textLength = range.text().length
                val estimatedPages = (textLength / 2000) + 1 // Rough estimate: 2000 chars per page
                document.close()
                estimatedPages
            } ?: 0
        } catch (e: Exception) {
            Log.e("FeedVideoThumbnailAdapter", "Error reading DOC file: ${e.message}")
            1 // Default to 1 page if error occurs
        }
    }

    // Get number of pages from DOCX file
    fun getNumberOfPagesFromUriForDocx(
        context: Context,
        uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = XWPFDocument(inputStream)
                val properties = document.properties
                val extendedProperties = properties.extendedProperties

                // Try to get page count from document properties
                val pageCount = extendedProperties?.underlyingProperties?.pages ?: 0

                document.close()

                if (pageCount > 0) pageCount else {
                    // If page count not available, estimate based on paragraphs
                    val paragraphs = document.paragraphs.size
                    (paragraphs / 10) + 1 // Rough estimate: 10 paragraphs per page
                }
            } ?: 1
        } catch (e: Exception) {
            Log.e("FeedVideoThumbnailAdapter", "Error reading DOCX file: ${e.message}")
            1 // Default to 1 page if error occurs
        }
    }

    // Get number of pages from PDF file
    fun getNumberOfPagesFromUriForPDF(
        activity: UploadFeedActivity,
        uri: Uri): Int {
        return try {
            val parcelFileDescriptor = activity.contentResolver.openFileDescriptor(uri, "r")
            parcelFileDescriptor?.use { pfd ->
                val pdfRenderer = PdfRenderer(pfd)
                val pageCount = pdfRenderer.pageCount
                pdfRenderer.close()
                pageCount
            } ?: 0
        } catch (e: Exception) {
            Log.e("FeedVideoThumbnailAdapter", "Error reading PDF file: ${e.message}")
            0
        }
    }

    // Retrieve first page as bitmap from PDF
    fun retrieveFirstPageAsBitmap(
        activity: UploadFeedActivity,
        uri: Uri): Bitmap? {
        return try {
            val parcelFileDescriptor = activity.contentResolver.openFileDescriptor(uri, "r")
            parcelFileDescriptor?.use { pfd ->
                val pdfRenderer = PdfRenderer(pfd)
                if (pdfRenderer.pageCount > 0) {
                    val page = pdfRenderer.openPage(0)
                    val bitmap = createBitmap(page.width, page.height)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    pdfRenderer.close()
                    bitmap
                } else {
                    pdfRenderer.close()
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FeedVideoThumbnailAdapter", "Error retrieving first page as bitmap: ${e.message}")
            null
        }
    }


    override fun getItemCount(): Int {
        return thumbnails.size
    }

    inner class VideoThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)
    }

    interface ThumbnailClickListener {
        fun onThumbnailClick(thumbnail: Bitmap, videoDetails: FeedMultipleVideos)
        fun showAttachmentDialog()
        fun backFromShortsUpload()
        fun cancelShortsUpload()
        fun saveBitmapToCache2(context: Context, bitmap: Bitmap): String
        fun saveBitmapToFile(bitmap: Bitmap, context: Context): File
        fun handleDocumentUriToUploadReturn(uri: Uri): FeedMultipleDocumentsDataClass
    }
}


// Fixed function - Returns Bitmap? instead of Unit
private fun loadBitmapFromUri(context: Context, uri: Uri?): Bitmap? {
    if (uri == null) return null
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        Log.e("YourActivity", "Error loading bitmap: ${e.message}")
        null
    }
}

fun saveBitmapToCache(context: Context, bitmap: Bitmap): Unit? {
    // Example: implement saving logic here
    return null
}

fun getNumberOfPagesFromUriForDoc(uri: Uri): Int {
    // TODO: Implement DOC file page counting logic
    return 0
}

fun getNumberOfPagesFromUriForDocx(uri: Uri): Int {
    // TODO: Implement DOCX file page counting logic
    return 0
}

fun getNumberOfPagesFromUriForPDF(activity: Activity, uri: Uri): Int {
    // TODO: Implement PDF page counting logic using PDF renderer
    return 0
}

fun retrieveFirstPageAsBitmap(activity: Activity, uri: Uri): Bitmap? {
    // TODO: Implement retrieval of first page as bitmap
    return null
}

fun retrieveFirstPageAndSaveAsImage(activity: Activity, uri: Uri) {
    // TODO: Implement logic to save first page of document as image
}
