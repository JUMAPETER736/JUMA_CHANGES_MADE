package com.uyscuti.social.business.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.business.FullScreenImageActivity
import com.uyscuti.social.business.R

class AddMediaPagerAdapter(
    private val mediaUrls: List<String>,
    private val context: Activity,
    private val videoThumbnail: String? = null
) :
    RecyclerView.Adapter<AddMediaPagerAdapter.ViewHolder>() {
    private val PERMISSION_REQUEST_CODE = 1001
    private val REQUEST_CODE_IMAGE_PICKER = 525
    private val REQUEST_CODE_VIDEO_PICKER = 158
    private val REQUEST_CODE_IMAGE_PICKER_CATALOGUE = 110

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val playerView: PlayerView = itemView.findViewById(R.id.playerView)
        val playButton: ImageView = itemView.findViewById(R.id.playButton)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val addImageView: ImageView = itemView.findViewById(R.id.addImage)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_media, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaUrl = mediaUrls[position]

        if (isVideoUrl(mediaUrl)) {
            Glide.with(context).load(videoThumbnail).centerCrop().into(holder.imageView)

            holder.playButton.visibility = View.VISIBLE
            holder.playerView.visibility = View.GONE
            holder.imageView.visibility = View.VISIBLE

            holder.playButton.setOnClickListener {

                if (holder.playerView.visibility == View.VISIBLE) {
                    stopVideo()
                }
                holder.playButton.visibility = View.GONE
                holder.imageView.visibility = View.GONE
                holder.playerView.visibility = View.VISIBLE
                val player = ExoPlayer.Builder(context).build()
                holder.playerView.player = player

                val mediaItem = MediaItem.fromUri(mediaUrl)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true
                holder.playerView.useController = true

            }
        } else {
            Glide.with(context).load(mediaUrl).centerCrop().into(holder.imageView)

            if (position == mediaUrls.size - 1) {
                holder.addImageView.visibility = View.VISIBLE
                holder.imageView.visibility = View.GONE
                holder.playButton.visibility = View.GONE
            } else {
                holder.addImageView.visibility = View.GONE
                holder.playButton.visibility = View.GONE
                holder.playerView.visibility = View.GONE
                holder.imageView.visibility = View.VISIBLE
            }

//            Glide.with(context)
//                .load(mediaUrl)
//                .centerCrop()
//                .into(holder.imageView)

            holder.addImageView.setOnClickListener {
                pickMedia(context)
            }

            holder.imageView.setOnClickListener {
                // Handle image click event
                if (position == mediaUrls.size - 1) {
                    pickMedia(context)
                    return@setOnClickListener
                }

                val imagePaths = ArrayList<String>()
                for (url in mediaUrls) {
                    imagePaths.add(url.toString())
                }

                val intent = Intent(context, FullScreenImageActivity::class.java)
                intent.putStringArrayListExtra("imageUrls", imagePaths)
                intent.putExtra("position", position)
                context.startActivity(intent)
            }
        }

    }

    private fun stopVideo() {
        val player = ExoPlayer.Builder(context).build()
        player.stop()
        player.release()

    }

    @SuppressLint("IntentReset")
    private fun pickMedia() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/video" // Set the MIME type for images
        intent.putExtra(
            MediaStore.EXTRA_PICK_IMAGES_MAX,
            8

        ) // Set the maximum number of images to pickstartActivityForResult(intent, PICK_IMAGE_REQUEST)
        val PICK_IMAGE_REQUEST = 525
        context.startActivityForResult(intent, PICK_IMAGE_REQUEST)
//        context.startActivity()
    }

    fun pickMedia(activity: Activity) {
        val options = arrayOf("Pick Image", "Pick Video")
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select Media")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    // Pick Image
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activity.startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
                }
                1 -> {
                    // Pick Video
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    activity.startActivityForResult(intent, REQUEST_CODE_VIDEO_PICKER)

                }
            }
        }
        builder.show()
    }

    private fun isVideoUrl(url: String): Boolean {
        val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".avi", ".mov")
        return videoExtensions.any { url.endsWith(it, ignoreCase = true) }
    }

    override fun getItemCount(): Int {
        return mediaUrls.size
    }

}



