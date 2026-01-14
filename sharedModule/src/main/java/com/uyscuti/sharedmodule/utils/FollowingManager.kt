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

    //Synchronous method to get from localStorage only
    fun getFollowingList(): Set<String> {
        val list = localStorage.getFollowingList()
        Log.d("FollowingManager", "Get Following List from localStorage: ${list.size} users")
        return list
    }

    //Async method to fetch from server
    suspend fun loadFollowingList(): Set<String> {
        // First, load from cache
        val cachedList = localStorage.getFollowingList()
        Log.d("FollowingManager", "Loaded ${cachedList.size} users from localStorage cache")

        // Update adapter cache immediately with cached data
        FeedAdapter.setCachedFollowingList(cachedList)

        // Then fetch fresh data from server
        val serverList = fetchFollowingListFromServer()

        // Return the server list (or cached list if server fetch failed)
        return if (serverList.isNotEmpty()) {
            Log.d("FollowingManager", "Returning server data: ${serverList.size} users")
            serverList
        } else {
            Log.d("FollowingManager", "Server fetch failed, returning cached data: ${cachedList.size} users")
            cachedList
        }
    }

    //  Returns the fetched list
    private suspend fun fetchFollowingListFromServer(): Set<String> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUsername = localStorage.getUsername()
                if (currentUsername.isNullOrEmpty()) {
                    Log.e("FollowingManager", "Username is null or empty")
                    return@withContext emptySet()
                }

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

                // Save to local storage if we got data
                if (followingUserIds.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        val json = Gson().toJson(followingUserIds.toList())
                        localStorage.saveFollowingList(json)
                        FeedAdapter.setCachedFollowingList(followingUserIds)
                        Log.d("FollowingManager", "Saved ${followingUserIds.size} following users to localStorage")
                    }
                }

                return@withContext followingUserIds

            } catch (e: Exception) {
                Log.e("FollowingManager", "Error fetching following list from server", e)
                return@withContext emptySet()
            }
        }
    }

    fun addToFollowing(userId: String) {
        val currentList = localStorage.getFollowingList().toMutableSet()
        currentList.add(userId)
        val json = Gson().toJson(currentList.toList())
        localStorage.saveFollowingList(json)

        // Update BOTH FeedAdapter cache methods
        FeedAdapter.setCachedFollowingList(currentList)
        FeedAdapter.addToFollowingCache(userId)

        Log.d("FollowingManager", "Added user $userId to following list (Total: ${currentList.size})")
    }

    fun removeFromFollowing(userId: String) {
        val currentList = localStorage.getFollowingList().toMutableSet()
        currentList.remove(userId)
        val json = Gson().toJson(currentList.toList())
        localStorage.saveFollowingList(json)

        // Update BOTH FeedAdapter cache methods
        FeedAdapter.setCachedFollowingList(currentList)
        FeedAdapter.removeFromFollowingCache(userId)

        Log.d("FollowingManager", "Removed user $userId from following list (Total: ${currentList.size})")
    }
}