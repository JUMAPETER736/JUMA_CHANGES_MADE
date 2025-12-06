@file:JvmName("FeedDocumentsAdapterKt")

package com.uyscuti.social.circuit.adapter.feed.multiple_files

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.uyscuti.social.circuit.utils.commaSeparatedStringToList
import com.uyscuti.social.circuit.utils.feedutils.feedDisplayPdfFirstPage
import com.uyscuti.social.circuit.utils.feedutils.feedDownloadPdf
import com.uyscuti.social.circuit.utils.generateRandomFileName
import com.uyscuti.social.circuit.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "FeedImageViewAdapter"
class FeedDocumentsViewAdapter(

    imageList: MutableList<String>,
    context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val documentList = ArrayList<String>()

    private var feedDocumentPostData: com.uyscuti.social.network.api.response.posts.Post? = null
    private lateinit var context: Context

    fun setContext(context: Context) {
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SimpleViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return documentList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SimpleViewHolder).onBind(documentList[position])
    }

    fun setDocumentList(documentList: List<String>) {
        this.documentList.addAll(documentList)
    }

    fun setData(data: com.uyscuti.social.network.api.response.posts.Post) {
        this.feedDocumentPostData = data
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    inner class SimpleViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.feed_multiple_documents_only_view_item, parent, false)
    ) {

        private val documentName: TextView = itemView.findViewById(R.id.documentName)
        private val numberOfPages: TextView = itemView.findViewById(R.id.numberOfPages)
        private val pdfImageView: ImageView = itemView.findViewById(R.id.pdfImageView)
        private val documentIcon: ImageView = itemView.findViewById(R.id.documentIcon)

        @SuppressLint("SetTextI18n")
        fun onBind(textToDisplay: String) {

            if (feedDocumentPostData != null) {
                val fileName = feedDocumentPostData!!.fileNames?.let { commaSeparatedStringToList(it.toString()) }
                val noOfPages = feedDocumentPostData!!.numberOfPages?.let { commaSeparatedStringToList(it.toString()) }
                val documentTypes = feedDocumentPostData!!.fileTypes?.let { commaSeparatedStringToList(it.toString()) }

                documentName.text = fileName?.get(absoluteAdapterPosition)
                numberOfPages.text = "${noOfPages?.get(absoluteAdapterPosition)} pages"
                val documentType = documentTypes?.get(absoluteAdapterPosition)

                if (documentType == "pdf") {
                    pdfImageView.visibility = View.VISIBLE
                    if (feedDocumentPostData!!.thumbnail.isNotEmpty()) {
                        Log.d(TAG, "bind: thumbnail ${feedDocumentPostData!!.thumbnail}")
                        Glide.with(pdfImageView.context)
                            .load(feedDocumentPostData!!.thumbnail[0].thumbnailUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)// Load the Bitmap
                            .into(pdfImageView)
                    } else {
                        val pdfUrl = feedDocumentPostData!!.files[absoluteAdapterPosition].url
                        val localPdfFile = File(context.cacheDir, generateRandomFileName("pdf"))

                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                // Simulate a background task, e.g., network request or computation
                                feedDownloadPdf(pdfUrl, localPdfFile)
                            }
                            // Update UI with result on the main thread
                            feedDisplayPdfFirstPage(pdfImageView, localPdfFile)
                        }
                        documentIcon.visibility = View.GONE
                    }
                } else if (documentType == "doc") {
                    if (feedDocumentPostData!!.thumbnail.isNotEmpty()) {
                        Log.d(TAG, "bind: thumbnail ${feedDocumentPostData!!.thumbnail}")
                        Glide.with(pdfImageView.context)
                            .load(feedDocumentPostData!!.thumbnail[0].thumbnailUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)// Load the Bitmap
                            .into(pdfImageView)
                    } else {
                        val pdfUrl = feedDocumentPostData!!.files[absoluteAdapterPosition].url
                        val localPdfFile = File(context.cacheDir, generateRandomFileName("pdf"))

                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                // Simulate a background task, e.g., network request or computation
                                feedDownloadPdf(pdfUrl, localPdfFile)
                            }
                            // Update UI with result on the main thread
                            feedDisplayPdfFirstPage(pdfImageView, localPdfFile)
                        }

                        pdfImageView.visibility = View.VISIBLE
                        documentIcon.visibility = View.GONE
                    }
                }
            }
        }
    }
}
