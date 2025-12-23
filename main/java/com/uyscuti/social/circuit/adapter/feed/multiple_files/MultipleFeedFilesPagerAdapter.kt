package com.uyscuti.social.circuit.adapter.feed.multiple_files

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.uyscuti.social.circuit.feed_demo.MixedFeedFilesUploadFragment
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.model.feed.FeedMultipleImages
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleAudios
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleDocumentsDataClass
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.social.circuit.User_Interface.fragments.feed.UploadFeedActivity
import com.uyscuti.social.circuit.viewmodels.feed.FeedUploadViewModel

private const val TAG = "MultipleFeedFilesPagerAdapter"

class MultipleFeedFilesPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val isFullScreen: Boolean = false
) : FragmentStateAdapter(fragmentActivity) {

    private val mixedFeedUploadDataClass: MutableList<MixedFeedUploadDataClass> = mutableListOf()
    private var backPressedCallback: OnBackPressedCallback? = null
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null
    private var currentFragment: MixedFeedFilesUploadFragment? = null

    private var currentPlayingPosition = -1
    private var thumbnailsRestored = false

    private val activity = fragmentActivity

    private var viewPager2: ViewPager2? = null // Add this line

    @SuppressLint("NotifyDataSetChanged")
    fun setMixedFeedUploadDataClass(mixedFeedUploadDataClass: MixedFeedUploadDataClass) {
        this.mixedFeedUploadDataClass.clear()
        this.mixedFeedUploadDataClass.add(mixedFeedUploadDataClass)
        Log.d(TAG, "setMixedFeedUploadDataClass: Added single item, fileId=${mixedFeedUploadDataClass.fileId}, fileType=${mixedFeedUploadDataClass.fileTypes}, size=${this.mixedFeedUploadDataClass.size}")
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMixedFeedUploadDataClass(data: List<MixedFeedUploadDataClass>) {
        this.mixedFeedUploadDataClass.clear()
        this.mixedFeedUploadDataClass.addAll(data)
        Log.d(TAG, "setMixedFeedUploadDataClass: Added ${data.size} items, fileIds=${data.map { it.fileId }}")

        // Verify thumbnails for documents
        verifyThumbnailIntegrity()

        notifyDataSetChanged()
    }

    fun getItem(position: Int): MixedFeedUploadDataClass? {
        return mixedFeedUploadDataClass.getOrNull(position).also {
            Log.d(TAG, "getItem: position=$position, fileId=${it?.fileId}, fileType=${it?.fileTypes}")
        }
    }

    fun setViewPager(viewPager: ViewPager2) {
        this.viewPager2 = viewPager
        Log.d(TAG, "setViewPager: ViewPager reference set")
    }

    fun setBackPressedCallback(backPressedCallback: OnBackPressedCallback) {
        this.backPressedCallback = backPressedCallback
        Log.d(TAG, "setBackPressedCallback: Callback set")
    }

    fun setFeedTextViewFragmentInterface(feedTextViewFragmentInterface: FeedTextViewFragmentInterface?) {
        this.feedTextViewFragmentInterface = feedTextViewFragmentInterface
        Log.d(TAG, "setFeedTextViewFragmentInterface: Interface set")
        // Update existing fragments with the new interface
        currentFragment?.setFeedTextViewFragmentInterface(feedTextViewFragmentInterface)
    }

    override fun getItemCount(): Int = mixedFeedUploadDataClass.size



    override fun createFragment(position: Int): Fragment {
        val data = mixedFeedUploadDataClass.getOrNull(position)
        if (data == null) {
            Log.e(TAG, "createFragment: No data at position=$position")
            return Fragment()
        }

        Log.d(TAG, "createFragment: Creating fragment for position=$position, fileId=${data.fileId}, fileType=${data.fileTypes}")

        // Verify document thumbnail before creating fragment
        data.documents?.let { doc ->
            Log.d(TAG, "createFragment: Document ${doc.filename} has thumbnail: ${doc.documentThumbnailFilePath != null}")
        }

        val fragment = MixedFeedFilesUploadFragment.newInstance(data, isFullScreen).apply {
            setFeedTextViewFragmentInterface(feedTextViewFragmentInterface)
            setAllMediaItems(mixedFeedUploadDataClass, position)
            // Set adapter reference for zoom control
          //  setAdapterReference(this@MultipleFeedFilesPagerAdapter)
        }

        Log.d(TAG, "createFragment: Created fragment for position=$position, fileId=${data.fileId}, fileType=${data.fileTypes}, isFullScreen=$isFullScreen")
        return fragment
    }

    fun getAudioDetails(position: Int): FeedMultipleAudios? {
        return mixedFeedUploadDataClass.getOrNull(position)?.audios.also {
            Log.d(TAG, "getAudioDetails: position=$position, fileId=${mixedFeedUploadDataClass.getOrNull(position)?.fileId}, audioPath=${it?.audioPath}")
        } ?: run {
            Log.w(TAG, "getAudioDetails: Invalid position or no audio data: $position")
            null
        }
    }

    fun getDocumentDetails(position: Int): FeedMultipleDocumentsDataClass? {
        return mixedFeedUploadDataClass.getOrNull(position)?.documents.also {
            Log.d(TAG, "getDocumentDetails: position=$position, fileId=${mixedFeedUploadDataClass.getOrNull(position)?.fileId}, filename=${it?.filename}, hasThumbnail=${it?.documentThumbnailFilePath != null}")
        } ?: run {
            Log.w(TAG, "getDocumentDetails: Invalid position or no document data: $position")
            null
        }
    }

    fun getVideoDetails(position: Int): FeedMultipleVideos? {
        return mixedFeedUploadDataClass.getOrNull(position)?.videos.also {
            Log.d(TAG, "getVideoDetails: position=$position, fileId=${mixedFeedUploadDataClass.getOrNull(position)?.fileId}, videoPath=${it?.videoPath}")
        } ?: run {
            Log.w(TAG, "getVideoDetails: Invalid position or no video data: $position")
            null
        }
    }

    fun getImageDetails(position: Int): FeedMultipleImages? {
        return mixedFeedUploadDataClass.getOrNull(position)?.images.also {
            Log.d(TAG, "getImageDetails: position=$position, fileId=${mixedFeedUploadDataClass.getOrNull(position)?.fileId}, imagePath=${it?.imagePath}")
        } ?: run {
            Log.w(TAG, "getImageDetails: Invalid position or no image data: $position")
            null
        }
    }


    fun updateSelectedVideo(position: Int, feedVideo: FeedMultipleVideos) {
        if (position in mixedFeedUploadDataClass.indices) {
            mixedFeedUploadDataClass[position].videos = feedVideo
            currentFragment?.takeIf { it.arguments?.getInt("position", -1) == position }?.let {
                it.updateVideo(feedVideo)
                Log.d(TAG, "updateSelectedVideo: Updated video at position=$position, fileId=${mixedFeedUploadDataClass[position].fileId}, thumbnail=${feedVideo.thumbnail}")
            }
            notifyItemChanged(position)
        } else {
            Log.w(TAG, "updateSelectedVideo: Invalid position=$position")
        }
    }

    fun getAllItems(): List<MixedFeedUploadDataClass> {
        return mixedFeedUploadDataClass.toList().also {
            Log.d(TAG, "getAllItems: Returning ${it.size} items, fileIds=${it.map { item -> item.fileId }}")
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

    fun removeItem(position: Int) {
        if (position >= 0 && position < mixedFeedUploadDataClass.size) {
            mixedFeedUploadDataClass.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, mixedFeedUploadDataClass.size)
        }
    }

    fun setVideoLooping(position: Int, shouldLoop: Boolean) {
        val fragment = getFragmentAt(position) as? MixedFeedFilesUploadFragment
        fragment?.setVideoLooping(shouldLoop)
        Log.d(TAG, "setVideoLooping: Set to $shouldLoop for position $position")
    }

    fun setAudioLooping(position: Int, shouldLoop: Boolean) {
        val fragment = getFragmentAt(position) as? MixedFeedFilesUploadFragment
        fragment?.setAudioLooping(shouldLoop)
        Log.d(TAG, "setAudioLooping: Set to $shouldLoop for position $position")
    }

    // Also add this method to set looping for all fragments
    fun setAllMediaLooping(shouldLoop: Boolean) {
        for (i in 0 until itemCount) {
            val fragment = getFragmentAt(i) as? MixedFeedFilesUploadFragment
            fragment?.setVideoLooping(shouldLoop)
            fragment?.setAudioLooping(shouldLoop)
        }
        Log.d(TAG, "setAllMediaLooping: Set to $shouldLoop for all ${itemCount} items")
    }

    // Method to enable/disable ViewPager swiping when zooming
    fun setViewPagerSwipeEnabled(enabled: Boolean) {
        viewPager2?.isUserInputEnabled = enabled
        Log.d(TAG, "setViewPagerSwipeEnabled: $enabled")
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


    fun getAllDocuments(): List<FeedMultipleDocumentsDataClass> {
        return mixedFeedUploadDataClass.mapNotNull { it.documents }.also { docs ->
            Log.d(TAG, "getAllDocuments: Found ${docs.size} documents")
            docs.forEachIndexed { index, doc ->
                Log.d(TAG, "Document $index: ${doc.filename}, hasThumbnail: ${doc.documentThumbnailFilePath != null}")
            }
        }
    }

    fun MultipleFeedFilesPagerAdapter.setDataWithThumbnailPreservation(
        data: List<MixedFeedUploadDataClass>,
        viewModel: FeedUploadViewModel
    ) {
        // First preserve thumbnails in ViewModel
        viewModel.preserveDocumentThumbnails()

        // Verify thumbnails before setting data
        val dataWithThumbnails = data.map { mixedData ->
            mixedData.documents?.let { doc ->
                if (doc.documentThumbnailFilePath == null) { // Fixed: Changed from isNullOrEmpty() to == null
                    // Try to restore from ViewModel cache
                    viewModel.getDocumentThumbnail(doc.filename)?.let { cachedThumbnail ->
                        val bitmap = BitmapFactory.decodeFile(cachedThumbnail) // Fixed: Convert string path to Bitmap
                        doc.documentThumbnailFilePath = bitmap
                        thumbnailsRestored = true
                        Log.d("AdapterExtension", "Restored thumbnail for: ${doc.filename}")
                    }
                }
            }
            mixedData
        }

        // Set the data with preserved thumbnails
        this.setMixedFeedUploadDataClass(dataWithThumbnails)

        // Verify integrity after setting
        this.verifyThumbnailIntegrity()
    }

    // Method to call before any operations that might reset data
    fun MultipleFeedFilesPagerAdapter.preserveDocumentThumbnailsBeforeOperation() {
        val allData = this.getAllItems()
        allData.forEach { mixedData ->
            mixedData.documents?.let { doc ->
                if (doc.documentThumbnailFilePath != null) { // Fixed: Changed from !isNullOrEmpty() to != null
                    Log.d("AdapterExtension", "Preserving thumbnail for: ${doc.filename} -> ${doc.documentThumbnailFilePath}")
                }
            }
        }
    }

    // Enhanced method for your Activity to set data safely
    fun UploadFeedActivity.setAdapterDataSafely(
        adapter: MultipleFeedFilesPagerAdapter,
        viewModel: FeedUploadViewModel,
        data: List<MixedFeedUploadDataClass>
    ) {
        // Preserve thumbnails first
        adapter.preserveDocumentThumbnailsBeforeOperation()

        // Set data with thumbnail preservation
        adapter.setDataWithThumbnailPreservation(data, viewModel)

        // Verify all thumbnails are present
        val allThumbnailsPresent = viewModel.verifyDocumentThumbnails()
        if (!allThumbnailsPresent) {
            Log.w("UploadFeedActivity", "Some document thumbnails are missing after setting data")
        }
    }

    // Method to call in your Activity's onResume or whenever you need to ensure thumbnails are intact
    fun UploadFeedActivity.restoreDocumentThumbnails(
        adapter: MultipleFeedFilesPagerAdapter,
        viewModel: FeedUploadViewModel
    ) {
        val currentData = adapter.getAllItems()

        currentData.forEachIndexed { index, mixedData ->
            mixedData.documents?.let { doc ->
                if (doc.documentThumbnailFilePath == null) { // Fixed: Changed from isNullOrEmpty() to == null
                    // Try to restore from ViewModel cache
                    viewModel.getDocumentThumbnail(doc.filename)?.let { cachedThumbnail ->
                        val bitmap = BitmapFactory.decodeFile(cachedThumbnail) // Fixed: Convert string path to Bitmap
                        doc.documentThumbnailFilePath = bitmap
                        Log.d("UploadFeedActivity", "Restored thumbnail for document: ${doc.filename}")

                        // Update the specific item in adapter
                        adapter.setMixedFeedUploadDataClass(currentData)
                    }
                }
            }
        }
    }
}