package com.uyscuti.social.circuit.User_Interface.uploads.feed_uploads.adapter

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.circuit.User_Interface.uploads.feed_uploads.models.AudioDataClass
import com.uyscuti.social.circuit.R
import java.io.File
import java.io.FileOutputStream

private const val TAG = "FeedAudiAdapter"
class FeedAudioAdapter(
    private val audioList: List<AudioDataClass>,
    private val resources: Resources,
    private val onSelectedFilesCount: SelectedFilesCount,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<FeedAudioAdapter.AudioViewHolder>()
{

    var audiosSelected: MutableList<AudioDataClass> = mutableListOf()
    //
//    init {
//        audioLists = audioList
//    }
    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumArtImageView: ImageView = itemView.findViewById(R.id.albumArtImageView)
        val audioTitleTextView: TextView = itemView.findViewById(R.id.audioTitleTextView)
        val audioArtistTextView: TextView = itemView.findViewById(R.id.audioArtistTextView)
        val audioSize: TextView = itemView.findViewById(R.id.audioSize)
        val audioItemContainer: LinearLayout = itemView.findViewById(R.id.audioItemContainer)



        init {
//            val selectableItemBackground = TypedValue()
//            itemView.context.theme.resolveAttribute(
//                android.R.attr.selectableItemBackground,
//                selectableItemBackground,
//                true
//            )
//            itemView.setBackgroundResource(selectableItemBackground.resourceId)

//            audioItemContainer.setOnClickListener {
//                Log.d(TAG, "itemClicked: ")
//                setMultipleSelection(adapterPosition)
//            }
        }




    }
    private fun setMultipleSelection(adapterPosition: Int) {
        val audio = audioList[adapterPosition]

        audio.isSelected = !audio.isSelected

        if (audio.isSelected) {
            audiosSelected.add(audio)
        } else {
            audiosSelected.remove(audio)
        }

        Log.d(TAG, "setMultipleSelection: audiosSelected size ${audiosSelected.size}")
        onSelectedFilesCount.onFilesCount(audiosSelected.size)
        notifyItemChanged(adapterPosition)
    }

    //    private fun setMultipleSelection(adapterPosition: Int) {
//
//        audioList[adapterPosition].isSelected = !audioList[adapterPosition].isSelected
////        audiosSelected[adapterPosition].isSelected = !audiosSelected[adapterPosition].isSelected
//        if (audioList[adapterPosition].isSelected) {
//            audiosSelected.add(adapterPosition, audioList[adapterPosition])
//        }else {
//            try {
//                for(i in audiosSelected) {
//                    val found = audioList.find { it.audioList == i.audioList }
//                    audiosSelected.remove(found)
//
//                }
//            }catch (e: IndexOutOfBoundsException) {
//                Log.e(TAG, "setMultipleSelection: IndexOutOfBoundsException ${e.message}")
//                e.printStackTrace()
//            }
//        }
//        Log.d(TAG, "onBindViewHolder: audioList size ${audiosSelected.size}")
//
//        notifyItemChanged(adapterPosition)
//    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.audio_item_layout, parent, false)
        return AudioViewHolder(view)
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioPath = audioList[position]

        val audioTitle = getAudioTitle(audioPath.audioList)
        val audioArtist = getAudioArtist(audioPath.audioList)
        val albumArt = getAlbumArt(audioPath.audioList)

        val size = File(audioPath.audioList).length()

        holder.audioTitleTextView.text = audioTitle
        holder.audioArtistTextView.text = audioArtist

        holder.audioSize.text = formatSize(size)


        if (albumArt != null) {
            Glide.with(holder.itemView.context)
                .asBitmap()
                .load(albumArt)
                .error(R.drawable.error_drawable)
                .centerCrop()
                .into(holder.albumArtImageView)
        } else {
            holder.albumArtImageView.setImageResource(R.drawable.ic_launcher_background)
        }
//        holder.itemView.setOnClickListener {
//            // Handle audio item click event
//            // You can start audio playback or perform other actions here
//            onItemClick(audioPath.audioList)
//        }

        if(audioPath.isSelected) {
            holder.itemView.setBackgroundColor(holder.itemView.context.resources.getColor(R.color.bluejeans))
        }else {
            holder.itemView.setBackgroundColor(holder.itemView.context.resources.getColor(R.color.white))
        }

        holder.audioItemContainer.setOnClickListener {
            onItemClick(audioPath.audioList)
            setMultipleSelection(position)
        }
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    fun getSelectedAudios() : MutableList<AudioDataClass> {
        return audiosSelected
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

        val outputStream: FileOutputStream = FileOutputStream(outputFile)
        outputStream.write(albumArtBytes)
        outputStream.close()

        return outputFile
    }

    private fun getRoundedBitmapDrawable(bitmap: Bitmap): RoundedBitmapDrawable {
        val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundedDrawable.isCircular = true
        return roundedDrawable
    }


}

interface SelectedFilesCount {
    fun onFilesCount(count: Int)
//    fun doneClicked(count: Int, selectedAudios: AudioDataClass)
}