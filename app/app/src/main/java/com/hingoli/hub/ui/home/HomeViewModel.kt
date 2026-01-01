package com.hingoli.hub.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.model.AppStats
import com.hingoli.hub.data.model.Banner
import com.hingoli.hub.data.model.Listing
import com.hingoli.hub.data.model.ShopProduct
import com.hingoli.hub.data.repository.SharedDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CategoryFilter(val displayName: String, val displayNameMr: String, val listingType: String?) {
    ALL("All", "सर्व", null),
    SERVICES("Services", "सेवा", "services"),
    BUSINESS("Business", "व्यवसाय", "business"),
    JOBS("Jobs", "नोकरी", "jobs")
}

data class HomeUiState(
    val servicesListings: List<Listing> = emptyList(),
    val businessListings: List<Listing> = emptyList(),
    val jobsListings: List<Listing> = emptyList(),
    val shopProducts: List<ShopProduct> = emptyList(),
    val oldProducts: List<ShopProduct> = emptyList(), // Buy/Sell Old section - condition='old'
    val isLoading: Boolean = false,
    val isDataReady: Boolean = false, // True when data has been loaded at least once
    val error: String? = null,
    val selectedFilter: CategoryFilter = CategoryFilter.ALL,
    val searchQuery: String = "",
    val banners: List<Banner> = emptyList(),
    val bottomBanners: List<Banner> = emptyList(),
    val currentCity: String? = null,
    val stats: AppStats? = null
)

/**
 * HomeViewModel - uses SharedDataRepository cache exclusively.
 * No direct API calls - all data comes from prefetched cache.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sharedDataRepository: SharedDataRepository,
    private val apiService: ApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var currentCity: String? = null
    private var isInitialized = false
    
    // Debug tag for cache logging - filter with "tag:CacheDebug" in Logcat
    private val TAG = "CacheDebug"
    
    init {
        loadHomeData()
    }
    
    fun loadHomeData(city: String? = null) {
        Log.d(TAG, "🏠 HomeViewModel.loadHomeData() called, city=$city")
        
        // Skip if already initialized with same city AND cache is still valid
        if (isInitialized && city == currentCity && 
            _uiState.value.servicesListings.isNotEmpty() && 
            sharedDataRepository.isCacheValid()) {
            Log.d(TAG, "⏭️ Skipping load - already initialized with valid cache")
            return
        }
        
        currentCity = city
        viewModelScope.launch {
            // Load stats in parallel (fresh each time for accurate numbers)
            loadStats()
            
            // Check if cache is valid - instant load without loading state
            if (sharedDataRepository.isCacheValid()) {
                Log.d(TAG, "📦 Cache valid - loading from cache (no loading indicator)")
                loadFromCache()
                _uiState.value = _uiState.value.copy(isLoading = false, currentCity = city)
                isInitialized = true
            } else {
                Log.d(TAG, "⚠️ Cache invalid - showing loading and refreshing...")
                // Show loading while fetching fresh data
                _uiState.value = _uiState.value.copy(isLoading = true, currentCity = city)
                
                // Ensure data is fresh - this will refresh if cache is stale
                sharedDataRepository.ensureDataFresh(city)
                
                // Now load from refreshed cache
                loadFromCache()
                Log.d(TAG, "✅ Data loaded after refresh")
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                isInitialized = true
            }
        }
    }
    
    private suspend fun loadStats() {
        try {
            val response = apiService.getAppStats()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { stats ->
                    _uiState.value = _uiState.value.copy(stats = stats)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load stats: ${e.message}")
        }
    }
    
    private suspend fun loadFromCache() {
        // All data comes from SharedDataRepository cache
        // SharedDataRepository.getListings() will return cached data or fetch if empty
        _uiState.value = _uiState.value.copy(
            servicesListings = sharedDataRepository.getListings("services").take(10),
            businessListings = sharedDataRepository.getListings("business").take(10),
            jobsListings = sharedDataRepository.getListings("jobs").take(10),
            shopProducts = sharedDataRepository.getShopProducts().take(10),
            oldProducts = sharedDataRepository.getOldProducts().take(10),
            banners = sharedDataRepository.getBanners(),
            bottomBanners = sharedDataRepository.getBannersForPlacement("home_bottom"),
            isLoading = false,
            isDataReady = true
        )
    }
    
    fun onCityChanged(city: String) {
        currentCity = city
        isInitialized = false // Force refresh on city change
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, currentCity = city)
            // Refresh all data for new city
            sharedDataRepository.refreshAll(city)
            loadFromCache()
            _uiState.value = _uiState.value.copy(isLoading = false)
            isInitialized = true
        }
    }
    
    fun onFilterSelected(filter: CategoryFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }
    
    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun searchListings() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) {
            viewModelScope.launch {
                loadFromCache()
            }
            return
        }
        
        // For search, filter locally from cached data
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val filter = _uiState.value.selectedFilter
            val allListings = listOf(
                sharedDataRepository.getListings("services"),
                sharedDataRepository.getListings("business"),
                sharedDataRepository.getListings("jobs")
            ).flatten()
            
            // Filter by search query (case insensitive)
            val queryLower = query.lowercase()
            val filtered = allListings.filter { listing ->
                listing.title.lowercase().contains(queryLower) ||
                (listing.description?.lowercase()?.contains(queryLower) == true)
            }
            
            // Apply category filter if selected
            val typeFiltered = if (filter.listingType != null) {
                filtered.filter { it.listingType == filter.listingType }
            } else {
                filtered
            }
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                servicesListings = if (filter.listingType == null || filter.listingType == "services") 
                    typeFiltered.filter { it.listingType == "services" } else emptyList(),
                businessListings = if (filter.listingType == null || filter.listingType == "business") 
                    typeFiltered.filter { it.listingType == "business" } else emptyList(),
                jobsListings = if (filter.listingType == null || filter.listingType == "jobs") 
                    typeFiltered.filter { it.listingType == "jobs" } else emptyList()
            )
        }
    }
    
    fun retry() {
        isInitialized = false
        loadHomeData(currentCity)
    }
}
