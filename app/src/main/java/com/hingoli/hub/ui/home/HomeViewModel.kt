package com.hingoli.hub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    SELLING("Buy", "खरेदी", "selling"),
    BUSINESS("Business", "व्यवसाय", "business"),
    JOBS("Jobs", "नोकरी", "jobs")
}

data class HomeUiState(
    val servicesListings: List<Listing> = emptyList(),
    val sellingListings: List<Listing> = emptyList(),
    val businessListings: List<Listing> = emptyList(),
    val jobsListings: List<Listing> = emptyList(),
    val shopProducts: List<ShopProduct> = emptyList(),
    val oldProducts: List<ShopProduct> = emptyList(), // Buy/Sell Old section - condition='old'
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: CategoryFilter = CategoryFilter.ALL,
    val searchQuery: String = "",
    val banners: List<Banner> = emptyList(),
    val bottomBanners: List<Banner> = emptyList(),
    val currentCity: String? = null
)

/**
 * HomeViewModel - uses SharedDataRepository cache exclusively.
 * No direct API calls - all data comes from prefetched cache.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sharedDataRepository: SharedDataRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var currentCity: String? = null
    private var isInitialized = false
    
    init {
        loadHomeData()
    }
    
    fun loadHomeData(city: String? = null) {
        // Skip if already initialized with same city
        if (isInitialized && city == currentCity && _uiState.value.servicesListings.isNotEmpty()) {
            return
        }
        
        currentCity = city
        viewModelScope.launch {
            // Check if cache is valid - instant load without loading state
            if (sharedDataRepository.isCacheValid()) {
                loadFromCache()
                _uiState.value = _uiState.value.copy(isLoading = false, currentCity = city)
                isInitialized = true
            } else {
                // Show loading while waiting for prefetch to complete
                _uiState.value = _uiState.value.copy(isLoading = true, currentCity = city)
                
                // Load from cache - SharedDataRepository handles fetching if needed
                loadFromCache()
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                isInitialized = true
            }
        }
    }
    
    private suspend fun loadFromCache() {
        // All data comes from SharedDataRepository cache
        // SharedDataRepository.getListings() will return cached data or fetch if empty
        _uiState.value = _uiState.value.copy(
            servicesListings = sharedDataRepository.getListings("services").take(10),
            sellingListings = sharedDataRepository.getListings("selling").take(10),
            businessListings = sharedDataRepository.getListings("business").take(10),
            jobsListings = sharedDataRepository.getListings("jobs").take(10),
            shopProducts = sharedDataRepository.getShopProducts().take(10),
            oldProducts = sharedDataRepository.getOldProducts().take(10),
            banners = sharedDataRepository.getBanners(),
            bottomBanners = sharedDataRepository.getBannersForPlacement("home_bottom"),
            isLoading = false
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
                sharedDataRepository.getListings("selling"),
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
                sellingListings = if (filter.listingType == null || filter.listingType == "selling") 
                    typeFiltered.filter { it.listingType == "selling" } else emptyList(),
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
