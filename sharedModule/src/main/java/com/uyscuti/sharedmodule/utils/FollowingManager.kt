package com.uyscuti.sharedmodule.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections


class FollowingManager(private val context: Context) {

    private val localStorage = LocalStorage.getInstance(context)
    private var retrofitInstance: RetrofitInstance? = null

    init {
        try {
            retrofitInstance = RetrofitInstance(localStorage, context)
        } catch (e: Exception) {
            Log.e("FollowingManager", "Failed to initialize Retrofit", e)
        }
    }

    suspend fun loadFollowingList(): Set<String> {
        // First, load from cache
        val cachedList = localStorage.getFollowingList()
        Log.d("FollowingManager", "Loaded ${cachedList.size} users from cache")

        // Update adapter cache immediately
        FeedAdapter.setCachedFollowingList(cachedList)

        // Then fetch fresh data from server in background
        fetchFollowingListFromServer()

        return cachedList
    }

    private suspend fun fetchFollowingListFromServer() {
        withContext(Dispatchers.IO) {
            try {
                val currentUsername = localStorage.getUsername() ?: return@withContext
                val followingUserIds = mutableSetOf<String>()
                var currentPage = 1
                var hasMorePages = true

                Log.d("FollowingManager", "Fetching following list from server for: $currentUsername")

                while (hasMorePages) {
                    val response = retrofitInstance?.apiService?.getOtherUserFollowing(
                        username = currentUsername,
                        page = currentPage,
                        limit = 50
                    )

                    if (response?.isSuccessful == true) {
                        val users = response.body()?.data ?: Collections.emptyList()

                        // Extract user IDs
                        users.forEach { user ->
                            user._id?.let { followingUserIds.add(it) }
                        }

                        Log.d("FollowingManager", "Page $currentPage: Found ${users.size} users, Total: ${followingUserIds.size}")

                        // Check if there are more pages
                        hasMorePages = users.size >= 50
                        currentPage++

                        if (!hasMorePages) {
                            Log.d("FollowingManager", "Completed fetching all pages. Total: ${followingUserIds.size} users")
                        }
                    } else {
                        Log.e("FollowingManager", "Failed to fetch page $currentPage: ${response?.code()}")
                        hasMorePages = false
                    }
                }

                // Save to local storage
                withContext(Dispatchers.Main) {
                    if (followingUserIds.isNotEmpty()) {
                        val json = Gson().toJson(followingUserIds.toList())
                        localStorage.saveFollowingList(json)
                        FeedAdapter.setCachedFollowingList(followingUserIds)
                        Log.d("FollowingManager", "Saved ${followingUserIds.size} following users")
                    }
                }

            } catch (e: Exception) {
                Log.e("FollowingManager", "Error fetching following list", e)
            }
        }
    }

    fun addToFollowing(userId: String) {
        val currentList = localStorage.getFollowingList().toMutableSet()
        currentList.add(userId)
        val json = Gson().toJson(currentList.toList())
        localStorage.saveFollowingList(json)
        FeedAdapter.setCachedFollowingList(currentList)
        Log.d("FollowingManager", "Added user $userId to following list")
    }

    fun removeFromFollowing(userId: String) {
        val currentList = localStorage.getFollowingList().toMutableSet()
        currentList.remove(userId)
        val json = Gson().toJson(currentList.toList())
        localStorage.saveFollowingList(json)
        FeedAdapter.setCachedFollowingList(currentList)
        Log.d("FollowingManager", "Removed user $userId from following list")
    }
}