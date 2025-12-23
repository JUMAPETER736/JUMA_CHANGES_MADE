package com.uyscuti.social.circuit.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uyscuti.social.network.api.response.feed.getallfeed.Post
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity

class FeedShortsViewModel : ViewModel() {

    private val _data = MutableLiveData<FollowUnFollowEntity>()
    private val _allFeedData = MutableLiveData<com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Post>()


    val data: LiveData<FollowUnFollowEntity> get() = _data
    val allFeedData: LiveData<com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Post> get() = _allFeedData

    fun setAllFeedData(feedData:com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Post) {
        _allFeedData.value = feedData
    }
    fun setData(newData: FollowUnFollowEntity) {
        _data.value = newData
    }
}
