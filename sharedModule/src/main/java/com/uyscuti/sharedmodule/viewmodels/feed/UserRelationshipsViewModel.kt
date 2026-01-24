package com.uyscuti.sharedmodule.viewmodels.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserRelationshipsViewModel @Inject constructor(
    private val repository: UserRelationshipsRepository // Using Repository now
) : ViewModel() {

    private val TAG = "UserRelationshipsVM"

    // Close Friends
    private val _closeFriendIds = MutableStateFlow<Set<String>>(emptySet())
    val closeFriendIds: StateFlow<Set<String>> = _closeFriendIds.asStateFlow()

    // Muted Posts
    private val _mutedPostsIds = MutableStateFlow<Set<String>>(emptySet())
    val mutedPostsIds: StateFlow<Set<String>> = _mutedPostsIds.asStateFlow()

    // Muted Stories
    private val _mutedStoriesIds = MutableStateFlow<Set<String>>(emptySet())
    val mutedStoriesIds: StateFlow<Set<String>> = _mutedStoriesIds.asStateFlow()

    // Favorites
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    // Restricted
    private val _restrictedIds = MutableStateFlow<Set<String>>(emptySet())
    val restrictedIds: StateFlow<Set<String>> = _restrictedIds.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load all relationship data in parallel
     */
    fun loadAllRelationships() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Load all relationships in parallel using async
                awaitAll(
                    async { loadCloseFriends() },
                    async { loadMutedPosts() },
                    async { loadMutedStories() },
                    async { loadFavorites() },
                    async { loadRestricted() }
                )

                Log.d(TAG, "All relationships loaded successfully")
                Log.d(TAG, "Close Friends: ${_closeFriendIds.value.size}")
                Log.d(TAG, "Muted Posts: ${_mutedPostsIds.value.size}")
                Log.d(TAG, "Muted Stories: ${_mutedStoriesIds.value.size}")
                Log.d(TAG, "Favorites: ${_favoriteIds.value.size}")
                Log.d(TAG, "Restricted: ${_restrictedIds.value.size}")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading relationships: ${e.message}", e)
                _error.value = "Failed to load relationship data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load close friends list
     */
    private suspend fun loadCloseFriends() {
        try {
            val response = repository.getCloseFriends() // Using repository
            if (response.isSuccessful && response.body() != null) {
                val closeFriends = response.body()!!.data
                _closeFriendIds.value = closeFriends.mapNotNull { it.user?._id }.toSet()
                Log.d(TAG, "Loaded ${_closeFriendIds.value.size} close friends")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading close friends: ${e.message}", e)
        }
    }

    /**
     * Load muted posts users
     */
    private suspend fun loadMutedPosts() {
        try {
            val response = repository.getMutedPostsUsers() // Using repository
            if (response.isSuccessful && response.body() != null) {
                val mutedPosts = response.body()!!.data
                _mutedPostsIds.value = mutedPosts.mapNotNull { it.user?._id }.toSet()
                Log.d(TAG, "Loaded ${_mutedPostsIds.value.size} muted posts users")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading muted posts: ${e.message}", e)
        }
    }

    /**
     * Load muted stories users
     */
    private suspend fun loadMutedStories() {
        try {
            val response = repository.getMutedStoriesUsers() //  Using repository
            if (response.isSuccessful && response.body() != null) {
                val mutedStories = response.body()!!.data
                _mutedStoriesIds.value = mutedStories.mapNotNull { it.user?._id }.toSet()
                Log.d(TAG, "Loaded ${_mutedStoriesIds.value.size} muted stories users")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading muted stories: ${e.message}", e)
        }
    }

    /**
     * Load favorites list
     */
    private suspend fun loadFavorites() {
        try {
            val response = repository.getFavorites() // Using repository
            if (response.isSuccessful && response.body() != null) {
                val favorites = response.body()!!.data
                _favoriteIds.value = favorites.mapNotNull { it.user?._id }.toSet()
                Log.d(TAG, "Loaded ${_favoriteIds.value.size} favorites")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading favorites: ${e.message}", e)
        }
    }

    /**
     * Load restricted users
     */
    private suspend fun loadRestricted() {
        try {
            val response = repository.getRestrictedUsers() // Using repository
            if (response.isSuccessful && response.body() != null) {
                val restricted = response.body()!!.data
                _restrictedIds.value = restricted.mapNotNull { it.user?._id }.toSet()
                Log.d(TAG, "Loaded ${_restrictedIds.value.size} restricted users")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading restricted users: ${e.message}", e)
        }
    }

    // Helper methods to check relationships
    fun isCloseFriend(userId: String): Boolean = _closeFriendIds.value.contains(userId)
    fun isPostsMuted(userId: String): Boolean = _mutedPostsIds.value.contains(userId)
    fun isStoriesMuted(userId: String): Boolean = _mutedStoriesIds.value.contains(userId)
    fun isFavorite(userId: String): Boolean = _favoriteIds.value.contains(userId)
    fun isRestricted(userId: String): Boolean = _restrictedIds.value.contains(userId)

    // Methods to update relationships locally
    fun addCloseFriend(userId: String) {
        _closeFriendIds.value = _closeFriendIds.value + userId
    }

    fun removeCloseFriend(userId: String) {
        _closeFriendIds.value = _closeFriendIds.value - userId
    }

    fun addMutedPosts(userId: String) {
        _mutedPostsIds.value = _mutedPostsIds.value + userId
    }

    fun removeMutedPosts(userId: String) {
        _mutedPostsIds.value = _mutedPostsIds.value - userId
    }



    fun addMutedStories(userId: String) {
        _mutedStoriesIds.value = _mutedStoriesIds.value + userId
    }

    fun removeMutedStories(userId: String) {
        _mutedStoriesIds.value = _mutedStoriesIds.value - userId
    }

    fun addFavorite(userId: String) {
        _favoriteIds.value = _favoriteIds.value + userId
    }

    fun removeFavorite(userId: String) {
        _favoriteIds.value = _favoriteIds.value - userId
    }

    fun addRestricted(userId: String) {
        _restrictedIds.value = _restrictedIds.value + userId
    }

    fun removeRestricted(userId: String) {
        _restrictedIds.value = _restrictedIds.value - userId
    }

    /**
     * Refresh all relationships
     */
    fun refresh() {
        loadAllRelationships()
    }
}