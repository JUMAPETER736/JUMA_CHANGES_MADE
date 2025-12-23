package com.uyscuti.social.circuit.adapter.feed.multiple_files

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.circuit.model.feed.multiple_files.MultipleAudios
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.User_Interface.fragments.feed.UploadFeedActivity
import java.io.File
import java.io.FileOutputStream

private const val TAG ="MultipleFeedAudioAdapter"

class MultipleFeedAudioAdapter(
    private var context: Context,
    private var audios: List<MultipleAudios>,
    private var multipleAudiosListener: UploadFeedActivity
) :
    RecyclerView.Adapter<MultipleFeedAudioAdapter.Pager2ViewHolder>() {
    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val images: ImageView = itemView.findViewById(R.id.img)


        init {
            images.setOnClickListener {
                val position = absoluteAdapterPosition
                Log.d(TAG, "${position + 1}: ")
            }
        }
    }

    fun getAudioDetails(position: Int): MultipleAudios {
        return audios[position]
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.feed_multiple_audios_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

//        holder.images.setImageResource(images[position])
        val audioPath = audios[position]
        val albumArt = getAlbumArt(audioPath.audioPath)
        multipleAudiosListener.onAudioDisplay(audios[position])

        if (albumArt != null) {
            // Define the color and blending mode for tinting
            val color = ContextCompat.getColor(context, com.uyscuti.social.chatsuit.R.color.transparent)
            val mode = PorterDuff.Mode.SRC_ATOP  // Or another mode as per your requirement

// Create a color filter with the specified color and mode
            val colorFilter = PorterDuffColorFilter(color, mode)

// Apply the color filter to the ImageView
            holder.images.colorFilter = colorFilter
            Glide.with(context)
                .load(albumArt)
                .into( holder.images)
        } else {
            holder.images.setImageResource(R.drawable.baseline_headphones_24)
        }


        holder.images.setOnClickListener {
            multipleAudiosListener.onAudioClick()
        }
    }

    override fun getItemCount(): Int {
        return audios.size
    }
    private fun getAlbumArt(audioPath: String): Uri? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        return try {
            mediaMetadataRetriever.setDataSource(audioPath)

            val albumArtBytes = mediaMetadataRetriever.embeddedPicture

            if (albumArtBytes != null) {
                val albumArtFile = saveAlbumArt(albumArtBytes)
                Uri.fromFile(albumArtFile)
            } else {
                null
            }
        } catch (e: Exception){
            e.printStackTrace()
            null
        } finally {
            // Make sure to release the resources when done
            mediaMetadataRetriever.release()
        }

    }

    private fun saveAlbumArt(albumArtBytes: ByteArray): File {
        val outputDir: File =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val outputFile = File.createTempFile("album_art", ".jpg", outputDir)

        val outputStream: FileOutputStream = FileOutputStream(outputFile)
        outputStream.write(albumArtBytes)
        outputStream.close()

        return outputFile
    }
    fun getAudioDuration(audioPath: String): String {
        val audio =  audios.find { it.audioPath == audioPath }
        if(audio != null) {
            return audio.audioDuration
        }else {
            return "00:00"
        }
    }
}



interface MultipleAudiosListener {
    fun onAudioClick()
    fun onAudioDisplay(details: MultipleAudios)
}