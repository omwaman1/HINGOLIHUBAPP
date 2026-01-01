package com.hingoli.hub.ui.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.data.model.Listing
import com.hingoli.hub.data.repository.CategoryRepository
import com.hingoli.hub.data.repository.SharedDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobsUiState(
    val categories: List<Category> = emptyList(),
    val listings: List<Listing> = emptyList(),
    val selectedCategoryId: Int? = null,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true,
    val currentCity: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val categoryRepository: CategoryRepository,
    private val sharedDataRepository: SharedDataRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(JobsUiState())
    val uiState: StateFlow<JobsUiState> = _uiState.asStateFlow()
    
    private var isInitialized = false
    
    fun loadJobsData(city: String? = null) {
        if (isInitialized && city == _uiState.value.currentCity) return
        isInitialized = true
        
        viewModelScope.launch {
            // OPTIMIZATION: Show cached data immediately without loading indicator
            val cachedCategories = sharedDataRepository.getCategories("jobs")
            val cachedListings = sharedDataRepository.getListings("jobs")
            
            val hasCachedData = cachedCategories.isNotEmpty() || cachedListings.isNotEmpty()
            
            if (hasCachedData) {
                val mainCategories = cachedCategories.filter { it.parentId == null }
                _uiState.value = _uiState.value.copy(
                    categories = mainCategories,
                    listings = cachedListings,
                    isLoading = false,  // No loading indicator when cache exists
                    currentCity = city,
                    hasMorePages = cachedListings.size >= 20
                )
            } else {
                // Only show loading if no cached data exists
                _uiState.value = _uiState.value.copy(
                    isLoading = true, 
                    error = null,
                    currentCity = city,
                    currentPage = 1
                )
            }
            
            // OPTIMIZATION: Always fetch fresh data in background (silently)
            try {
                val categoriesResponse = apiService.getCategories(listingType = "jobs")
                val categories: List<Category> = if (categoriesResponse.isSuccessful) {
                    categoriesResponse.body()?.data ?: emptyList()
                } else emptyList()
                
                val listingsResponse = apiService.getListings(
                    listingType = "jobs",
                    city = city,
                    page = 1,
                    perPage = 20
                )
                val listings: List<Listing> = if (listingsResponse.isSuccessful) {
                    listingsResponse.body()?.data ?: emptyList()
                } else emptyList()
                
                val mainCategories = categories.filter { it.parentId == null }
                
                // Only update UI if data is different from cache (avoid flicker)
                if (listings != cachedListings || mainCategories != _uiState.value.categories) {
                    _uiState.value = _uiState.value.copy(
                        categories = mainCategories,
                        listings = listings,
                        isLoading = false,
                        hasMorePages = listings.size >= 20
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                // If API fails but we have cached data, just hide loading (don't show error)
                if (!hasCachedData) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load jobs"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
    
    fun onCategorySelected(categoryId: Int?) {
        if (categoryId == _uiState.value.selectedCategoryId) return
        
        _uiState.value = _uiState.value.copy(
            selectedCategoryId = categoryId,
            currentPage = 1,
            listings = emptyList()
        )
        
        loadJobListings()
    }
    
    fun onCityChanged(city: String?) {
        _uiState.value = _uiState.value.copy(
            currentCity = city,
            currentPage = 1,
            listings = emptyList()
        )
        isInitialized = false
        loadJobsData(city)
    }
    
    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        // Filter locally from cached/loaded data
        if (query.isBlank()) {
            // Reset to full list
            viewModelScope.launch {
                val cachedListings = sharedDataRepository.getListings("jobs")
                _uiState.value = _uiState.value.copy(listings = cachedListings)
            }
        } else {
            // Filter listings by search query
            val queryLower = query.lowercase()
            val filtered = _uiState.value.listings.filter { listing ->
                listing.title.lowercase().contains(queryLower) ||
                (listing.description?.lowercase()?.contains(queryLower) == true)
            }
            _uiState.value = _uiState.value.copy(listings = filtered)
        }
    }
    
    fun loadMoreJobs() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMorePages) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            
            try {
                val nextPage = _uiState.value.currentPage + 1
                val response = apiService.getListings(
                    listingType = "jobs",
                    categoryId = _uiState.value.selectedCategoryId,
                    city = _uiState.value.currentCity,
                    page = nextPage,
                    perPage = 20
                )
                
                if (response.isSuccessful) {
                    val newListings: List<Listing> = response.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        listings = _uiState.value.listings + newListings,
                        currentPage = nextPage,
                        isLoadingMore = false,
                        hasMorePages = newListings.size >= 20
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingMore = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
            }
        }
    }
    
    private fun loadJobListings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val response = apiService.getListings(
                    listingType = "jobs",
                    categoryId = _uiState.value.selectedCategoryId,
                    city = _uiState.value.currentCity,
                    page = 1,
                    perPage = 20
                )
                
                if (response.isSuccessful) {
                    val listings: List<Listing> = response.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        listings = listings,
                        isLoading = false,
                        hasMorePages = listings.size >= 20
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load jobs"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load jobs"
                )
            }
        }
    }
}
