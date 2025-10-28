package com.uyscuti.social.circuit.viewmodels.feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uyscuti.social.circuit.model.feed.RefreshFeedData
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.remove

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
    // Expose a LiveData object to observe changes externally
    val isFeedDataAvailable: LiveData<Boolean>
        get() = isDataAvailable

    val isSingleFeedAvailable: LiveData<Boolean>
        get() = singleFeed

    val isFavoritesFeedDataAvailable: LiveData<Boolean>
        get() = isFavoriteFeedDataAvailable

    val isMineFeedDataAvailable: LiveData<Boolean>
        get() = isMyFeedDataAvailable

    // Function to update the value of isResuming
    fun setIsDataAvailable(resuming: Boolean) {isDataAvailable.value = resuming

    }

    fun setSingleFeedAvailable(resuming: Boolean) {singleFeed.value = resuming
    }

    fun setIsFeedDataAvailable(resuming: Boolean) {isFavoriteFeedDataAvailable.value = resuming
    }

    fun setIsMyFeedDataAvailable(resuming: Boolean) {isMyFeedDataAvailable.value = resuming
    }



    private var allFeedRepostData: MutableList<com.uyscuti.social.network.api.response.posts.OriginalPost> = mutableListOf()
    private var allFeedData: MutableList<Post> = mutableListOf()
    private var allFavoriteFeedData: MutableList<Post> = mutableListOf()
    private var myFeedData: MutableList<Post> = mutableListOf()

    private var allFollowingFeedData: MutableList<Post> = mutableListOf()

    fun getAllFeedData(): MutableList<Post> {
        return allFeedData
    }

    fun getAllFavoriteFeedData(): MutableList<Post> {
        return allFavoriteFeedData
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

    fun getMyFeedData(): MutableList<Post> {
        return myFeedData
    }

    var isResuming = false

    fun addAllFeedData(allFeedData: MutableList<Post>) {
        Log.d(TAG, "addAllFeedData: $allFeedData")
        allFeedData.let { newData ->
            for (post in newData) {
                // Check if post with the same ID already exists in allFeedData
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


    fun addSingleAllFeedData(allFeedData: Post) {
        Log.d(TAG, "addAllFeedData: $allFeedData")
        this.allFeedData.add(0, allFeedData)
        CoroutineScope(Dispatchers.Main).launch {
            setSingleFeedAvailable(true)
        }
    }



    fun getSingleAllFeedData(): Post {
        return allFeedData[0]
    }

    fun addAllFavoriteFeedData(allFeedData: MutableList<Post>) {
        Log.d(TAG, "addAllFeedData: $allFeedData")
        allFeedData.let { newData ->
            for (post in newData) {
                // Check if post with the same ID already exists in allFeedData
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


    fun removeSingleAllFeedData(filePath: String) {
        Log.d(TAG, "removeSingleAllFeedData: Attempting to remove post with filePath: $filePath")
        val iterator = allFeedData.iterator()
        while (iterator.hasNext()) {
            val post = iterator.next()
            if (post.files.isNotEmpty() && post.files[0].localPath == filePath) {
                iterator.remove()
                Log.d(TAG, "removeSingleAllFeedData: Removed post with filePath: $filePath")
                CoroutineScope(Dispatchers.Main).launch {
                    setSingleFeedAvailable(true) // Notify observers of data change
                }
                return
            }
        }
        Log.w(TAG, "removeSingleAllFeedData: No post found with filePath: $filePath")
    }

    fun addFollowingFeedData(allFeedData: MutableList<Post>) {
        Log.d(TAG, "addAllFeedData: $allFeedData")

        allFeedData.let { newData ->
            for (post in newData) {
                // Check if post with the same ID already exists in allFeedData
                val existingPost = this.allFollowingFeedData.find { it._id == post._id }
                if (existingPost == null) {
                    Log.d(TAG, "addAllFeedData: post not in all")
                    this.allFollowingFeedData.add(post)

                } else {
                    Log.d(TAG, "addAllFeedData: feed already exists ${existingPost._id}")
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            setIsFeedDataAvailable(true)
        }
    }

    fun addMyFeedData(allFeedData: MutableList<Post>) {
        Log.d(TAG, "addAllFeedData: $allFeedData")

        allFeedData.let { newData ->
            for (post in newData) {
                // Check if post with the same ID already exists in allFeedData
                val existingPost = this.myFeedData.find { it._id == post._id }
                if (existingPost == null) {
                    Log.d(TAG, "addAllFeedData: post not in all")
                    this.myFeedData.add(post)
                } else {
                    Log.d(TAG, "addAllFeedData: feed already exists ${existingPost._id}")
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            setIsMyFeedDataAvailable(true)
        }
    }

    fun addFavoriteFeed(position: Int, feed: Post):Boolean {

        try {
            Log.d(TAG, "addFavoriteFeed: add ${allFavoriteFeedData.size}")
            this.allFavoriteFeedData.add(0, feed)
            return true  // Return true if adding feed was successful
        } catch (e: Exception) {
            Log.e(TAG, "Error adding favorite feed", e)
            return false  // Return false if an exception occurred
        }
    }

    fun addSingleFeedToAllFeed(position: Int, feed: Post):Boolean {
        try {
            Log.d(TAG, "addFavoriteFeed: add ${allFeedData.size}")
            this.allFeedData.add(0, feed)
            return true  // Return true if adding feed was successful
        } catch (e: Exception) {
            Log.e(TAG, "Error adding favorite feed", e)
            return false  // Return false if an exception occurred
        }
    }

    fun removeFavoriteFeed(position: Int) {
        this.allFavoriteFeedData.removeAt(position)
    }

    fun removeAllFeedFragment(position: Int) {
        this.allFeedData.removeAt(position)
    }
    fun removeMyFeed(position: Int) {
        this.myFeedData.removeAt(position)
    }
    fun clearFavoriteData() {
        allFavoriteFeedData.clear()
    }

    fun updateForFavoriteFragment(position: Int, data: Post) {
        allFavoriteFeedData[position] = data
    }
    fun updateForFavoritesFragment(position: Int, data: Post) {
        allFollowingFeedData
    }
    fun updateForAllFeedFragment(position: Int, data: Post) {
        allFeedData[position] = data
    }

    fun updateMyFeedData(position: Int, data: Post) {
        myFeedData[position] = data
    }

    fun getPositionById(itemId: String): Int {
        for (i in allFavoriteFeedData.indices) {
            if (allFavoriteFeedData[i]._id == itemId) {
                return i // Return position if ID matches
            }
        }
        return -1 // Return -1 if item with given ID is not found
    }
    fun getPositionsById(itemId: String): Int {
        for (i in allFavoriteFeedData.indices) {
            if (allFavoriteFeedData[i]._id == itemId) {
                return i // Return position if ID matches
            }
        }
        return -1 // Return -1 if item with given ID is not found
    }




    fun getMyFeedPositionById(itemId: String): Int {
        for (i in myFeedData.indices) {
            if (myFeedData[i]._id == itemId) {
                return i // Return position if ID matches
            }
        }
        return -1 // Return -1 if item with given ID is not found
    }

    fun getAllFeedDataByPosition(position: Int) : Post {
        return allFeedData[position]
    }

    fun getAllFeedRepostDataByPosition(position: Int): com.uyscuti.social.network.api.response.posts.OriginalPost {
        return allFeedRepostData[position]
    }


    fun getAllFeedDataPositionById(itemId: String): Int {
        for (i in allFeedData.indices) {
            if (allFeedData[i]._id == itemId) {
                return i // Return position if ID matches
            }
        }
        return -1 // Return -1 if item with given ID is not found
    }

    private val _myData = MutableLiveData<RefreshFeedData>()
    val myData: LiveData<RefreshFeedData> get() = _myData

    // Method to set the values
    fun setRefreshMyData(position: Int, booleanValue: Boolean) {
        _myData.value = RefreshFeedData(position, booleanValue)
    }


    private var follow: MutableList< com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList> = mutableListOf()
    fun setFollowList(follow: List< com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList>) {
        this.follow.addAll(follow)
    }
    fun addFollowToFollowList(follow:  com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList) {
        this.follow.add(follow)
    }
    fun getFollowList():List<com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList> {
        return follow
    }

}