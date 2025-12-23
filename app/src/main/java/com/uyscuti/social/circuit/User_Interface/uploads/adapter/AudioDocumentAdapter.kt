package com.uyscuti.social.circuit.User_Interface.uploads.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import java.io.File
import java.util.Locale


class AudioDocumentAdapter(private val context: Context, private val documentList: List<String>, private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<AudioDocumentAdapter.DocumentViewHolder>() {

    class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val documentIconImageView: ImageView = itemView.findViewById(R.id.documentIconImageView)
        val documentNameTextView: TextView = itemView.findViewById(R.id.documentNameTextView)
        val documentSizeTextView: TextView = itemView.findViewById(R.id.documentSizeTextView)


        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.audio_item_layout, parent, false)
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val documentPath = documentList[position]

        val documentName = getDocumentName(documentPath)
        val documentSize = getDocumentSize(documentPath)
        val extension = getDocumentExtension(documentPath)

        holder.documentNameTextView.text = documentName
        holder.documentSizeTextView.text = documentSize

        // Set the document icon based on the file extension
        holder.documentIconImageView.setImageResource(getDocumentIconResource(extension))


        holder.itemView.setOnClickListener {
            // Handle document item click event
            // You can open the document or perform other actions here
            onItemClick(documentPath)
        }
    }

    private fun getDocumentExtension(documentPath: String): String {
        val lastDotIndex = documentPath.lastIndexOf('.')
        return if (lastDotIndex != -1) {
            documentPath.substring(lastDotIndex + 1)
        } else {
            ""
        }
    }

    private fun getDocumentIconResource(extension: String): Int {
        return when (extension.toLowerCase(Locale.ROOT)) {
            "pdf" -> R.drawable.pdf_document_svgrepo_com
            "doc", "docx" -> R.drawable.word_document_svgrepo_com
            "txt" -> R.drawable.txt_document_svgrepo_com
            // Add cases for other document types
            else -> R.drawable.gdoc_document_svgrepo_com // Default icon for unknown types
        }
    }

    override fun getItemCount(): Int {
        return documentList.size
    }

    private fun getDocumentName(documentPath: String): String {
        return File(documentPath).name
    }

    private fun getDocumentSize(documentPath: String): String {
        val fileSize = File(documentPath).length()
        return android.text.format.Formatter.formatFileSize(context, fileSize)
    }
}

