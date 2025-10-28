package com.uyscuti.social.circuit.adapter.feed.multiple_files

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleDocumentsDataClass

private const val TAG = "MultipleFeedDocAdapter"

class MultipleFeedDocAdapter(
    private var context: Context,
    private var documentList: MutableList<FeedMultipleDocumentsDataClass>, // Changed from Uri to FeedMultipleDocumentsDataClass
    private var documentListenerInterface: DocumentListenerInterface
) : RecyclerView.Adapter<MultipleFeedDocAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_multiple_documents_only_view_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {
        val documentData = documentList[position]

        Log.d(TAG, "Binding document at position $position: ${documentData.filename}")
        Log.d(TAG, "Document has thumbnail: ${documentData.documentThumbnailFilePath != null}")

        // Set click listener
        holder.itemView.setOnClickListener {
            documentListenerInterface.onDocumentClickListener(position, documentData)
        }

        // CRITICAL: Display the thumbnail that was generated and stored
        displayDocumentThumbnail(holder, documentData, position)

        // Set document title
        val titleWithoutExtension = documentData.filename.substringBeforeLast(".")


        // Set document info (pages, type, etc.)
        val documentInfo = buildString {
            append(documentData.documentType.uppercase())
            if (documentData.numberOfPages.isNotEmpty() && documentData.numberOfPages != "0" && documentData.numberOfPages != "1") {
                append(" • ${documentData.numberOfPages} pages")
            }
        }

    }

    private fun displayDocumentThumbnail(
        holder: Pager2ViewHolder,
        documentData: FeedMultipleDocumentsDataClass,
        position: Int
    ) {
        try {
            // Clear any previous styling


            // CRITICAL: Use the stored thumbnail
            if (documentData.documentThumbnailFilePath != null) {
                Log.d(TAG, "Displaying stored thumbnail for position $position: ${documentData.filename}")

            } else {
                Log.w(TAG, "No thumbnail found for position $position: ${documentData.filename}, using fallback")
                // Use fallback icon based on document type

            }

        } catch (e: Exception) {
            Log.e(TAG, "Error displaying thumbnail for position $position: ${documentData.filename}", e)

        }
    }

    private fun setFallbackIcon(imageView: ImageView, documentData: FeedMultipleDocumentsDataClass) {
        // Set fallback icon based on document type
        val iconRes = when (documentData.documentType.lowercase()) {
            "pdf" -> R.drawable.pdf_icon
            "word" -> R.drawable.word_icon
            "excel" -> R.drawable.excel_icon
            "powerpoint" -> R.drawable.powerpoint_icon
            "text" -> R.drawable.text_icon
            "csv" -> R.drawable.text_icon
            else -> R.drawable.documents
        }

        try {
            imageView.setImageResource(iconRes)
            imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            Log.d(TAG, "Set fallback icon for ${documentData.documentType}: ${documentData.filename}")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting fallback icon for: ${documentData.filename}", e)
            // Ultimate fallback - use a generic document icon
            imageView.setImageResource(android.R.drawable.ic_menu_agenda)
        }
    }

    // Method to verify thumbnail integrity across all items
    fun verifyThumbnailIntegrity() {
        documentList.forEachIndexed { index, doc ->
            val hasThumbnail = doc.documentThumbnailFilePath != null
            Log.d(TAG, "Adapter verification - Position $index (${doc.filename}) thumbnail status: $hasThumbnail")

            if (!hasThumbnail) {
                Log.w(TAG, "CRITICAL: Document at position $index (${doc.filename}) missing thumbnail in adapter")
            }
        }
    }

    // Method to refresh thumbnails if needed
    fun refreshThumbnails() {
        Log.d(TAG, "Refreshing thumbnails for ${documentList.size} documents")
        notifyDataSetChanged()
    }

    // Method to update the document list (useful when adding more documents)
    fun updateDocuments(newDocumentList: MutableList<FeedMultipleDocumentsDataClass>) {
        this.documentList = newDocumentList
        Log.d(TAG, "Updated document list with ${newDocumentList.size} documents")
        verifyThumbnailIntegrity()
        notifyDataSetChanged()
    }

    // Get document data at position
    fun getDocumentData(position: Int): FeedMultipleDocumentsDataClass {
        return documentList[position]
    }

    // Legacy method for backward compatibility (if needed)
    fun getDocumentUri(position: Int): Uri {
        return Uri.parse(documentList[position].uri as String?)
    }

    override fun getItemCount(): Int {
        return documentList.size
    }

    // Override these methods to ensure thumbnails are preserved during RecyclerView operations
    override fun onViewRecycled(holder: Pager2ViewHolder) {
        super.onViewRecycled(holder)
        // Clear the ImageView to prevent memory leaks but don't clear the stored thumbnail

    }

    override fun onViewAttachedToWindow(holder: Pager2ViewHolder) {
        super.onViewAttachedToWindow(holder)
        // Re-verify thumbnail when view is attached
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION && position < documentList.size) {
            val documentData = documentList[position]
            Log.d(TAG, "View attached at position $position, thumbnail available: ${documentData.documentThumbnailFilePath != null}")
        }
    }
}

interface DocumentListenerInterface {
    fun onDocumentClickListener(position: Int, documentData: FeedMultipleDocumentsDataClass)

    // Legacy method for backward compatibility
    fun onDocumentClickListener() {
        // Default implementation - calls the new method with dummy values
        onDocumentClickListener(-1, FeedMultipleDocumentsDataClass())
    }
}