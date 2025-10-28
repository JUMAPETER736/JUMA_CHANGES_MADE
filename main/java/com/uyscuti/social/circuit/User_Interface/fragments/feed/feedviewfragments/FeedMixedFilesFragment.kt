package com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.uyscuti.social.circuit.eventbus.ShowFeedFloatingActionButton
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.model.GoToShortsFragment
import com.uyscuti.social.circuit.model.ShowAppBar
import com.uyscuti.social.circuit.model.ShowBottomNav
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.FragmentFeedMixedFilesBinding
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import org.greenrobot.eventbus.EventBus

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedMixedFilesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "FeedMixedFilesFragment"

class FeedMixedFilesFragment : Fragment(), FeedTextViewFragmentInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data:  com.uyscuti.social.network.api.response.posts.Post
    private var position = 0
    private var feedPostPosition = -1
    private var fileId = ""
    private var mediaPlayer: MediaPlayer? = null
    private var audioPlayingPosition = -1
    private var isPlaying = false
    private lateinit var binding: FragmentFeedMixedFilesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            data = (it.getSerializable("feedPost") as  com.uyscuti.social.network.api.response.posts.Post?)!!
            position = it.getInt("position")
            feedPostPosition = it.getInt("feedPostPosition")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFeedMixedFilesBinding.inflate(layoutInflater, container, false)

        val fileIdToFind = data.fileTypes?.get(position)

        fileId = data.fileIds[position].toString()

        val fileItem = data.files.find { it.fileId == fileId }


        val contentType = data.fileTypes?.get(position)


        if (contentType != null) {


            if (fileItem != null) {
                Log.d(
                    TAG,
                    "onCreateView: content type: ${contentType.fileType}  file item ${fileItem.url}"
                )
            }

            when (contentType.fileType) {
                "image" -> {
                    if (fileItem != null) {
                        setImage(fileItem.url)
                    }
                }
                "audio" -> {
                    if (fileItem != null) {
                        setAudio(fileItem.url)
                    }
                }
                "video" -> {
                    if (fileItem != null) {
                        setVideo(fileItem.url)
                    }
                }
                "doc", "pdf" -> {
                    setDocument()
                }
            }
        }
        return binding.root
    }

    private fun setImage(imageUrl: String) {
        binding.feedImageView.visibility = View.VISIBLE
        binding.feedAudioContainer.visibility = View.GONE
        binding.videoItemLayout.visibility = View.GONE
        binding.documentLayoutContainer.visibility = View.GONE
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.profilepic2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.feedImageView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseMediaPlayer()
    }

    @SuppressLint("SetTextI18n")
    private fun setAudio(audioUrl: String) {
        releaseMediaPlayer()
        binding.feedAudioContainer.visibility = View.VISIBLE
        binding.feedImageView.visibility = View.GONE
        binding.videoItemLayout.visibility = View.GONE
        binding.documentLayoutContainer.visibility = View.GONE
        val durationItem = data.duration.find { it.fileId == fileId }
        if (durationItem != null) {
            binding.audioDuration.text = durationItem.duration

        } else {
            binding.audioDuration.text = "00:00"

        }
        binding.playPauseButton.setOnClickListener {
            Toast.makeText(requireContext(), "play button clicked", Toast.LENGTH_SHORT).show()
            EventBus.getDefault().post(FeedAudioViewFragment())
            Log.d(
                "buttonplay","audio url $audioUrl"
            )

            mediaPlayer.apply {
                if (this == null) {
                    mediaPlayer = MediaPlayer()

                }else
                {
                    mediaPlayer?.reset()
                }
                try {
                    mediaPlayer?.setDataSource(audioUrl)
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mediaPlayer?.setOnCompletionListener {
                    binding.playPauseButton.setImageResource(R.drawable.play_svgrepo_com)
                    mediaPlayer?.release()
                }

                binding.playPauseButton.setImageResource(R.drawable.baseline_pause_white_24)

                mediaPlayer?.setOnPreparedListener {
                    binding.playPauseButton.setImageResource(R.drawable.baseline_pause_white_24)
                }
            }
        }
        binding.playPauseButton.setOnClickListener {
            if (mediaPlayer == null) {
                initializeMediaPlayer(audioUrl)
            } else {
                if (isPlaying) pauseAudio() else playAudio()
            }
        }
    }

    private fun initializeMediaPlayer(audioUrl: String) {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioUrl)
                prepareAsync() // Asynchronous preparation
                setOnPreparedListener { mediaPlayer ->
                    // Set duration in seek bar and start playing
                    binding.seekBar.max = mediaPlayer.duration
                    binding.seekBar.progress = 0
                    binding.seekBar.isEnabled = true
//                    binding.audioDuration.text = (mediaPlayer.duration)
                    playAudio()
                }
                setOnCompletionListener {
                    Log.d("onCompletion", "onCompletion: ")
                    binding.playPauseButton.setImageResource(R.drawable.play_svgrepo_com)
                    resetUI()
                    releaseMediaPlayer()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // SeekBar change listener for manual scrubbing
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer?.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    private fun playAudio() {
        mediaPlayer?.start()
        isPlaying = true
        binding.playPauseButton.setImageResource(R.drawable.baseline_pause_white_24)
        updateSeekBar()
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        val handler = Handler(Looper.getMainLooper())
        binding.playPauseButton.setImageResource(R.drawable.play_svgrepo_com)
        handler.removeCallbacks(updateSeekBarRunnable)
    }

    private fun updateSeekBar() {
        Log.d("updateSeekBar", "seekbar moving")
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(updateSeekBarRunnable, 1000)
    }

    private val updateSeekBarRunnable = object : Runnable {

        override fun run() {
            Log.d("updateSeekBar2", "seekbar moving")

            mediaPlayer?.let {
                val handler = Handler(Looper.getMainLooper())

                binding.seekBar.progress = it.currentPosition
                binding.audioDuration.text = formatDuration(it.currentPosition)
                handler.postDelayed(this, 1000)
            }
        }
    }

    // Release MediaPlayer resources
    private fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        val handler = Handler(Looper.getMainLooper())

        handler.removeCallbacks(updateSeekBarRunnable)
    }

    // Reset UI when audio stops
    private fun resetUI() {
        binding.seekBar.progress = 0
        binding.audioDuration.text = "00:00"
        binding.playPauseButton.setImageResource(R.drawable.play_svgrepo_com)
    }

    // Helper function to format duration in mm:ss format
    @SuppressLint("DefaultLocale")
    private fun formatDuration(duration: Int): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format(
            "%02d:%02d",
            minutes
            , seconds)
    }

    private fun setVideo(videoUrl: String) {
        Log.d("setVideo", "setVideo: feedShortsBusinessId ${data.feedShortsBusinessId} ")
        binding.videoItemLayout.visibility = View.VISIBLE
        binding.feedImageView.visibility = View.GONE
        binding.feedAudioContainer.visibility = View.GONE
        binding.documentLayoutContainer.visibility = View.GONE

        val thumbnailItem = data.thumbnail.find { it.fileId == fileId }
        val durationItem = data.duration.find { it.fileId == fileId }
//        Log.d(TAG, "setVideo: displaying video")
//        if (thumbnailItem != null) {
//            Log.d(TAG, "setVideo: ${thumbnailItem.thumbnailUrl}")
//        }
        if (thumbnailItem != null) {
            Glide.with(this)
                .load(thumbnailItem.thumbnailUrl)
                .placeholder(R.drawable.profilepic2)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.videoThumbnail)
            binding.videoThumbnail.setOnClickListener {
                Log.d(TAG, "setVideo: clicked video thumbnail")
                EventBus.getDefault().post(
                    GoToShortsFragment(
                        feedPostPosition, data.feedShortsBusinessId, fileId
                    )
                )
            }
        }

        if (durationItem != null) {
            binding.thumbnailVideoDuration.text = durationItem.duration
            binding.finalDuration.text = durationItem.duration
        }
    }

    private fun setDocument( ) {
        binding.videoItemLayout.visibility = View.GONE
        binding.feedImageView.visibility = View.GONE
        binding.feedAudioContainer.visibility = View.GONE
        binding.documentLayoutContainer.visibility = View.VISIBLE

        val documentTitle = data.fileNames.find { it.fileId == fileId }
        val numberOfPages = data.numberOfPages.find { it.fileId == fileId }
        val thumbnailItem = data.thumbnail.find { it.fileId == fileId }
        if (documentTitle != null) {
            binding.documentTitle.text = documentTitle.fileName
        }
        if (numberOfPages != null) {
            binding.numberOfPages.text = numberOfPages.toString()
        }
        if (thumbnailItem != null) {
            Glide.with(this)
                .load(thumbnailItem.thumbnailUrl)
                .placeholder(R.drawable.profilepic2)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.documentImageView)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedMixedFilesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedMixedFilesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun backPressedFromFeedTextViewFragment() {
        EventBus.getDefault().post(ShowBottomNav(false))
        EventBus.getDefault().post(ShowAppBar(false))
        EventBus.getDefault().post(ShowFeedFloatingActionButton(false))
        releaseMediaPlayer()

    }

    override fun onCommentClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {


    }

    override fun onLikeUnLikeFeedFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {


    }

    override fun onFeedFavoriteClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {


    }

    override fun onMoreOptionsClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {


    }

    override fun finishedPlayingVideo(position: Int) {

    }

    override fun onRePostClickFromFeedTextViewFragment(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        TODO("Not yet implemented")
    }

    override fun onFullScreenClicked(data: MixedFeedUploadDataClass) {
        TODO("Not yet implemented")
    }

    override fun onMediaClick(data: MixedFeedUploadDataClass) {
        TODO("Not yet implemented")
    }

    override fun onMediaPrepared(mp: MediaPlayer) {
        TODO("Not yet implemented")
    }

    override fun onMediaError() {
        TODO("Not yet implemented")
    }
}