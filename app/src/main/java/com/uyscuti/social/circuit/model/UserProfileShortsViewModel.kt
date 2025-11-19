package com.uyscuti.social.circuit.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.uyscuti.social.network.api.response.getallshorts.Post
import com.uyscuti.social.network.api.response.getfavoriteshorts.BookmarkedPost
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.core.models.BookmarkedShortsEntity
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class UserProfileShortsViewModel @Inject constructor(
    private val retrofitInstance: RetrofitInstance) :
    ViewModel() {
    private var userProfileShortsLiveData: MutableLiveData<List<UserShortsEntity>?> =
        MutableLiveData()

    private var userProfileFavoriteShortsLiveData: MutableLiveData<List<BookmarkedShortsEntity>?> =
        MutableLiveData()

    private var userShortsLiveData: MutableLiveData<List<ShortsEntity>> = MutableLiveData()

    private val onErrorFeedBack: MutableLiveData<String> = MutableLiveData()

    var firstPageLoaded = false

    var isResuming = false

    var mutableShortsList = mutableListOf<UserShortsEntity>()
    var mutableFavoriteShortsList = mutableListOf<UserShortsEntity>()
    var shortsToRemove = mutableListOf<UserShortsEntity>()


    var isLiked: Boolean = false
    var isFavorite: Boolean = false
    var totalLikes: Int = 0

    val followList = mutableListOf<ShortsEntityFollowList>()


    fun getUserProfileShortsObserver(): MutableLiveData<List<UserShortsEntity>?> {
        return userProfileShortsLiveData
    }

    fun getUserProfileFavoriteShortsObserver(): MutableLiveData<List<BookmarkedShortsEntity>?> {
        return userProfileFavoriteShortsLiveData
    }




    fun getShortsObserver(): MutableLiveData<List<ShortsEntity>> {
        return userShortsLiveData
    }

    fun getOnErrorFeedBackObserver(): MutableLiveData<String> {
        return onErrorFeedBack
    }
    fun getUserProfileFavoriteShorts(page: Int) {

        Log.d("UserProfileViewModel", "getUserProfileShorts: current page is $page")
        if (page == 1 && firstPageLoaded) {

            Log.d("UserProfileViewModel", "First page already loaded , returning....")
//            return
        }

        if (page == 1) {
            firstPageLoaded = true
        }

        viewModelScope.launch(Dispatchers.IO) {

            try {
                val response = retrofitInstance.apiService.getFavoriteShorts(page.toString())
                val responseBody = response.body()
                val shortsEntity =
                    responseBody?.data?.bookmarkedPosts?.let { serverResponseToBookmarkedPost(it) }

                userProfileFavoriteShortsLiveData.postValue(shortsEntity)

                if (shortsEntity == null) {
                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@withContext, "User Data is empty", Toast.LENGTH_SHORT).show(
                        onErrorFeedBack.postValue("User Data is empty")
                    }
                }

            } catch (e: Exception) {
                Log.e("UserProfileShortsViewModel", "Exception: ${e.message}")
                onErrorFeedBack.postValue("Error connecting to server.....check internet connection")
                e.printStackTrace()
            }

        }
    }

    fun getUserProfileShorts(page: Int) {

        Log.d("UserProfileViewModel", "getUserProfileShorts: current page is $page")
        if (page == 1 && firstPageLoaded) {

            Log.d("UserProfileViewModel", "First page already loaded , returning....")
//            return
        }

        if (page == 1) {
            firstPageLoaded = true
        }

        viewModelScope.launch(Dispatchers.IO) {

            try {
                val response = retrofitInstance.apiService.myShorts(page.toString())
                val responseBody = response.body()
                val shortsEntity = responseBody?.data?.posts?.let { serverResponseToUserEntity(it) }

                val hasNextPage = responseBody!!.data.hasNextPage
                Log.d("getUserProfileShorts", "getUserProfileShorts: has next page $hasNextPage")
                userProfileShortsLiveData.postValue(shortsEntity)



                if (shortsEntity == null) {
                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@withContext, "User Data is empty", Toast.LENGTH_SHORT).show(
                        onErrorFeedBack.postValue("User Data is empty")
                    }
                }

            } catch (e: Exception) {
                Log.e("UserProfileShortsViewModel", "Exception: ${e.message}")
                onErrorFeedBack.postValue("Error connecting to server.....check internet connection")
                e.printStackTrace()
            }

        }
    }



    fun getShorts(page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = retrofitInstance.apiService.getShorts(page.toString())
            val responseBody = response.body()
            val shortsEntity =
                responseBody?.data?.posts?.posts?.let { serverResponseToShortsEntity(it) }

            userShortsLiveData.postValue(shortsEntity!!)

        }
    }

    suspend fun likeUnLikeShort(shortOwnerId: String) {
        val TAG = "likeUnLikeShort"
        try {
            val response = retrofitInstance.apiService.likeUnLikeShort(shortOwnerId)
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    TAG,
                    "likeUnLikeShort ${responseBody?.data!!.isLiked}"
                )
            } else {
                Log.d(TAG, "Error: ${response.message()}")


            }

        } catch (e: HttpException) {
            Log.d(TAG, "Http Exception ${e.message}")




        } catch (e: IOException) {
            Log.d(TAG, "IOException ${e.message}")



        }
    }


    private fun serverResponseToUserEntity(serverResponse: List<Post>): List<UserShortsEntity> {
        return serverResponse.map { serverResponseItem ->
            UserShortsEntity(
                __v = serverResponseItem.__v,
                _id = serverResponseItem._id,
                content = serverResponseItem.content,
                author = serverResponseItem.author,
                comments = serverResponseItem.comments,
                createdAt = serverResponseItem.createdAt,
                images = serverResponseItem.images,
                isBookmarked = serverResponseItem.isBookmarked,
                isLiked = serverResponseItem.isLiked,
                likes = serverResponseItem.likes,
                tags = serverResponseItem.tags,
                updatedAt = serverResponseItem.updatedAt,
                thumbnail = serverResponseItem.thumbnail
                // map other properties...
            )
        }
    }

    private fun serverResponseToBookmarkedPost(serverResponse: List<BookmarkedPost>): List<BookmarkedShortsEntity> {
        return serverResponse.map { serverResponseItem ->
            BookmarkedShortsEntity(
                __v = serverResponseItem.__v,
                _id = serverResponseItem._id,
                content = serverResponseItem.content,
                author = serverResponseItem.author,
                comments = serverResponseItem.comments,
                createdAt = serverResponseItem.createdAt,
                images = serverResponseItem.images,
                isBookmarked = serverResponseItem.isBookmarked,
                isLiked = serverResponseItem.isLiked,
                likes = serverResponseItem.likes,
                tags = serverResponseItem.tags,
                updatedAt = serverResponseItem.updatedAt,
                thumbnail = serverResponseItem.thumbnail
                // map other properties...
            )
        }
    }

    private fun serverResponseToShortsEntity(serverResponse: List<Post>): List<ShortsEntity> {
        return serverResponse.map { serverResponseItem ->
            ShortsEntity(
                __v = serverResponseItem.__v,
                _id = serverResponseItem._id,
                content = serverResponseItem.content,
                author = serverResponseItem.author,
                comments = serverResponseItem.comments,
                createdAt = serverResponseItem.createdAt,
                images = serverResponseItem.images,
                isBookmarked = serverResponseItem.isBookmarked,
                isLiked = serverResponseItem.isLiked,
                likes = serverResponseItem.likes,
                tags = serverResponseItem.tags,
                updatedAt = serverResponseItem.updatedAt,
                thumbnail = serverResponseItem.thumbnail
                // map other properties...
            )
        }
    }

}