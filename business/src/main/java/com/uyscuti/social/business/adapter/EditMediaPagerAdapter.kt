package com.uyscuti.social.business.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.uyscuti.social.business.R

class EditMediaPagerAdapter(
    private val mediaUrls: ArrayList<String>,
    private val context: Activity,
    private val videoThumbnail: String? = null


) :
    RecyclerView.Adapter<EditMediaPagerAdapter.ViewHolder>() {
    private val PERMISSION_REQUEST_IMAGE_PICKER = 1001
    private val REQUEST_CODE_IMAGE_PICKER = 525
    private val REQUEST_CODE_VIDEO_PICKER = 158
    private val REQUEST_CODE_IMAGE_PICKER_CATALOGUE = 110

    private var selectedPosition = -1
    private lateinit var player: ExoPlayer


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

//        val images = mutableListOf("image1.jpg", "image2.jpg", "image3.jpg") // Replace with actual image paths
//        var imageAdapter = ImageAdapter(images) { image, position ->
//            var selectedImage = image
//            var selectedPosition = position
//        }

    }


    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val mediaUrl = mediaUrls[position]

        if (isVideoUrl(mediaUrl)) {
            Glide.with(context).load(videoThumbnail).centerCrop()
                .placeholder(R.drawable.black).into(holder.imageView)

            holder.addImageView.visibility = View.GONE

            holder.playButton.visibility = View.VISIBLE
            holder.imageView.visibility = View.VISIBLE
            holder.playerView.visibility = View.GONE

            holder.playButton.setOnClickListener {
                stopVideo()

                holder.playButton.visibility = View.GONE
                holder.imageView.visibility = View.GONE
                holder.playerView.visibility = View.VISIBLE

                player = ExoPlayer.Builder(context).build()
                holder.playerView.player = player

                val mediaItem = MediaItem.fromUri(mediaUrl)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true
                holder.playerView.useController = true
                selectedPosition = position

                holder.playerView.setShowFastForwardButton(false)
                holder.playerView.setShowRewindButton(false)
                holder.playerView.setShowNextButton(false)
                holder.playerView.setShowPreviousButton(false)
            }

            holder.imageView.setOnClickListener {
                selectedPosition = position

                showBottomSheetDialog(position)
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
            holder.addImageView.setOnClickListener {
                pickMedia(context)

            }

            holder.imageView.setOnClickListener {
                if (position == mediaUrls.size - 1) {
                    pickMedia(context)
                    return@setOnClickListener

                } else {
                    selectedPosition = position
                    showBottomSheetDialog(position)

                }

                val imagePaths = ArrayList<String>()
                for (url in mediaUrls) {
                    imagePaths.add(url.toString())
                }

            }
        }
    }

    private fun showBottomSheetDialog(position: Int) {
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(R.layout.edit_bottom_sheet)
        val deleteButton = bottomSheetDialog.findViewById<LinearLayout>(R.id.delete)
        val videoButton = bottomSheetDialog.findViewById<LinearLayout>(R.id.video)
        val imageButton = bottomSheetDialog.findViewById<LinearLayout>(R.id.image)


        if (position != 0) {
            videoButton?.visibility = View.GONE
        }

        deleteButton?.setOnClickListener {
            mediaUrls.removeAt(position)
            notifyItemRemoved(position)
            bottomSheetDialog.dismiss()
        }

        videoButton?.setOnClickListener {

            // Pick Video
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            context.startActivityForResult(intent, REQUEST_CODE_VIDEO_PICKER)
            bottomSheetDialog.dismiss()
        }

        imageButton?.setOnClickListener {
            // Pick Image
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            context.startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
            bottomSheetDialog.dismiss()

        }

        bottomSheetDialog.show()

    }

    private fun deleteImage(it: Nothing) {


    }

    fun releasePlayer() {
        if (::player.isInitialized) {
            val player = ExoPlayer.Builder(context).build()
            player.stop()
            player.release()
            player.clearVideoSurface()
        }
    }

    fun pausePlayer() {
        if (::player.isInitialized) {
            player.pause()
        }
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }


    private fun stopVideo() {
        val player = ExoPlayer.Builder(context).build()
        player.stop()
        player.release()
        player.clearVideoSurface()
    }

    private fun isVideoUrl(url: String): Boolean {
        val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".avi", ".mov")
        return videoExtensions.any { url.endsWith(it, ignoreCase = true) }
    }

    override fun getItemCount(): Int {
        return mediaUrls.size
    }

    @SuppressLint("intentReset")
    private fun pickMedia() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/video" // Set the MIME type for images
        intent.putExtra(
            MediaStore.EXTRA_PICK_IMAGES_MAX,
            8
        ) // Set the maximum number of images to quickstartActivityForResult(intent, PICK_IMAGE_REQUEST)
        val PICK_IMAGE_REQUEST = 525
        context.startActivityForResult(intent, PICK_IMAGE_REQUEST)
//        context.startActivity()

    }

    private fun pickMedia(activity: Activity) {

        val options = arrayOf("Pick Image", "Pick Video")
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select Media")
        builder.setItems(options) { dialog, which ->
            // Handle the selected option
            when (which) {
                0 -> {
                    // Pick Image
                    selectedPosition = -1
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    context.startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
                }

                1 -> {
                    selectedPosition = -1
                    // Pick Video
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    context.startActivityForResult(intent, REQUEST_CODE_VIDEO_PICKER)
                }
            }
        }
        builder.show()
    }
}
