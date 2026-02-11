package com.uyscuti.sharedmodule.viewmodels.feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uyscuti.sharedmodule.model.feed.RefreshFeedData
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "GetFeedViewModel"

@HiltViewModel
class GetFeedViewModel @Inject constructor(private val retrofitInstance: RetrofitInstance) :
    ViewModel() {

    private val isDataAvailable = MutableLiveData<Boolean>()
    private val singleFeed = MutableLiveData<Boolean>()
    private val isFavoriteFeedDataAvailable = MutableLiveData<Boolean>()
    private val isMyFeedDataAvailable = MutableLiveData<Boolean>()

    var allFeedDataLastViewPosition = -1
    var totalFeed = 0

    val isFeedDataAvailable: LiveData<Boolean>
        get() = isDataAvailable

    val isSingleFeedAvailable: LiveData<Boolean>
        get() = singleFeed

    val isFavoritesFeedDataAvailable: LiveData<Boolean>
        get() = isFavoriteFeedDataAvailable

    val isMineFeedDataAvailable: LiveData<Boolean>
        get() = isMyFeedDataAvailable

    private var allFeedRepostData: MutableList<com.uyscuti.social.network.api.response.posts.OriginalPost> = mutableListOf()
    private var allFeedData: MutableList<com.uyscuti.social.network.api.response.posts.Post> = mutableListOf()
    private var allFavoriteFeedData: MutableList<com.uyscuti.social.network.api.response.posts.Post> = mutableListOf()
    private var myFeedData: MutableList<com.uyscuti.social.network.api.response.posts.Post> = mutableListOf()
    private var follow: MutableList<com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList> = mutableListOf()
    private val _myData = MutableLiveData<RefreshFeedData>()
    val myData: LiveData<RefreshFeedData> get() = _myData

    var isResuming = false

    fun setIsDataAvailable(resuming: Boolean) {
        isDataAvailable.value = resuming
    }

    fun setSingleFeedAvailable(resuming: Boolean) {
        singleFeed.value = resuming
    }

    fun setIsFeedDataAvailable(resuming: Boolean) {
        isFavoriteFeedDataAvailable.value = resuming
    }

    fun getAllFeedData(): MutableList<com.uyscuti.social.network.api.response.posts.Post> {
        return allFeedData
    }

    fun getAllFavoriteFeedData(): MutableList<com.uyscuti.social.network.api.response.posts.Post> {
        return allFavoriteFeedData
    }

    fun getMyFeedData(): MutableList<com.uyscuti.social.network.api.response.posts.Post> {
        return myFeedData
    }

    fun addAllFeedData(allFeedData: MutableList<com.uyscuti.social.network.api.response.posts.Post>) {
        Log.d(TAG, "addAllFeedData: $allFeedData")
        allFeedData.let { newData ->
            for (post in newData) {
                val existingPost = this.allFeedData.find { it._id == post._id }
                if (existingPost == null) {
                    Log.d(TAG, "addAllFeedData: post not in all")
                    this.allFeedData.add(post)
                } else {
                    Log.d(TAG, "addAllFeedData: feed already exists ${existingPost._id}")
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            setIsDataAvailable(true)
        }
    }

    fun addSingleAllFeedData(allFeedData: com.uyscuti.social.network.api.response.posts.Post) {
        Log.d(TAG, "addAllFeedData: $allFeedData")
        this.allFeedData.add(0, allFeedData)
        CoroutineScope(Dispatchers.Main).launch {
            setSingleFeedAvailable(true)
        }
    }

    fun getSingleAllFeedData(): com.uyscuti.social.network.api.response.posts.Post {
        return allFeedData[0]
    }

    fun addAllFavoriteFeedData(allFeedData: MutableList<com.uyscuti.social.network.api.response.posts.Post>) {
        Log.d(TAG, "addAllFeedData: $allFeedData")
        allFeedData.let { newData ->
            for (post in newData) {
                val existingPost = this.allFavoriteFeedData.find { it._id == post._id }
                if (existingPost == null) {
                    Log.d(TAG, "addAllFeedData: post not in all")
                    this.allFavoriteFeedData.add(post)
                } else {
                    Log.d(TAG, "addAllFeedData: feed already exists ${existingPost._id}")
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            setIsFeedDataAvailable(true)
        }
    }

    
    // Synchronized bookmark toggle across all feeds
    fun toggleBookmarkInAllFeeds(postId: String, isBookmarked: Boolean, bookmarkCount: Int) {
        Log.d(TAG, "toggleBookmarkInAllFeeds: postId=$postId, isBookmarked=$isBookmarked, count=$bookmarkCount")

        // Update in allFeedData
        allFeedData.find { it._id == postId }?.let { post ->
            post.isBookmarked = isBookmarked
            post.bookmarkCount = bookmarkCount
            Log.d(TAG, "Updated bookmark in allFeedData for post: $postId")
        }

        // Update in myFeedData
        myFeedData.find { it._id == postId }?.let { post ->
            post.isBookmarked = isBookmarked
            post.bookmarkCount = bookmarkCount
            Log.d(TAG, "Updated bookmark in myFeedData for post: $postId")
        }

        // Handle allFavoriteFeedData
        if (isBookmarked) {
            // Check if it's already in favorites
            val existsInFavorites = allFavoriteFeedData.any { it._id == postId }
            if (!existsInFavorites) {
                // Find post from allFeedData or myFeedData to add to favorites
                val postToAdd = allFeedData.find { it._id == postId }
                    ?: myFeedData.find { it._id == postId }

                postToAdd?.let { post ->
                    post.isBookmarked = true
                    post.bookmarkCount = bookmarkCount
                    allFavoriteFeedData.add(0, post.copy()) // Add copy to avoid reference issues
                    Log.d(TAG, "Added post to favorites: $postId")
                }
            } else {
                // Update existing favorite
                allFavoriteFeedData.find { it._id == postId }?.let { post ->
                    post.isBookmarked = true
                    post.bookmarkCount = bookmarkCount
                    Log.d(TAG, "Updated existing favorite: $postId")
                }
            }
        } else {
            // Remove from favorites if unbookmarked
            val position = allFavoriteFeedData.indexOfFirst { it._id == postId }
            if (position != -1) {
                allFavoriteFeedData.removeAt(position)
                Log.d(TAG, "Removed post from favorites at position $position: $postId")
            }
        }
    }

    // Replace your existing toggleRepostInAllFeeds method with this:
    fun toggleRepostInAllFeeds(postId: String, isReposted: Boolean, repostCount: Int) {
        Log.d(TAG, "toggleRepostInAllFeeds: postId=$postId, isReposted=$isReposted, count=$repostCount")

        // Update in allFeedData
        allFeedData.find { it._id == postId }?.let { post ->
            post.isReposted = isReposted

            // Only update count if this is NOT a repost wrapper
            if (post.originalPost.isNullOrEmpty()) {
                post.repostCount = repostCount
                Log.d(TAG, "Updated repost count in allFeedData for regular post: $postId")
            } else {
                Log.d(TAG, "Skipped repost count update for repost wrapper: $postId (preserving original post's count)")
            }
        }

        // Update in myFeedData
        myFeedData.find { it._id == postId }?.let { post ->
            post.isReposted = isReposted

            if (post.originalPost.isNullOrEmpty()) {
                post.repostCount = repostCount
                Log.d(TAG, "Updated repost count in myFeedData for regular post: $postId")
            } else {
                Log.d(TAG, "Skipped repost count update for repost wrapper: $postId")
            }
        }

        // Update in allFavoriteFeedData
        allFavoriteFeedData.find { it._id == postId }?.let { post ->
            post.isReposted = isReposted

            if (post.originalPost.isNullOrEmpty()) {
                post.repostCount = repostCount
                Log.d(TAG, "Updated repost count in allFavoriteFeedData for regular post: $postId")
            } else {
                Log.d(TAG, "Skipped repost count update for repost wrapper: $postId")
            }
        }
    }

    // Replace your existing toggleShareInAllFeeds method with this:
    fun toggleShareInAllFeeds(postId: String, isShared: Boolean, shareCount: Int) {
        Log.d(TAG, "toggleShareInAllFeeds: postId=$postId, isShared=$isShared, count=$shareCount")

        // Update in allFeedData
        allFeedData.find { it._id == postId }?.let { post ->
            post.isShared = isShared

            // Only update count if this is NOT a repost wrapper
            if (post.originalPost.isNullOrEmpty()) {
                post.shareCount = shareCount
                Log.d(TAG, "Updated share count in allFeedData for regular post: $postId")
            } else {
                Log.d(TAG, "Skipped share count update for repost wrapper: $postId (preserving original post's count)")
            }
        }

        // Update in myFeedData
        myFeedData.find { it._id == postId }?.let { post ->
            post.isShared = isShared

            if (post.originalPost.isNullOrEmpty()) {
                post.shareCount = shareCount
                Log.d(TAG, "Updated share count in myFeedData for regular post: $postId")
            } else {
                Log.d(TAG, "Skipped share count update for repost wrapper: $postId")
            }
        }

        // Update in allFavoriteFeedData
        allFavoriteFeedData.find { it._id == postId }?.let { post ->
            post.isShared = isShared

            if (post.originalPost.isNullOrEmpty()) {
                post.shareCount = shareCount
                Log.d(TAG, "Updated share count in allFavoriteFeedData for regular post: $postId")
            } else {
                Log.d(TAG, "Skipped share count update for repost wrapper: $postId")
            }
        }
    }

    //  Get post by ID from any feed
    fun getPostById(postId: String): com.uyscuti.social.network.api.response.posts.Post? {
        return allFeedData.find { it._id == postId }
            ?: allFavoriteFeedData.find { it._id == postId }
            ?: myFeedData.find { it._id == postId }
    }

    // Add to favorites with duplicate check
    fun addFavoriteFeed(position: Int, feed: com.uyscuti.social.network.api.response.posts.Post): Boolean {
        try {
            // Check if already exists
            val exists = allFavoriteFeedData.any { it._id == feed._id }
            if (exists) {
                Log.w(TAG, "addFavoriteFeed: Feed already exists in favorites: ${feed._id}")
                return false
            }

            Log.d(TAG, "addFavoriteFeed: add ${allFavoriteFeedData.size}")
            this.allFavoriteFeedData.add(0, feed)

            // Also update in other feeds
            toggleBookmarkInAllFeeds(feed._id, true, feed.bookmarkCount)

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding favorite feed", e)
            return false
        }
    }

    // Remove from favorites and sync
    fun removeFavoriteFeed(position: Int) {
        if (position in allFavoriteFeedData.indices) {
            val post = allFavoriteFeedData[position]
            this.allFavoriteFeedData.removeAt(position)

            // Update bookmark status in other feeds
            toggleBookmarkInAllFeeds(post._id, false, maxOf(0, post.bookmarkCount - 1))

            Log.d(TAG, "removeFavoriteFeed: Removed and synced post: ${post._id}")
        }
    }

    fun removeAllFeedFragment(position: Int) {
        this.allFeedData.removeAt(position)
    }

    fun removeMyFeed(position: Int) {
        this.myFeedData.removeAt(position)
    }

    // Update with sync
    fun updateForFavoriteFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        if (position in allFavoriteFeedData.indices) {
            allFavoriteFeedData[position] = data

            // Sync bookmark state across all feeds
            toggleBookmarkInAllFeeds(data._id, data.isBookmarked, data.bookmarkCount)
        }
    }

    // Update with sync
    fun updateForAllFeedFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        if (position in allFeedData.indices) {
            allFeedData[position] = data

            // Sync bookmark state across all feeds
            toggleBookmarkInAllFeeds(data._id, data.isBookmarked, data.bookmarkCount)
        }
    }

    fun updateMyFeedData(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        if (position in myFeedData.indices) {
            myFeedData[position] = data

            // Sync bookmark state across all feeds
            toggleBookmarkInAllFeeds(data._id, data.isBookmarked, data.bookmarkCount)
        }
    }

    fun getPositionById(itemId: String): Int {
        for (i in allFavoriteFeedData.indices) {
            if (allFavoriteFeedData[i]._id == itemId) {
                return i
            }
        }
        return -1
    }

    fun getMyFeedPositionById(itemId: String): Int {
        for (i in myFeedData.indices) {
            if (myFeedData[i]._id == itemId) {
                return i
            }
        }
        return -1
    }

    fun getAllFeedDataByPosition(position: Int): com.uyscuti.social.network.api.response.posts.Post {
        return allFeedData[position]
    }

    fun getAllFeedRepostDataByPosition(position: Int): com.uyscuti.social.network.api.response.posts.OriginalPost {
        return allFeedRepostData[position]
    }

    fun getAllFeedDataPositionById(itemId: String): Int {
        for (i in allFeedData.indices) {
            if (allFeedData[i]._id == itemId) {
                return i
            }
        }
        return -1
    }

    fun setRefreshMyData(position: Int, booleanValue: Boolean) {
        _myData.value = RefreshFeedData(position, booleanValue)
    }

    fun getFollowList(): List<com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList> {
        return follow
    }

    fun filterOutUserPosts(userId: String) {
        val iterator = allFeedData.iterator()
        while (iterator.hasNext()) {
            val post = iterator.next()
            val authorId = post.author?.account?._id
            if (authorId == userId) {
                iterator.remove()
                Log.d(TAG, "filterOutUserPosts: Removed own post with ID: ${post._id}")
            }
        }
    }

    fun clearAllFeedData() {
        allFeedData.clear()
    }

}