package com.uyscuti.social.business.viewmodel.business

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.network.api.response.business.response.post.Post
import com.uyscuti.social.network.api.response.business.response.post.BusinessPost
import com.uyscuti.social.business.repository.IFlashApiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BusinessCatalogueViewModel(
    private val repository: IFlashApiRepository
): ViewModel() {

    private val _catalogueItems = MutableLiveData<List<Post>>()
    private val _newPageItems = MutableLiveData<List<Post>>()
    private val _uiState = MutableLiveData<CatalogueUiState>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _isLoadingMore = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String?>()
    private val _hasMoreData = MutableLiveData<Boolean>()

    val catalogueItems: LiveData<List<Post>> = _catalogueItems
    val newPageItems: LiveData<List<Post>> = _newPageItems
    val uiState: LiveData<CatalogueUiState> = _uiState
    val isLoading: LiveData<Boolean> = _isLoading
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore
    val errorMessage: LiveData<String?> = _errorMessage
    val hasMoreData: LiveData<Boolean> = _hasMoreData

    // Catalogue browsing state
    private var allLoadedItems: MutableList<Post> = mutableListOf()
    private var currentPage = 1
    private var totalPages = 1
    private var hasNextPage = true
    private var isCurrentlyLoading = false

    // Search state
    private var currentSearchQuery: String = ""
    private var isSearchActive = false
    private var searchJob: Job? = null
    private var searchCurrentPage = 1
    private var searchTotalPages = 1
    private var searchHasNextPage = true
    private var searchLoadedItems: MutableList<Post> = mutableListOf()
    private var isSearchLoading = false

    init {
        loadCatalogueItems()
    }

    /**
     * Load catalogue items (BROWSE MODE)
     */
    fun loadCatalogueItems(refresh: Boolean = false) {
        if (isCurrentlyLoading && !refresh) return

        if (refresh) {
            currentPage = 1
            totalPages = 1
            hasNextPage = true
            allLoadedItems.clear()
        }

        if (!hasNextPage && !refresh) return

        isCurrentlyLoading = true

        viewModelScope.launch {
            try {
                if (currentPage == 1) {
                    _isLoading.value = true
                    _uiState.value = CatalogueUiState.Loading
                } else {
                    _isLoadingMore.value = true
                }

                _errorMessage.value = null

                repository.getAllCatalogues(currentPage.toString())
                    .onSuccess { businessPost ->
                        handleCatalogueSuccess(businessPost, refresh)
                    }
                    .onFailure { exception ->
                        handleCatalogueError(exception)
                    }
            } finally {
                isCurrentlyLoading = false
                _isLoading.value = false
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * Search items with debouncing (SEARCH MODE)
     */
    fun searchItems(query: String, debounceMs: Long = 500) {
        searchJob?.cancel()

        val trimmedQuery = query.trim()
        currentSearchQuery = trimmedQuery
        isSearchActive = trimmedQuery.isNotEmpty()

        if (!isSearchActive) {
            clearSearch()
            return
        }

        // Debounce search
        searchJob = viewModelScope.launch {
            delay(debounceMs)
            performServerSearch(trimmedQuery, refresh = true)
        }
    }

    /**
     * Search immediately without debouncing (SEARCH MODE)
     */
    fun searchItemsImmediate(query: String, refresh: Boolean = true) {
        searchJob?.cancel()

        val trimmedQuery = query.trim()
        currentSearchQuery = trimmedQuery
        isSearchActive = trimmedQuery.isNotEmpty()

        if (!isSearchActive) {
            clearSearch()
            return
        }

        performServerSearch(trimmedQuery, refresh)
    }


    /**
     * Perform server-side search
     */
    private fun performServerSearch(query: String, refresh: Boolean) {
        if (isSearchLoading && !refresh) return

        // Reset search pagination on new search
        if (refresh) {
            searchCurrentPage = 1
            searchTotalPages = 1
            searchHasNextPage = true
            searchLoadedItems.clear()
        }

        if (!searchHasNextPage && !refresh) return

        isSearchLoading = true

        viewModelScope.launch {
            try {
                if (searchCurrentPage == 1) {
                    _isLoading.value = true
                    _uiState.value = CatalogueUiState.Loading
                } else {
                    _isLoadingMore.value = true
                }

                _errorMessage.value = null

                // SERVER-SIDE SEARCH
                repository.searchCatalogues(query, searchCurrentPage.toString())
                    .onSuccess { businessPost ->
                        handleSearchSuccess(businessPost, refresh)
                    }
                    .onFailure { exception ->
                        handleSearchError(exception)
                    }
            } finally {
                isSearchLoading = false
                _isLoading.value = false
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * Load more items (works for both modes)
     */
    fun loadMore() {
        if (isSearchActive) {
            loadMoreSearchResults()
        } else {
            loadMoreCatalogueItems()
        }
    }

    private fun loadMoreCatalogueItems() {
        if (isCurrentlyLoading || !hasNextPage) return
        currentPage++
        loadCatalogueItems(refresh = false)
    }

    private fun loadMoreSearchResults() {
        if (isSearchLoading || !searchHasNextPage) return
        searchCurrentPage++
        performServerSearch(currentSearchQuery, refresh = false)
    }

    /**
     * Refresh (works for both modes)
     */
    fun refreshCatalogue() {
        if (isSearchActive) {
            searchItemsImmediate(currentSearchQuery, refresh = true)
        } else {
            loadCatalogueItems(refresh = true)
        }
    }

    /**
     * Clear search and return to browse mode
     */
    fun clearSearch() {
        searchJob?.cancel()
        currentSearchQuery = ""
        isSearchActive = false

        searchCurrentPage = 1
        searchTotalPages = 1
        searchHasNextPage = true
        searchLoadedItems.clear()

        // Show catalogue items
        _catalogueItems.value = allLoadedItems.toList()
        _uiState.value = if (allLoadedItems.isEmpty()) {
            CatalogueUiState.Empty
        } else {
            CatalogueUiState.Success(allLoadedItems.toList())
        }
        _hasMoreData.value = hasNextPage
    }

    /**
     * Add new post
     */
    fun addNewPost(post: Post) {
        allLoadedItems.add(0, post)

        if (isSearchActive) {
            searchItemsImmediate(currentSearchQuery, refresh = true)
        } else {
            _catalogueItems.value = allLoadedItems.toList()
            _uiState.value = CatalogueUiState.Success(allLoadedItems.toList())
        }
    }

    /**
     * Update post
     */
    fun updatePost(postId: String, updater: (Post) -> Post) {
        val catalogueIndex = allLoadedItems.indexOfFirst { it._id == postId }
        if (catalogueIndex != -1) {
            allLoadedItems[catalogueIndex] = updater(allLoadedItems[catalogueIndex])
        }

        if (isSearchActive) {
            val searchIndex = searchLoadedItems.indexOfFirst { it._id == postId }
            if (searchIndex != -1) {
                searchLoadedItems[searchIndex] = updater(searchLoadedItems[searchIndex])
                _catalogueItems.value = searchLoadedItems.toList()
            }
        } else {
            _catalogueItems.value = allLoadedItems.toList()
        }
    }

    fun incrementCommentCount(postId: String) {
        updatePost(postId) { post ->
            post.copy(comments = post.comments + 1)
        }
    }

    /**
     * Remove post
     */
    fun removePost(postId: String) {
        allLoadedItems.removeAll { it._id == postId }

        if (isSearchActive) {
            searchLoadedItems.removeAll { it._id == postId }
            _catalogueItems.value = searchLoadedItems.toList()
            _uiState.value = if (searchLoadedItems.isEmpty()) {
                CatalogueUiState.EmptySearch(currentSearchQuery)
            } else {
                CatalogueUiState.Success(searchLoadedItems.toList())
            }
        } else {
            _catalogueItems.value = allLoadedItems.toList()
            _uiState.value = if (allLoadedItems.isEmpty()) {
                CatalogueUiState.Empty
            } else {
                CatalogueUiState.Success(allLoadedItems.toList())
            }
        }
    }

    fun canLoadMore(): Boolean {
        return if (isSearchActive) {
            searchHasNextPage && !isSearchLoading
        } else {
            hasNextPage && !isCurrentlyLoading
        }
    }


    fun isInSearchMode(): Boolean = isSearchActive

    fun clearError() {
        _errorMessage.value = null
    }

    // ===== HANDLE CATALOGUE SUCCESS =====
    private fun handleCatalogueSuccess(businessPost: BusinessPost, refresh: Boolean) {
        val products = businessPost.data.posts

        currentPage = businessPost.currentPage
        totalPages = businessPost.totalPages
        hasNextPage = businessPost.hasNextPage

        if (refresh || currentPage == 1) {
            allLoadedItems.clear()
            allLoadedItems.addAll(products)

            _catalogueItems.value = allLoadedItems.toList()
            _uiState.value = if (allLoadedItems.isEmpty()) {
                CatalogueUiState.Empty
            } else {
                CatalogueUiState.Success(allLoadedItems.toList())
            }
        } else {
            val newItems = products.filter { newItem ->
                allLoadedItems.none { it._id == newItem._id }
            }

            if (newItems.isNotEmpty()) {
                allLoadedItems.addAll(newItems)
                _newPageItems.value = newItems
            }
        }

        _hasMoreData.value = hasNextPage
    }

    // ===== HANDLE SEARCH SUCCESS =====
    private fun handleSearchSuccess(businessPost: BusinessPost, refresh: Boolean) {
        val searchResults = businessPost.data.posts

        searchCurrentPage = businessPost.currentPage
        searchTotalPages = businessPost.totalPages
        searchHasNextPage = businessPost.hasNextPage

        if (refresh || searchCurrentPage == 1) {
            searchLoadedItems.clear()
            searchLoadedItems.addAll(searchResults)

            _catalogueItems.value = searchLoadedItems.toList()
            _uiState.value = if (searchLoadedItems.isEmpty()) {
                CatalogueUiState.EmptySearch(currentSearchQuery)
            } else {
                CatalogueUiState.Success(searchLoadedItems.toList())
            }
        } else {
            val newItems = searchResults.filter { newItem ->
                searchLoadedItems.none { it._id == newItem._id }
            }

            if (newItems.isNotEmpty()) {
                searchLoadedItems.addAll(newItems)
                _newPageItems.value = newItems
            }
        }

        _hasMoreData.value = searchHasNextPage
    }

    private fun handleCatalogueError(exception: Throwable) {
        val errorMsg = exception.message ?: "Unknown error occurred"
        _errorMessage.value = errorMsg

        if (currentPage == 1) {
            _uiState.value = CatalogueUiState.Error(errorMsg)
        }

        _hasMoreData.value = hasNextPage
    }

    private fun handleSearchError(exception: Throwable) {
        val errorMsg = exception.message ?: "Search failed"
        _errorMessage.value = errorMsg

        if (searchCurrentPage == 1) {
            _uiState.value = CatalogueUiState.SearchError(errorMsg, currentSearchQuery)
        }

        _hasMoreData.value = searchHasNextPage
    }

    sealed class CatalogueUiState {
        object Loading : CatalogueUiState()
        data class Success(val items: List<Post>) : CatalogueUiState()
        data class Error(val message: String) : CatalogueUiState()
        object Empty : CatalogueUiState()
        data class EmptySearch(val query: String) : CatalogueUiState()
        data class SearchError(val message: String, val query: String) : CatalogueUiState()
    }
}