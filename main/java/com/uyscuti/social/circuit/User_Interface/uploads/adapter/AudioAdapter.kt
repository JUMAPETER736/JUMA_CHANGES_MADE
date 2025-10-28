package com.uyscuti.social.circuit.User_Interface.uploads.adapter

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.circuit.R
import java.io.File
import java.io.FileOutputStream

class AudioAdapter(
    private val audioList: List<String>,
    private val resources: Resources,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>()
{

    class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumArtImageView: ImageView = itemView.findViewById(R.id.albumArtImageView)
        val audioTitleTextView: TextView = itemView.findViewById(R.id.audioTitleTextView)
        val audioArtistTextView: TextView = itemView.findViewById(R.id.audioArtistTextView)
        val audioSize: TextView = itemView.findViewById(R.id.audioSize)


        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                selectableItemBackground,
                true
            )
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.audio_item_layout, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioPath = audioList[position]

        val audioTitle = getAudioTitle(audioPath)
        val audioArtist = getAudioArtist(audioPath)
        val albumArt = getAlbumArt(audioPath)

        val size = File(audioPath).length()

        holder.audioTitleTextView.text = audioTitle
        holder.audioArtistTextView.text = audioArtist

        holder.audioSize.text = formatSize(size)


        if (albumArt != null) {
            Glide.with(holder.itemView.context)
                .asBitmap()
                .load(albumArt)
                .error(R.drawable.music_placeholder)
                .centerCrop()
                .into(holder.albumArtImageView)
        } else {
            holder.albumArtImageView.setImageResource(R.drawable.music_placeholder)
        }
        holder.itemView.setOnClickListener {
            // Handle audio item click event
            // You can start audio playback or perform other actions here
            onItemClick(audioPath)
        }
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    @SuppressLint("DefaultLocale")
    private fun formatSize(sizeBytes: Long): String {
        val sizeKb = sizeBytes / 1024
        return if (sizeKb < 1024) {
            "$sizeKb KB"
        } else {
            String.format("%.2f MB", sizeKb / 1024.0)
        }
    }

    private fun getAudioTitle(audioPath: String): String {
        // Extract the audio title from the audio path or use a different approach based on your needs
        return File(audioPath).nameWithoutExtension
    }

    private fun getAudioArtist(audioPath: String): String {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        return try {
            mediaMetadataRetriever.setDataSource(audioPath)

            val artist =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)

            artist ?: "Unknown Artist"
        } catch (e: Exception) {
            // Handle any exceptions that may occur during metadata retrieval
            e.printStackTrace()
            "Unknown Artist"
        } finally {
            // Make sure to release the resources when done
            mediaMetadataRetriever.release()
        }
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

        val outputStream: FileOutputStream? = FileOutputStream(outputFile)
        outputStream?.write(albumArtBytes)
        outputStream?.close()

        return outputFile
    }

    private fun getRoundedBitmapDrawable(bitmap: Bitmap): RoundedBitmapDrawable {
        val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundedDrawable.isCircular = true
        return roundedDrawable
    }
}

