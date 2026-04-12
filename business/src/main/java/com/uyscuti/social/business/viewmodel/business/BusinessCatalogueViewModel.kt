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


    fun loadMore() {
        if (currentSearchQuery.isEmpty()) {
            // Only load more from server when not searching
            loadCatalogueItems()
        }
        // When searching, all results are already loaded locally
    }

    // Helper method to check if we can load more
    fun canLoadMore(): Boolean {
        return hasNextPage && !isCurrentlyLoading && currentSearchQuery.isEmpty()
    }


    fun refreshCatalogue() {
        loadCatalogueItems(refresh = true)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun searchItems(query: String) {
        currentSearchQuery = query
        val searchResults = applySearch(query, originalItems)
        _catalogueItems.value = searchResults
        _uiState.value = CatalogueUiState.Success(searchResults)
    }

    private fun applySearch(query: String, items: List<Post>): List<Post> {
        return if (query.isEmpty()) {
            items
        } else {
            items.filter { item ->
                item.itemName.contains(query, ignoreCase = true) ||
                        item.description.contains(query, ignoreCase = true)
            }
        }
    }


    // UI State sealed class for better state management
    sealed class CatalogueUiState {
        object Loading : CatalogueUiState()
        data class Success(val items: List<Post>) : CatalogueUiState()
        data class Error(val message: String) : CatalogueUiState()
    }
}