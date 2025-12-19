package com.uyscuti.sharedmodule.feed_demo

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.uyscuti.sharedmodule.databinding.FragmentMixedFeedFilesUploadBinding
import com.uyscuti.sharedmodule.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.sharedmodule.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.sharedmodule.R
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "MixedFeedFilesUploadFragment"
class MixedFeedFilesUploadFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var video: FeedMultipleVideos? = null
    private var compressedImageFile: File? = null
    private lateinit var binding: FragmentMixedFeedFilesUploadBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMixedFeedFilesUploadBinding.inflate(layoutInflater, container, false)
        val mixedFeedUploadData =
            arguments?.getParcelable<MixedFeedUploadDataClass>("mixedFeedUploadDataClass")

        Log.d(TAG, "onCreateView: mixedFeedUploadData $mixedFeedUploadData")

        if (mixedFeedUploadData != null) {
            video = mixedFeedUploadData.videos

            if (mixedFeedUploadData.images?.imagePath?.isNotEmpty() == true) {
                Glide.with(requireContext())
                    .load(mixedFeedUploadData.images!!.imagePath)
//                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .placeholder(R.drawable.flash21)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.feedUploadImageView)

                if (mixedFeedUploadData.images!!.compressedImagePath.isEmpty()) {
                    val file = File(mixedFeedUploadData.images!!.imagePath)
                    lifecycleScope.launch {
                        val compressedImageFile =
                            Compressor.compress(requireContext(), file)
                        Log.d(
                            "cameraLauncher",
                            "cameraLauncher: compressedImageFile absolutePath: ${compressedImageFile.absolutePath}"
                        )

                        mixedFeedUploadData.images!!.compressedImagePath =
                            compressedImageFile.absolutePath
                    }
                } else {
                    Log.i(TAG, "onCreateView: compressed image is not empty")
                }

            } else if (mixedFeedUploadData.videos?.videoPath?.isNotEmpty() == true) {
                if (video?.thumbnail != null) {
                    Log.d(TAG, "onCreateView: thumbnail not null")
                    Glide.with(requireContext())
                        .load(video!!.thumbnail)
                        .into(binding.feedUploadImageView)
                } else {
                    Log.d(TAG, "onCreateView: thumbnail  null")
                    Glide.with(requireContext())
                        .load(Uri.fromFile(File(video?.videoPath ?: "")))
                        .into(binding.feedUploadImageView)
                }
            } else if (mixedFeedUploadData.audios?.audioPath?.isNotEmpty() == true) {
                val audioPath = mixedFeedUploadData.audios?.audioPath
                Log.d(
                    TAG,
                    "onCreateView: audioPath $audioPath audio path to string ${audioPath.toString()}"
                )

                val albumArt = audioPath?.let { getAlbumArt(it.toString()) }

                if (albumArt != null) {
                    Log.d(TAG, "onCreateView: album art not null")

                    Glide.with(requireContext())
                        .load(albumArt)
                        .into(binding.feedUploadImageView)
                } else {
                    Log.d(TAG, "onCreateView: album art null")
                    Glide.with(requireContext())
                        .load(R.drawable.music_placeholder)
                        .placeholder(R.drawable.flash21)
                        .error(R.drawable.error_drawable)
                        .into(binding.feedUploadImageView)
//                    binding.feedUploadImageView.setImageResource(R.drawable.baseline_headphones_24)
                }
            } else if (mixedFeedUploadData.documents?.uri != null) {
                if (mixedFeedUploadData.documents?.documentThumbnailFilePath != null) {
                    Glide.with(requireContext())
                        .load(mixedFeedUploadData.documents?.documentThumbnailFilePath)
                        .into(binding.feedUploadImageView)
                } else {
                    Glide.with(requireContext())
                        .load(R.drawable.documents)
                        .into(binding.feedUploadImageView)
                }

            }
        } else {
            Log.i(TAG, "onCreateView: mixed feed upload data is empty")
        }
        return binding.root
    }

    fun getVideoDetails(): FeedMultipleVideos? {
        return video
    }

    fun updateVideo(newVideo: FeedMultipleVideos) {
        video = newVideo

        Log.d(TAG, "updateVideo: $video")
        if (video?.thumbnail != null) {
            Log.d(TAG, "onCreateView: thumbnail not null")
            Glide.with(requireContext())
                .load(video!!.thumbnail)
                .into(binding.feedUploadImageView)
        }
        // Update UI with the new video data
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
        } catch (e: Exception) {
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


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MixedFeedFilesUploadFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        fun newInstance(mixedFeedUploadDataClass: MixedFeedUploadDataClass) =
            MixedFeedFilesUploadFragment().apply {
                arguments = Bundle().apply {

                    putParcelable("mixedFeedUploadDataClass", mixedFeedUploadDataClass)
                }
            }
    }
}