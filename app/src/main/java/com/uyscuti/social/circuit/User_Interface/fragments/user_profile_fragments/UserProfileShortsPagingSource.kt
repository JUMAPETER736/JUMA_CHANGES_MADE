package com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments

import android.net.Uri
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.network.api.response.getallshorts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance

class UserProfileShortsPagingSource(private val retrofitInstance: RetrofitInstance): PagingSource<Int, UserShortsEntity>() {
    override fun getRefreshKey(state: PagingState<Int, UserShortsEntity>): Int? {

        return state.anchorPosition

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


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserShortsEntity> {
        return try {
            val nextPage: Int = params.key ?: FIRST_PAGE_INDEX
            val response = retrofitInstance.apiService.myShorts(nextPage.toString())

            val responseBody = response.body()

            var nextPageNumber: Int? = null
            val hasMoreShorts = responseBody?.data?.hasNextPage
            if(hasMoreShorts == true) {
                Log.d("Load", "has next page: ")
                val uri = Uri.parse(responseBody.data.nextPage.toString())
                val nextPageQuery = uri.getQueryParameter("page")
                nextPageNumber = nextPageQuery?.toInt()
            }
            var prevPageNumber: Int? = null
            if(responseBody?.data?.hasPrevPage == true) {
                val uri = Uri.parse(responseBody.data.nextPage.toString())
                val prevPageQuery = uri.getQueryParameter("page")

                prevPageNumber = prevPageQuery?.toInt()
            }
            val shortsEntity = responseBody?.data?.posts?.let { serverResponseToUserEntity(it) }

            LoadResult.Page(
                data = shortsEntity!!,
                prevKey = prevPageNumber,
                nextKey = nextPageNumber
            )
        }
        catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    companion object {
        private const val FIRST_PAGE_INDEX = 1
    }



}