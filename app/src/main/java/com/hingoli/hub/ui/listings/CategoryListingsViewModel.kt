package com.hingoli.hub.ui.listings

import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.model.Banner
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.data.model.Listing
import com.hingoli.hub.data.model.ShopProduct
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.settings.SettingsManager
import com.hingoli.hub.data.repository.ListingRepository
import com.hingoli.hub.data.repository.ListingResult
import com.hingoli.hub.data.repository.SharedDataRepository
import com.hingoli.hub.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryListingsUiState(
    val listings: List<Listing> = emptyList(),
    val shopProducts: List<ShopProduct> = emptyList(), // For selling type - shows products instead
    val isSellingType: Boolean = false, // Flag to indicate if showing products
    val subcategories: List<Category> = emptyList(),
    val selectedSubcategory: Category? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true,
    val currentCity: String? = null,
    val topBanners: List<Banner> = emptyList(),
    val bottomBanners: List<Banner> = emptyList(),
    val isMarathi: Boolean = true
)

/**
 * ViewModel for category listings screens.
 * Uses SharedDataRepository cache for subcategory lookups.
 */
@HiltViewModel
class CategoryListingsViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val apiService: ApiService,
    private val sharedDataRepository: SharedDataRepository,
    settingsManager: SettingsManager
) : BaseViewModel(settingsManager) {
    
    private val _uiState = MutableStateFlow(CategoryListingsUiState())
    val uiState: StateFlow<CategoryListingsUiState> = _uiState.asStateFlow()
    
    private var currentListingType: String? = null
    private var currentCategoryId: Int? = null
    private var parentCategoryId: Int? = null
    private var currentCity: String? = null
    
    init {
        // Sync language from BaseViewModel
        viewModelScope.launch {
            isMarathi.collect { value ->
                _uiState.value = _uiState.value.copy(isMarathi = value)
            }
        }
    }
    
    fun loadListings(listingType: String, categoryId: Int, city: String? = null) {
        currentListingType = listingType
        currentCategoryId = categoryId
        currentCity = city
        
        viewModelScope.launch {
            val isSellingType = listingType == "selling"
            _uiState.value = CategoryListingsUiState(isLoading = true, currentCity = city, isSellingType = isSellingType)
            
            // For selling type, load products instead of listings
            if (isSellingType) {
                loadProductsForCategory(categoryId, city)
            } else {
                loadListingsForCategory(listingType, categoryId, city)
            }
            
            // Also load sibling subcategories (categories under the same parent) from cache
            loadSubcategories(listingType, categoryId)
            
            // Load banners for this listing type
            loadBanners(listingType)
        }
    }
    
    fun onCityChanged(city: String) {
        val type = currentListingType ?: return
        val categoryId = currentCategoryId ?: return
        
        currentCity = city
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, currentCity = city)
            if (type == "selling") {
                loadProductsForCategory(categoryId, city)
            } else {
                loadListingsForCategory(type, categoryId, city)
            }
        }
    }
    
    private suspend fun loadListingsForCategory(listingType: String, categoryId: Int, city: String? = null) {
        when (val result = listingRepository.getListings(
            listingType = listingType,
            subcategoryId = categoryId,
            city = city,
            page = 1
        )) {
            is ListingResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    listings = result.listings,
                    currentPage = 1,
                    hasMorePages = result.listings.size >= 20
                )
            }
            is ListingResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }
    
    /**
     * Load shop products for selling category (condition=old)
     */
    private suspend fun loadProductsForCategory(categoryId: Int, city: String? = null) {
        try {
            val response = apiService.getShopProducts(
                listingId = null,
                condition = "old",  // Only old/used products
                categoryId = null,  // We filter by subcategory
                subcategoryId = categoryId,
                city = city,
                page = 1,
                perPage = 20
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                val shopProducts = response.body()?.data?.products ?: emptyList()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    shopProducts = shopProducts,
                    currentPage = 1,
                    hasMorePages = shopProducts.size >= 20
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = response.body()?.message ?: "Failed to load products"
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Failed to load products"
            )
        }
    }
    
    /**
     * Load subcategories from SharedDataRepository cache
     */
    private suspend fun loadSubcategories(listingType: String, categoryId: Int) {
        try {
            // Get all categories for this type from cache
            val allCategories = sharedDataRepository.getCategories(listingType)
            
            // Find the current category
            val currentCategory = allCategories.find { it.categoryId == categoryId }
            
            // Determine parent ID
            val parentId = currentCategory?.parentId ?: categoryId
            parentCategoryId = parentId
            
            // Set the selected subcategory
            _uiState.value = _uiState.value.copy(
                selectedSubcategory = currentCategory
            )
            
            // Get sibling subcategories from cache (indexed by parent ID)
            val siblingSubcats = sharedDataRepository.getSubcategoriesForParent(parentId)
            
            if (siblingSubcats.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    subcategories = siblingSubcats
                )
            } else {
                // Fallback: filter from all categories
                val subcats = allCategories.filter { it.parentId == parentId }
                _uiState.value = _uiState.value.copy(
                    subcategories = subcats
                )
            }
        } catch (e: Exception) {
            // Silently fail - not critical
        }
    }
    
    fun onSubcategorySelected(subcategory: Category) {
        val type = currentListingType ?: return
        currentCategoryId = subcategory.categoryId
        
        _uiState.value = _uiState.value.copy(
            selectedSubcategory = subcategory,
            isLoading = true
        )
        
        viewModelScope.launch {
            if (type == "selling") {
                loadProductsForCategory(subcategory.categoryId, currentCity)
            } else {
                loadListingsForCategory(type, subcategory.categoryId, currentCity)
            }
        }
    }
    
    fun loadMoreListings() {
        val type = currentListingType ?: return
        val categoryId = currentCategoryId ?: return
        
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMorePages) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            
            val nextPage = _uiState.value.currentPage + 1
            
            when (val result = listingRepository.getListings(
                listingType = type,
                subcategoryId = categoryId,
                city = currentCity,
                page = nextPage
            )) {
                is ListingResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        listings = _uiState.value.listings + result.listings,
                        currentPage = nextPage,
                        hasMorePages = result.listings.size >= 20
                    )
                }
                is ListingResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    private suspend fun loadBanners(listingType: String) {
        try {
            // Map listing type to placement prefix
            val placementPrefix = when (listingType) {
                "services" -> "services"
                "selling" -> "selling"
                "business" -> "business"
                "jobs" -> "jobs"
                else -> "home"
            }
            
            // Load banners from SharedDataRepository cache (24-hour cache)
            val topBanners = sharedDataRepository.getBannersForPlacement("${placementPrefix}_top")
            val bottomBanners = sharedDataRepository.getBannersForPlacement("${placementPrefix}_bottom")
            
            _uiState.value = _uiState.value.copy(
                topBanners = topBanners,
                bottomBanners = bottomBanners
            )
        } catch (e: Exception) {
            // Silently fail - banners are not critical
        }
    }
    
    fun retry() {
        val type = currentListingType
        val categoryId = currentCategoryId
        
        if (type != null && categoryId != null) {
            loadListings(type, categoryId, currentCity)
        }
    }
}
