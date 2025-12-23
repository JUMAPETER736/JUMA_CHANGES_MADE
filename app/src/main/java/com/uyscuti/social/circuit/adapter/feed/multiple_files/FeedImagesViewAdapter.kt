package com.uyscuti.social.circuit.adapter.feed.multiple_files

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.rajat.pdfviewer.PdfRendererView
import com.uyscuti.social.circuit.adapter.feed.OnMultipleImagesClickListener
import com.uyscuti.social.circuit.utils.commaSeparatedStringToList
import com.uyscuti.social.circuit.utils.feedutils.feedDisplayPdfFirstPage
import com.uyscuti.social.circuit.utils.feedutils.feedDownloadPdf
import com.uyscuti.social.circuit.utils.generateRandomFileName
import com.uyscuti.social.circuit.R
import com.uyscuti.social.network.api.response.allFeedRepostsPost.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "FeedImageViewAdapter"

class FeedImagesViewAdapter(
    private val imageList: List<String>?,
    val context: Context) :
    RecyclerView.Adapter<FeedImagesViewAdapter.ViewHolder>() {
    private var onMultipleImagesClickListener: OnMultipleImagesClickListener? = null
    fun setOnMultipleImagesClickListener(l: OnMultipleImagesClickListener) {
        onMultipleImagesClickListener = l
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feed_multiple_documents_only_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageItem = imageList?.get(position)
        if (imageItem != null) {
            holder.bind(imageItem)
        }
    }

    override fun getItemCount(): Int {
        return imageList?.size ?: -1
    }

    private var data: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post? = null


    fun setData(data: Post) {
        this.data = data
    }

    fun setDocumentList(documentList: MutableList<String>) {

    }

    fun setContext(it: Context) {

    }

    // Coroutine scope for background tasks
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //        private val feedThumbnail: ImageView = itemView.findViewById(R.id.feedThumbnail)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)
        private val pdfImageView: ImageView = itemView.findViewById(R.id.pdfImageView)
        private val documentIcon: ImageView = itemView.findViewById(R.id.documentIcon)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val countTextView: TextView = itemView.findViewById(R.id.textView)
        private val docName: TextView = itemView.findViewById(R.id.docName)
        private val numberOfPages: TextView = itemView.findViewById(R.id.docInfo)

        //        private val transparentView: View = itemView.findViewById(R.id.transparentView)
        private val pdfView: PdfRendererView = itemView.findViewById(R.id.pdfView)

        private val feedDocLayout: ConstraintLayout = itemView.findViewById(R.id.feedDocLayout)

        @SuppressLint("ResourceAsColor", "SetTextI18n")
        fun bind(imageItem: String) {

            Log.d(TAG, "bind: data $data")
            if (data != null) {
                val layoutParams = feedDocLayout.layoutParams


                val fileName = data!!.fileNames?.let { commaSeparatedStringToList(it.toString()) }
                val noOfPages = data!!.numberOfPages?.let { commaSeparatedStringToList(it.toString()) }
                val documentTypes = data!!.fileTypes?.let { commaSeparatedStringToList(it.toString()) }


                val documentType = documentTypes?.get(absoluteAdapterPosition)
                Log.d(TAG, "bind: document Type $documentType")
                if (documentType == "pdf") {

                    if (data!!.thumbnail.isNotEmpty()) {
                        Log.d(TAG, "bind: thumbnail ${data!!.thumbnail}")
                        Glide.with(pdfImageView.context)
                            .load(data!!.thumbnail[0].thumbnailUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)// Load the Bitmap
                            .into(pdfImageView)
                    } else {
                        val heightInDp = 300 // Change this value to the desired height in dp
                        val density = context.resources.displayMetrics.density
                        layoutParams.height = (heightInDp * density).toInt()

                        feedDocLayout.layoutParams = layoutParams

                        val pdfUrl = data!!.files[absoluteAdapterPosition].url
                        val localPdfFile = File(context.cacheDir, generateRandomFileName("pdf"))

                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                // Simulate a background task, e.g., network request or computation
                                feedDownloadPdf(pdfUrl, localPdfFile)
                            }
                            // Update UI with result on the main thread
                            feedDisplayPdfFirstPage(pdfImageView, localPdfFile)

                        }



                    }

                }
                else {
                    // Get the existing layout parameters
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    feedDocLayout.layoutParams = layoutParams

                    documentIcon.visibility = View.VISIBLE
                }
                docName.text = fileName?.get(absoluteAdapterPosition)
                numberOfPages.text = "${noOfPages?.get(absoluteAdapterPosition)} pages"
            } else {
                docName.text = "Unknown"
            }

            cardView.setOnClickListener {
                if (onMultipleImagesClickListener != null) {

                    onMultipleImagesClickListener?.multipleImagesClickListener()
                }
            }
            if (imageList?.size!! <= 2) {
                Log.d("TAG", "bind: more")
            } else if (imageList.size == 3) {

                val layoutParams = cardView.layoutParams
                val newWidthInPixels = 320  // for example, 300 pixels
                val newHeightInPixels = 180 // for example, 200 pixels
                when (absoluteAdapterPosition) {
                    0 -> {
                        layoutParams.width = newWidthInPixels
                        layoutParams.height = newHeightInPixels
                        cardView.layoutParams = layoutParams
                    }

                    1 -> {
                        layoutParams.width = newWidthInPixels
                        layoutParams.height = newHeightInPixels
                        cardView.layoutParams = layoutParams
                    }

                    2 -> {
                        layoutParams.width = 640
                        layoutParams.height = newHeightInPixels
                        cardView.layoutParams = layoutParams
                    }
                    // Add more cases as needed
                    else -> {
                        layoutParams.width = newWidthInPixels
                        layoutParams.height = newHeightInPixels
                        cardView.layoutParams = layoutParams
                    }
                }

            } else if (imageList.size > 4) {
                Log.d("TAG", "bind: size list > 4")
                val layoutParams = cardView.layoutParams
                val newWidthInPixels = 320  // for example, 300 pixels
                val newHeightInPixels = 185 // for example, 200 pixels

                when (absoluteAdapterPosition) {
                    0 -> {
                        layoutParams.width = newWidthInPixels
                        layoutParams.height = newHeightInPixels
                        cardView.layoutParams = layoutParams
                    }

                    1 -> {
                        layoutParams.width = newWidthInPixels
                        layoutParams.height = newHeightInPixels
                        cardView.layoutParams = layoutParams
                    }

                    2 -> {
                        layoutParams.width = newWidthInPixels
                        layoutParams.height = newHeightInPixels
                        cardView.layoutParams = layoutParams
                    }

                    3 -> {
                        Log.d("TAG", "bind: load 4th")

                        imageView2.visibility = View.VISIBLE
                        countTextView.visibility = View.VISIBLE

                        countTextView.text = "+ ${imageList.size - 4}"
                        layoutParams.width = newWidthInPixels
                        layoutParams.height = newHeightInPixels
                        cardView.layoutParams = layoutParams
                    }
                    // Add more cases as needed
                    else -> {
                        layoutParams.width = newWidthInPixels
                        layoutParams.height = newHeightInPixels
                        cardView.layoutParams = layoutParams
                    }
                }
            } else {
                // Specify new width and height programmatically
                val newWidthInPixels = 320  // for example, 300 pixels
                val newHeightInPixels = 180 // for example, 200 pixels

                // Get the existing layout parameters of the ImageView
                val params = cardView.layoutParams


                // Change the width and height to the specified sizes
                params.width = newWidthInPixels
                params.height = newHeightInPixels


                // Apply the changes to the ImageView
                cardView.layoutParams = params
            }



        }
    }

}