package com.uyscuti.social.circuit.adapter.feed.multiple_files

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.circuit.adapter.feed.MultipleImagesListener
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedFilesClass
import com.uyscuti.social.circuit.R
import java.io.File

private const val TAG = "MixedFilesUploadAdapter"

class MixedFilesUploadAdapter(
    private var context: Context,
    private var multipleImagesListener: MultipleImagesListener,
    private var multipleVideosListener: MultipleVideosListener,
    private var multipleDocumentsListener: MultipleFeedDocAdapter? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{


    private var mixedFileSize = 0
    private var images: ArrayList<String> = ArrayList()
    private var videos: ArrayList<FeedMultipleVideos> = ArrayList()

    private var mixedFiles: MixedFeedFilesClass? = null
    private var type: String = "image"


    private val viewTypeVideo = 0
    private val viewTypeImage = 1
    private var documents: ArrayList<String> = ArrayList()
    private val viewTypeDocument = 2



    @SuppressLint("NotifyDataSetChanged")
    fun setType(type: String) {
        this.type = type
        notifyDataSetChanged()
    }

    fun setSize(size: Int) {
        mixedFileSize = size
    }
    fun addMixedFiles(mixedFile: MixedFeedFilesClass)
    {
        mixedFiles = mixedFile
    }

    fun addImages(imageList: List<String>) {
        val startPosition = images.size
        images.addAll(imageList)
        notifyItemRangeInserted(startPosition, imageList.size)
    }

    fun addVideos(videoList: List<FeedMultipleVideos>) {
        val startPosition = images.size + videos.size
        videos.addAll(videoList)
        notifyItemRangeInserted(startPosition, videoList.size)
    }



    override fun getItemViewType(position: Int): Int {

        return if (position < images.size){
            viewTypeImage
        } else {
            viewTypeVideo
        }


    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.img)

        fun onBind(imagesList: String) {
            Glide.with(context)
                .load(imagesList)
                .into(imageView)
        }

        init {
            imageView.setOnClickListener {
                val position = absoluteAdapterPosition
                Log.d(TAG, "Image clicked at position: $position")
                multipleImagesListener.onImageClick()
            }
        }
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.img)

        fun onBind(videos: FeedMultipleVideos) {

            Log.d(TAG, "onBind: $mixedFileSize $absoluteAdapterPosition")
            try {
                val videoData = videos


                multipleVideosListener.onVideoDisplay(videos)
                if (videoData.thumbnail != null) {
                    Glide.with(context)
                        .load(videoData.thumbnail)
                        .into(thumbnail)
                } else {
                    Glide.with(context)
                        .load(Uri.fromFile(File(videos.videoPath)))
                        .into(thumbnail)
                }


                thumbnail.setOnClickListener {
                    multipleVideosListener.onVideoClick()

                }
                } catch (e: Exception) {
                Log.e(TAG, "onBind: ${e.message}", )
                e.printStackTrace()
            }
            
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            viewTypeImage -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.multiple_feed_images_item, parent, false)
                ImageViewHolder(view)
            }

            viewTypeVideo -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.multiple_feed_images_item, parent, false)
                VideoViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageViewHolder -> {
                holder.onBind(images[position])
            }
            is VideoViewHolder -> {
                val videoPosition = position - images.size
                if (videoPosition in videos.indices) {
                    holder.onBind(videos[videoPosition])
                }
            }
        }
    }


    fun getVideoDetails(): ArrayList<FeedMultipleVideos> {
        return videos
    }



    fun getVideoDetails(position: Int): FeedMultipleVideos? {
        val videoPosition = position - images.size
        return if (videoPosition in videos.indices) {
            videos[videoPosition]
        } else {
            null
        }
    }

    fun updateSelectedVideo(position: Int, feedVideo: FeedMultipleVideos) {
        val videoPosition = position - images.size
        if (videoPosition in videos.indices) {
            videos[videoPosition] = feedVideo
            notifyItemChanged(position)
        }
    }
    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${images.size + videos.size}")

        return images.size + videos.size
    }

    fun addDocuments(documentList: List<String>) {
        val startPosition = images.size + videos.size + documents.size
        documents.addAll(documentList)
        notifyItemRangeInserted(startPosition, documentList.size)
    }


}