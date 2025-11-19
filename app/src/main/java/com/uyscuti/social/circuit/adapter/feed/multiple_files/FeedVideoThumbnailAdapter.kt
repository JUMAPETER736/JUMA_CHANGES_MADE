package com.uyscuti.social.circuit.adapter.feed.multiple_files

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleDocumentsDataClass
import com.uyscuti.social.circuit.User_Interface.fragments.feed.UploadFeedActivity
import com.bumptech.glide.Glide
import androidx.core.graphics.createBitmap
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream

class FeedVideoThumbnailAdapter(
    private val thumbnails: List<Bitmap>,
    private val clickListener: ThumbnailClickListener
) : RecyclerView.Adapter<FeedVideoThumbnailAdapter.ThumbnailViewHolder>() {

    private var itemWidth: Int = 0
    private var videoDetails: FeedMultipleVideos? = null
    private var selectedPosition: Int = -1 // Track the selected thumbnail position

    @SuppressLint("NotifyDataSetChanged")
    fun setItemWidth(width: Int) {
        this.itemWidth = width
        notifyDataSetChanged()
    }

    fun setVideoDetails(details: FeedMultipleVideos) {
        this.videoDetails = details
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_thumbnail, parent, false)
        return ThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        val thumbnail = thumbnails[position]

        // Set the calculated width if available
        if (itemWidth > 0) {
            val layoutParams = holder.itemView.layoutParams
            layoutParams.width = itemWidth
            holder.itemView.layoutParams = layoutParams
        }

        // Load the thumbnail image
        Glide.with(holder.itemView.context)
            .load(thumbnail)
            .centerCrop()
            .into(holder.thumbnailImageView)

        // Update selection state
        holder.selectionBorder.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE
        holder.selectionOverlay.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE

        // Set click listener
        holder.itemView.setOnClickListener {
            videoDetails?.let { details ->
                // Update selected position
                val previousPosition = selectedPosition
                selectedPosition = holder.adapterPosition
                // Notify to update previous and current items
                if (previousPosition != -1) notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                clickListener.onThumbnailClick(thumbnail, details)
            }
        }

        // Set long click listener
        holder.itemView.setOnLongClickListener {
            videoDetails?.let { details ->
                clickListener.onThumbnailLongClick(thumbnail, details, position)
            }
            true
        }
    }

    override fun getItemCount(): Int = thumbnails.size

    class ThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)
        val selectionBorder: View = itemView.findViewById(R.id.selectionBorder)
        val selectionOverlay: View = itemView.findViewById(R.id.selectionOverlay)
    }

    interface ThumbnailClickListener {
        fun onThumbnailClick(thumbnail: Bitmap, videoDetails: FeedMultipleVideos)
        fun onThumbnailLongClick(thumbnail: Bitmap, videoDetails: FeedMultipleVideos, position: Int)
        fun showAttachmentDialog()
        fun backFromShortsUpload()
        fun cancelShortsUpload()
        fun saveBitmapToCache2(context: Context, bitmap: Bitmap): String
        fun saveBitmapToFile(bitmap: Bitmap, context: Context): File
        fun handleDocumentUriToUploadReturn(uri: Uri): FeedMultipleDocumentsDataClass
    }
}

// Load bitmap from URI
fun loadBitmapFromUri(
    context: Context,
    uri: Uri?): Bitmap? {
    if (uri == null) return null
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        Log.e("FeedVideoThumbnailAdapter", "Error loading bitmap: ${e.message}")
        null
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

// Retrieve first page and save as image
fun retrieveFirstPageAndSaveAsImage(
    activity: UploadFeedActivity,
    uri: Uri): String? {
    return try {
        val bitmap = retrieveFirstPageAsBitmap(activity, uri)
        bitmap?.let {
            saveBitmapToCache(activity, it)
        }
    } catch (e: Exception) {
        Log.e("FeedVideoThumbnailAdapter", "Error retrieving and saving first page: ${e.message}")
        null
    }
}