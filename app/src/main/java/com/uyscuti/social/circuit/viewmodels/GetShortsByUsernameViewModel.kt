package com.uyscuti.social.circuit.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.circuit.User_Interface.fragments.forshorts.FollowersFollowingCount
import com.uyscuti.social.network.api.response.getallshorts.Post
import com.uyscuti.social.network.api.response.otherusersprofileshorts.Data
//import com.uyscut.network.api.response.otherusersprofileshorts.Post
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
//import com.uyscut.network.api.response.getallshorts.Post
import kotlinx.coroutines.GlobalScope
import retrofit2.HttpException
import java.io.IOException


private const val TAG = "GetShortsByUsernameViewModel"

@HiltViewModel
class GetShortsByUsernameViewModel @Inject constructor(private val retrofitInstance: RetrofitInstance) :
    ViewModel() {

    private val otherUsersShortsLiveData: MutableLiveData<Data> = MutableLiveData()
    private val followersFollowingCount: MutableLiveData<FollowersFollowingCount> =
        MutableLiveData()

    //    private val followingCount: MutableLiveData<String> = MutableLiveData()
    private val usersShortsLiveData: MutableLiveData<List<UserShortsEntity>?> = MutableLiveData()
    private val onErrorFeedBack: MutableLiveData<String> = MutableLiveData()

    var postCount = 0
//    private var totalPostCount: MutableLiveData<Int> = MutableLiveData(0)

    var refreshPostCount =  MutableLiveData<Boolean>()

    val isRefreshPostCount: MutableLiveData<Boolean>
        get() = refreshPostCount

    fun setIsRefreshPostCount(resuming: Boolean) {
        refreshPostCount.value = resuming
    }
    var followersCount = 0
    var followingCount = 0
    fun getUserProfileShortsObserver(): MutableLiveData<Data> {
        return otherUsersShortsLiveData
    }

    fun getFollowersCount(): MutableLiveData<FollowersFollowingCount> {
        return followersFollowingCount
    }

    fun getUsersShortsObserver(): MutableLiveData<List<UserShortsEntity>?> {
        return usersShortsLiveData
    }

    fun getOnErrorFeedBackObserver(): MutableLiveData<String> {
        return onErrorFeedBack
    }

    fun getOtherUsersProfileShorts(username: String) {
        viewModelScope.launch(Dispatchers.IO) {

            try {
                val response = retrofitInstance.apiService.getShortsByUsername(username)
                val feedResponse = retrofitInstance.apiService.getMyFeed(
                    1.toString()
                )
                val responseBody = response.body()

                val feedResponseBody = feedResponse.body()


//                val shortsEntity = responseBody?.data?.posts?.let { serverResponseToUserEntity(it) }

                if (responseBody != null) {
                    otherUsersShortsLiveData.postValue(responseBody.data)
                    if (feedResponseBody != null) {
                        responseBody.data.totalPosts += feedResponseBody.data.posts.totalPosts
                        otherUsersShortsLiveData.postValue(responseBody.data)
                    }
                } else {
                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@withContext, "User Data is empty", Toast.LENGTH_SHORT).show()
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

    fun getMyFeed(onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        Log.d(
            TAG,
            "getMyFeed:"
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitInstance.apiService.getMyFeed(
                    1.toString()
                )
                val responseBody = response.body()
                if (responseBody != null) {

                    onSuccess(responseBody.data.posts.totalPosts)
                } else {
                    onError("Failed to retrieve data")
                }

            } catch (e: Exception) {
                Log.e(TAG, "comment: $e")
                Log.e(TAG, "comment: ${e.message}")
                var zero = 0
                e.printStackTrace()
            }
        }

    }

    //    fun getMyFeed()
    fun getUsersShorts(username: String, page: Int) {
        viewModelScope.launch(Dispatchers.IO) {

            try {
                val response = retrofitInstance.apiService.getShortsByUsernameWithPage(
                    username,
                    page.toString()
                )
                val responseBody = response.body()


                val shortsEntity = responseBody?.data?.posts?.let { serverResponseToUserEntity(it) }
//                val postConvert = responseBody?.data?.posts?.let { serverResponseToUserEntity(it) }

                if (responseBody != null) {
                    usersShortsLiveData.postValue(shortsEntity)

                } else {
                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@withContext, "User Data is empty", Toast.LENGTH_SHORT).show()
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

    @OptIn(DelicateCoroutinesApi::class)
    fun getUserProfile() {

        GlobalScope.launch {
            val response = try {

                retrofitInstance.apiService.getMyProfile()

            } catch (e: HttpException) {
                Log.d("RetrofitActivity", "Http Exception ${e.message}")
                onErrorFeedBack.postValue("HTTP error. Please try again.")
//                requireActivity().runOnUiThread {
////                    Toast.makeText(
////                        this@MainActivity,
////                        "HTTP error. Please try again.",
////                        Toast.LENGTH_SHORT
////                    ).show()
//
//                    MotionToast.createToast(
//                        requireActivity(),
//                        "Failed To Retrieve Data☹️",
//                        "HTTP error. Please try again.",
//                        MotionToastStyle.ERROR,
//                        MotionToast.GRAVITY_BOTTOM,
//                        MotionToast.LONG_DURATION,
//                        ResourcesCompat.getFont(requireActivity(), R.font.helvetica_regular)
//                    )
//                }

                return@launch
            } catch (e: IOException) {
                Log.d("RetrofitActivity", "IOException ${e.message}")
                onErrorFeedBack.postValue("HTTP error. Please try again.")
//                requireActivity().runOnUiThread {
////                    Toast.makeText(
////                        this@MainActivity,
////                        "Network error. Please try again.",
////                        Toast.LENGTH_SHORT
////                    ).show()
//                    MotionToast.createToast(
//                        requireActivity(),
//                        "Failed To Retrieve Data☹️",
//                        "Network error. Please try again.",
//                        MotionToastStyle.ERROR,
//                        MotionToast.GRAVITY_BOTTOM,
//                        MotionToast.LONG_DURATION,
//                        ResourcesCompat.getFont(requireActivity(), R.font.helvetica_regular)
//                    )
//
//                }
                return@launch
            } finally {
                // Ensure the progress bar is hidden in case of an error
//                withContext(Dispatchers.Main) {
//                    dismissLoadingDialog()
//                }
            }

            if (response.isSuccessful) {
                val responseBody = response.body()
//                Log.d("UserProfile", "User profile ${responseBody?.data}")

                if (responseBody?.data != null) {

                    val fFCount = FollowersFollowingCount(
                        responseBody.data.followersCount.toString(),
                        responseBody.data.followingCount.toString()
                    )
                    followersFollowingCount.postValue(fFCount)
//                    followersFollowingCount.postValue(
//                    withContext(Dispatchers.Main) {
//                        shortsViewModel.followersCount = responseBody.data.followersCount
//                        shortsViewModel.followingCount = responseBody.data.followingCount
//                    }


//                    val editor = settings.edit()
//                    editor.putString("firstname", responseBody.data.firstName)
//                    editor.putString("lastname", responseBody.data.lastName)
//                    editor.putString("avatar", responseBody.data.account.avatar.url)
//                    editor.putString("bio", responseBody.data.bio)
//                    editor.apply()

//                    val myProfile = ProfileEntity(
//                        __v = responseBody.data.__v,
//                        _id = responseBody.data._id,
//                        bio = responseBody.data.bio,
//                        firstName = responseBody.data.firstName,
//                        lastName = responseBody.data.lastName,
//                        account = responseBody.data.account,
//                        createdAt = responseBody.data.createdAt,
//                        dob = responseBody.data.dob,
//                        countryCode = responseBody.data.countryCode,
//                        coverImage = responseBody.data.coverImage,
//                        updatedAt = responseBody.data.updatedAt,
//                        followersCount = responseBody.data.followersCount,
//                        isFollowing = responseBody.data.isFollowing,
//                        location = responseBody.data.location,
//                        owner = responseBody.data.owner,
//                        phoneNumber = responseBody.data.phoneNumber,
//                        followingCount = responseBody.data.followingCount
//                    )

//                    insertProfile(myProfile)
//                    Log.d("ProfileLocal", "To localDb $myProfile")

                } else {
                    Log.d("RetrofitActivity", "Response body or data is null")
                }
            }

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

}