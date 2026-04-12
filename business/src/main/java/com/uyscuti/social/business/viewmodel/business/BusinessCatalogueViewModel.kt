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
