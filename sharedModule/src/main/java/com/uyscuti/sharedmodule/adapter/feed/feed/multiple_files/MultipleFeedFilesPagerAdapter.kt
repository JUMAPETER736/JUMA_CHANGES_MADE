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

    private var mixedFeedUploadDataClass: MutableList<MixedFeedUploadDataClass> = mutableListOf()
    var position = 0
    var fragment : MixedFeedFilesUploadFragment? = null
    fun setMixedFeedUploadDataClass(mixedFeedUploadDataClass: MixedFeedUploadDataClass) {
        this.mixedFeedUploadDataClass.clear()
        this.mixedFeedUploadDataClass.add(mixedFeedUploadDataClass)
    }

    fun setMixedFeedUploadDataClass2(mixedFeedUploadDataClass: MutableList<MixedFeedUploadDataClass>) {
        Log.d(TAG, "setMixedFeedUploadDataClass2: size b4 clear: ${mixedFeedUploadDataClass.size}")
        this.mixedFeedUploadDataClass.clear()
        Log.d(TAG, "setMixedFeedUploadDataClass2: size after clear: ${mixedFeedUploadDataClass.size}")
        this.mixedFeedUploadDataClass.addAll(mixedFeedUploadDataClass)
        Log.d(TAG, "setMixedFeedUploadDataClass2: size after add: ${mixedFeedUploadDataClass.size}")
    }
    fun setBackPressedCallback(backPressedCallback: OnBackPressedCallback) {
        this.backPressedCallback = backPressedCallback
    }

    fun setFeedTextViewFragmentInterface(feedTextViewFragmentInterface: FeedTextViewFragmentInterface) {
        this.feedTextViewFragmentInterface = feedTextViewFragmentInterface
    }
    override fun getItemCount(): Int = mixedFeedUploadDataClass.size

    override fun createFragment(position: Int): Fragment {
        fragment = MixedFeedFilesUploadFragment()

        this.position = position
        fragment!!.arguments = Bundle().apply {
//            putString("videoUri", videoUrls[position])
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

}
