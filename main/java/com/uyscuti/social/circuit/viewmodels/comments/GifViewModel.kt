package com.uyscuti.social.circuit.viewmodels.comments

import android.util.Log
import androidx.lifecycle.ViewModel
import com.uyscuti.social.circuit.model.GifResults
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class GifViewModel @Inject constructor(private val retrofitInterface: RetrofitInstance) :
    ViewModel() {
    // Example method to fetch data
//    suspend fun fetchData(): String {
//        // Simulate fetching data from a remote source or database
//        return withContext(Dispatchers.IO) {
//            // Perform data fetching operations here
//            // For example:
//            // fetchDataFromRepository()
//            "Sample Data"
//        }
//    }

//   suspend fun fetchData(
//        page: Int
//    ): GifResults {
//        val TAG = "fetchData"
//        try {
//            var hasNextPage: Boolean
//            val pageNumber = page + 1
//            withContext(Dispatchers.IO) {
//                // Handle UI-related tasks if needed
//                val response =
//                    retrofitInterface.apiService.getGif(page.toString())
//
//                val responseBody = response.body()
//
//                Log.d(TAG, "fetchData responseBody: $responseBody")
//                Log.d(TAG, "fetchData responseBody.data: ${responseBody?.data}")
//                val gifs = responseBody!!.data
//                return@withContext GifResults(gifs.gifs, gifs.hasNextPage, pageNumber)
////            return CommentReplyResults(commentsReplyViewModel.commentsReplyMutableList, hasNextPage, pageNumber)
//            }
//        } catch (e: Exception) {
//            Log.e("UserProfileShortsViewModel", "Exception: ${e.message}")
//
//            e.printStackTrace()
//        }
//
//        return GifResults(Collections.emptyList(), false, page)
//    }

    suspend fun fetchData(page: Int): GifResults {
        val TAG = "fetchData"
        try {
            return withContext(Dispatchers.IO) {
                val response = retrofitInterface.apiService.getGif(page.toString())
                val responseBody = response.body()

                Log.d(TAG, "fetchData responseBody: $responseBody")
                Log.d(TAG, "fetchData responseBody.data: ${responseBody?.data}")

                val gifs = responseBody!!.data
                GifResults(gifs.gifs, gifs.hasNextPage, page + 1)
            }
        } catch (e: Exception) {
            Log.e("fetchData", "Exception: ${e.message}")
            e.printStackTrace()
            return GifResults(Collections.emptyList(), false, page)
        }
    }

}

