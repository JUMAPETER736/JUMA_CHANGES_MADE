package com.uyscuti.social.business.viewmodel.business

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.network.api.response.business.response.post.Post
import com.uyscuti.social.business.repository.IFlashApiRepository
import kotlinx.coroutines.launch

class BusinessCatalogueViewModel(
    private val repository: IFlashApiRepository
): ViewModel() {

    // Private MutableLiveData for internal updates
    private val _catalogueItems = MutableLiveData<List<Post>>()
    private val _uiState = MutableLiveData<CatalogueUiState>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _isLoadingMore = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String?>()

    // Public LiveData for UI observation
    val catalogueItems: LiveData<List<Post>> = _catalogueItems
    val uiState: LiveData<CatalogueUiState> = _uiState
    val isLoading: LiveData<Boolean> = _isLoading
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore
    val errorMessage: LiveData<String?> = _errorMessage

    // Store original items for filtering/searching
    private var originalItems: List<Post> = emptyList()
    private var allLoadedItems: List<Post> = emptyList()

    // Pagination state
    private var currentPage = 1
    private var hasNextPage = true
    private var isCurrentlyLoading = false

    // Search state
    private var currentSearchQuery: String = ""

    init {
        loadCatalogueItems()
    }

    fun loadCatalogueItems(refresh: Boolean = false) {
        if (isCurrentlyLoading && !refresh) return

        // Reset pagination state when refreshing
        if (refresh) {
            currentPage = 1
            hasNextPage = true
            allLoadedItems = emptyList()
            originalItems = emptyList()
            _catalogueItems.value = emptyList()
            currentSearchQuery = ""
        }

        if (!hasNextPage && !refresh) return

        isCurrentlyLoading = true

        viewModelScope.launch {

            // Show appropriate loading state
            if (currentPage == 1) {
                _isLoading.value = true
                _uiState.value = CatalogueUiState.Loading
            } else {
                _isLoadingMore.value = true
            }

            _errorMessage.value = null

            // getting catalogues from the server
            repository.getAllCatalogues(currentPage.toString())
                .onSuccess { products ->
                    val sortedProducts = products

                    // Combine with existing items for pagination
                    allLoadedItems = if (refresh || currentPage == 1) {
                        sortedProducts
                    } else {
                        (allLoadedItems + sortedProducts).distinctBy { it._id }
                    }

                    originalItems = allLoadedItems

                    // Apply current search if active
                    val itemsToShow = if (currentSearchQuery.isNotEmpty()) {
                        applySearch(currentSearchQuery, allLoadedItems)
                    } else {
                        allLoadedItems
                    }


                    _catalogueItems.value = itemsToShow
                    _uiState.value = CatalogueUiState.Success(itemsToShow)

                    // Update pagination state
                    hasNextPage = products.isNotEmpty() // Adjust based on your API response
                    currentPage++

                    _isLoading.value = false
                }
                .onFailure { exception ->
                    val errorMsg = exception.message ?: "Unknown error occurred"
                    _errorMessage.value = errorMsg
//                    _uiState.value = CatalogueUiState.Error(errorMsg)
//                    _isLoading.value = false

                    // Don't override success state when loading more fails
                    if (currentPage == 1) {
                        _uiState.value = CatalogueUiState.Error(errorMsg)
                    }
                }

            // Reset loading states
            isCurrentlyLoading = false
            _isLoading.value = false
            _isLoadingMore.value = false
        }

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