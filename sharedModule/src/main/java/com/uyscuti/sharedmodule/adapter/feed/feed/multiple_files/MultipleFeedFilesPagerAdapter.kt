package com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uyscuti.sharedmodule.feed_demo.MixedFeedFilesUploadFragment
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.sharedmodule.model.feed.multiple_files.FeedMultipleAudios
import com.uyscuti.sharedmodule.model.feed.multiple_files.FeedMultipleDocumentsDataClass
import com.uyscuti.sharedmodule.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.sharedmodule.model.feed.multiple_files.MixedFeedUploadDataClass


private const val TAG = "MultipleFeedFilesPagerAdapter"

class MultipleFeedFilesPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null

    private val activity = fragmentActivity

    private var mixedFeedUploadDataClass: MutableList<MixedFeedUploadDataClass> = mutableListOf()
    var position = 0
    var fragment : MixedFeedFilesUploadFragment? = null

    fun setMixedFeedUploadDataClass2(mixedFeedUploadDataClass: MutableList<MixedFeedUploadDataClass>) {
        Log.d(TAG, "setMixedFeedUploadDataClass2: size b4 clear: ${mixedFeedUploadDataClass.size}")
        this.mixedFeedUploadDataClass.clear()
        Log.d(TAG, "setMixedFeedUploadDataClass2: size after clear: ${mixedFeedUploadDataClass.size}")
        this.mixedFeedUploadDataClass.addAll(mixedFeedUploadDataClass)
        Log.d(TAG, "setMixedFeedUploadDataClass2: size after add: ${mixedFeedUploadDataClass.size}")
    }

    override fun getItemCount(): Int = mixedFeedUploadDataClass.size

    override fun createFragment(position: Int): Fragment {
        fragment = MixedFeedFilesUploadFragment()

        this.position = position
        fragment!!.arguments = Bundle().apply {

            putParcelable("mixedFeedUploadDataClass", mixedFeedUploadDataClass[position])
        }
        return fragment as MixedFeedFilesUploadFragment
    }

    fun getVideoDetails(position: Int): FeedMultipleVideos? {
        return mixedFeedUploadDataClass[position].videos
    }
    fun getVideoDetails(): MutableList<MixedFeedUploadDataClass> {
        return mixedFeedUploadDataClass
    }

    fun updateSelectedVideo(position: Int, feedVideo: FeedMultipleVideos) {
        Log.d(TAG, "updateSelectedVideo: position ${this.position} position $position")
        mixedFeedUploadDataClass[this.position].videos = feedVideo
        // Update the fragment arguments directly if needed
        fragment?.let {
            it.arguments = Bundle().apply {
                putParcelable("mixedFeedUploadDataClass", mixedFeedUploadDataClass[position])
            }
            it.updateVideo(feedVideo) // Call a method in the fragment to handle data update
        }


        notifyItemChanged(this.position)

    }

    fun getAudioDetails(position: Int): FeedMultipleAudios? {
        return mixedFeedUploadDataClass[position].audios
    }

    fun getDocumentDetails(position: Int): FeedMultipleDocumentsDataClass? {
        return mixedFeedUploadDataClass[position].documents
    }


    fun getAllItems(): List<MixedFeedUploadDataClass> {
        return mixedFeedUploadDataClass.toList().also {
            Log.d(TAG, "getAllItems: Returning ${it.size} items, fileIds=${it.map { item -> item.fileId }}")
        }
    }

    // Add this method to verify thumbnail integrity
    fun verifyThumbnailIntegrity() {
        mixedFeedUploadDataClass.forEachIndexed { index, mixedData ->
            val doc = mixedData.documents
            if (doc != null) {
                Log.d(TAG, "Document $index (${doc.filename}) has thumbnail: ${doc.documentThumbnailFilePath != null}")
                if (doc.documentThumbnailFilePath == null) {
                    Log.w(TAG, "WARNING: Document ${doc.filename} at position $index missing thumbnail!")
                }
            }
        }
    }

    fun getItem(position: Int): MixedFeedUploadDataClass? {
        return mixedFeedUploadDataClass.getOrNull(position).also {
            Log.d(TAG, "getItem: position=$position, fileId=${it?.fileId}, fileType=${it?.fileTypes}")
        }
    }

    fun getCurrentVideoPosition(position: Int): Long {
        val fragment = getFragmentAt(position) as? MixedFeedFilesUploadFragment
        return fragment?.getCurrentVideoPosition() ?: 0L
    }

    fun getCurrentAudioPosition(position: Int): Long {
        val fragment = getFragmentAt(position) as? MixedFeedFilesUploadFragment
        return fragment?.getCurrentAudioPosition() ?: 0L
    }

    fun getVideoDuration(position: Int): Long {
        val fragment = getFragmentAt(position) as? MixedFeedFilesUploadFragment
        return fragment?.getVideoDuration() ?: run {
            // Fallback: try to parse from video data
            getVideoDetails(position)?.videoDuration?.toLongOrNull() ?: 0L
        }
    }

    fun getAudioDuration(position: Int): Long {
        val fragment = getFragmentAt(position) as? MixedFeedFilesUploadFragment
        return fragment?.getAudioDuration() ?: 0L
    }

    fun playVideo(position: Int) {
        val fragment = getFragmentAt(position) as? MixedFeedFilesUploadFragment
        fragment?.playVideo()
        Log.d(TAG, "playVideo: position=$position, fileId=${getItem(position)?.fileId}")
    }

    fun pauseVideo(position: Int) {
        val fragment = getFragmentAt(position) as? MixedFeedFilesUploadFragment
        fragment?.pauseVideo()
        Log.d(TAG, "pauseVideo: position=$position, fileId=${getItem(position)?.fileId}")
    }

    fun playAudio(position: Int) {
        val fragment = getFragmentAt(position) as? MixedFeedFilesUploadFragment
        fragment?.playAudio()
        Log.d(TAG, "playAudio: position=$position, fileId=${getItem(position)?.fileId}")
    }

    fun pauseAudio(position: Int) {
        val fragment = getFragmentAt(position) as? MixedFeedFilesUploadFragment
        fragment?.pauseAudio()
        Log.d(TAG, "pauseAudio: position=$position, fileId=${getItem(position)?.fileId}")
    }

    fun seekVideo(position: Int, seekPosition: Long) {
        val fragment = getFragmentAt(position) as? MixedFeedFilesUploadFragment
        fragment?.seekVideo(seekPosition)
        Log.d(TAG, "seekVideo: position=$position, seekTo=$seekPosition, fileId=${getItem(position)?.fileId}")
    }

    fun seekAudio(position: Int, seekPosition: Long) {
        val fragment = getFragmentAt(position) as? MixedFeedFilesUploadFragment
        fragment?.seekAudio(seekPosition)
        Log.d(TAG, "seekAudio: position=$position, seekTo=$seekPosition, fileId=${getItem(position)?.fileId}")
    }

    fun releaseAllPlayers() {
        try {
            // Release all media players in fragments
            for (i in 0 until itemCount) {
                val fragment = getFragmentAt(i) as? MixedFeedFilesUploadFragment
                fragment?.releasePlayer()
            }
            Log.d(TAG, "releaseAllPlayers: Released all players")
        } catch (e: Exception) {
            Log.e(TAG, "releaseAllPlayers: Error releasing players: ${e.message}")
        }
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < mixedFeedUploadDataClass.size) {
            mixedFeedUploadDataClass.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, mixedFeedUploadDataClass.size)
        }
    }

    private fun getFragmentAt(position: Int): Fragment? {
        return try {
            val fragmentManager = activity.supportFragmentManager
            val fragmentTag = "f$position"
            fragmentManager.findFragmentByTag(fragmentTag)
        } catch (e: Exception) {
            Log.e(TAG, "getFragmentAt: Error getting fragment at position $position: ${e.message}")
            null
        }
    }


}
