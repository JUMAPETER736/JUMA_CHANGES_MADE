package com.uyscuti.sharedmodule.viewmodels.feed

import com.uyscuti.social.network.api.response.profile.followingList.BaseResponse
import com.uyscuti.social.network.api.response.profile.followingList.CloseFriendStatusResponse
import com.uyscuti.social.network.api.response.profile.followingList.CloseFriendsListResponse
import com.uyscuti.social.network.api.response.profile.followingList.FavoriteStatusResponse
import com.uyscuti.social.network.api.response.profile.followingList.FavoritesListResponse
import com.uyscuti.social.network.api.response.profile.followingList.MutedPostsListResponse
import com.uyscuti.social.network.api.response.profile.followingList.MutedPostsStatusResponse
import com.uyscuti.social.network.api.response.profile.followingList.MutedStoriesListResponse
import com.uyscuti.social.network.api.response.profile.followingList.MutedStoriesStatusResponse
import com.uyscuti.social.network.api.response.profile.followingList.RestrictedListResponse
import com.uyscuti.social.network.api.response.profile.followingList.RestrictedStatusResponse
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRelationshipsRepository @Inject constructor(
    private val retrofitInstance: RetrofitInstance
) {

    // ==================== CLOSE FRIENDS ====================
    suspend fun getCloseFriends(): Response<CloseFriendsListResponse> {
        return retrofitInstance.apiService.getCloseFriends()
    }

    suspend fun checkCloseFriendStatus(userId: String): Response<CloseFriendStatusResponse> {
        return retrofitInstance.apiService.checkCloseFriendStatus(userId)
    }

    suspend fun addToCloseFriends(userId: String): Response<BaseResponse> {
        return retrofitInstance.apiService.addToCloseFriends(userId)
    }

    suspend fun removeFromCloseFriends(userId: String): Response<BaseResponse> {
        return retrofitInstance.apiService.removeFromCloseFriends(userId)
    }

    // ==================== MUTE POSTS ====================
    suspend fun getMutedPostsUsers(): Response<MutedPostsListResponse> {
        return retrofitInstance.apiService.getMutedPostsUsers()
    }

    suspend fun checkMutedPostsStatus(userId: String): Response<MutedPostsStatusResponse> {
        return retrofitInstance.apiService.checkMutedPostsStatus(userId)
    }

    suspend fun mutePosts(userId: String): Response<BaseResponse> {
        return retrofitInstance.apiService.mutePosts(userId)
    }

    suspend fun unMutePosts(userId: String): Response<BaseResponse> {
        return retrofitInstance.apiService.unMutePosts(userId)
    }

    // ==================== MUTE STORIES ====================
    suspend fun getMutedStoriesUsers(): Response<MutedStoriesListResponse> {
        return retrofitInstance.apiService.getMutedStoriesUsers()
    }

    suspend fun checkMutedStoriesStatus(userId: String): Response<MutedStoriesStatusResponse> {
        return retrofitInstance.apiService.checkMutedStoriesStatus(userId)
    }

    suspend fun muteStories(userId: String): Response<BaseResponse> {
        return retrofitInstance.apiService.muteStories(userId)
    }

    suspend fun unMuteStories(userId: String): Response<BaseResponse> {
        return retrofitInstance.apiService.unMuteStories(userId)
    }

    // ==================== FAVORITES ====================
    suspend fun getFavorites(): Response<FavoritesListResponse> {
        return retrofitInstance.apiService.getFavorites()
    }

    suspend fun checkFavoriteStatus(userId: String): Response<FavoriteStatusResponse> {
        return retrofitInstance.apiService.checkFavoriteStatus(userId)
    }

    suspend fun addToFavorites(userId: String): Response<BaseResponse> {
        return retrofitInstance.apiService.addToFavorites(userId)
    }

    suspend fun removeFromFavorites(userId: String): Response<BaseResponse> {
        return retrofitInstance.apiService.removeFromFavorites(userId)
    }

    // ==================== RESTRICT ====================
    suspend fun getRestrictedUsers(): Response<RestrictedListResponse> {
        return retrofitInstance.apiService.getRestrictedUsers()
    }

    suspend fun checkRestrictedStatus(userId: String): Response<RestrictedStatusResponse> {
        return retrofitInstance.apiService.checkRestrictedStatus(userId)
    }

    suspend fun restrictUser(userId: String): Response<BaseResponse> {
        return retrofitInstance.apiService.restrictUser(userId)
    }

    suspend fun unRestrictUser(userId: String): Response<BaseResponse> {
        return retrofitInstance.apiService.unRestrictUser(userId)
    }
}