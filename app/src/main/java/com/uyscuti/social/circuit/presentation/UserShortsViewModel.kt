package com.uyscuti.social.circuit.presentation
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uyscuti.social.circuit.User_Interface.fragments.SHORTS
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.network.api.response.getallshorts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class UserShortsViewModel @Inject constructor(
    private val retrofitIns: RetrofitInstance
) : ViewModel() {
    var isLoading = false
    var isLastPage = false
    private var currentPage = 1
    private var shortsList = ArrayList<String>()

    private val _shortsData = MutableLiveData<List<ShortsEntity>>()
    val shortsData: LiveData<List<ShortsEntity>> get() = _shortsData

    private val _showToast = MutableLiveData<String>()
    val showToast: LiveData<String> get() = _showToast



    private fun serverResponseToEntity(serverResponse: List<Post>): List<ShortsEntity> {
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

    suspend fun loadMoreShorts() {
        if (isLoading || isLastPage) return

        isLoading = true
        try {

            val response = withContext(Dispatchers.IO) {
                retrofitIns.apiService.myShorts(currentPage.toString())
            }
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("AllShorts", "Shorts List in page $currentPage ${responseBody?.data}")

                val shortsEntity = responseBody?.data?.posts?.let { serverResponseToEntity(it) }

                shortsEntity?.let { entityList ->
                    withContext(Dispatchers.IO) {
                        for (entity in entityList) {
                            val images = entity.images
                            for (image in images) {
                                val imageUrl = image.url
                                Log.d(SHORTS, "imageUrl - $imageUrl")
                                shortsList.add(imageUrl)
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        isLoading = false
                        _shortsData.value = entityList
                        isLastPage = entityList.isEmpty()
                    }
                } ?: run {
                    Log.d(SHORTS, "Failed to parse shorts data")
                    isLoading = false
                }
            } else {
                Log.d("AllShorts", "Error: ${response.message()}")
                isLoading = false
                _showToast.value = response.message()
            }

        } catch (e: HttpException) {
            Log.d("AllShorts", "Http Exception ${e.message}")
            _showToast.value = "Failed to connect, please try again..."
        } catch (e: IOException) {
            Log.d("AllShorts", "IOException ${e.message}")
            _showToast.value = "Failed to connect, please try again..."
        }
    }

}
