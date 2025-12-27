package com.hingoli.hub.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.model.Banner
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.repository.SharedDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Unified category section used for all listing types
 */
data class CategorySection(
    val category: Category,
    val subcategories: List<Category>
)

data class CategoryUiState(
    val listingType: String = "",
    val categories: List<Category> = emptyList(),
    val categorySections: List<CategorySection> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val topBanners: List<Banner> = emptyList(),
    val bottomBanners: List<Banner> = emptyList()
)

/**
 * Unified ViewModel for all category screens (Services, Selling, Jobs, Businesses)
 * Uses SharedDataRepository cache exclusively - no direct API calls for categories.
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sharedDataRepository: SharedDataRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()
    
    private var currentListingType: String? = null
    
    fun loadCategories(listingType: String) {
        // Avoid reloading if same type and already loaded
        if (listingType == currentListingType && _uiState.value.categorySections.isNotEmpty()) {
            return
        }
        currentListingType = listingType
        
        viewModelScope.launch {
            // Use cache - SharedDataRepository handles deduplication
            val cachedCategories = sharedDataRepository.getCategories(listingType)
            
            if (cachedCategories.isNotEmpty()) {
                val parentCategories = cachedCategories.filter { it.parentId == null }
                val subcategories = cachedCategories.filter { it.parentId != null }
                
                val sections = parentCategories.map { parent ->
                    CategorySection(
                        category = parent,
                        subcategories = subcategories.filter { it.parentId == parent.categoryId }
                    )
                }.filter { it.subcategories.isNotEmpty() }
                
                _uiState.value = _uiState.value.copy(
                    listingType = listingType,
                    isLoading = false,
                    categories = parentCategories,
                    categorySections = sections.ifEmpty { parentCategories.map { CategorySection(it, emptyList()) } }
                )
                
                loadBanners(listingType)
            } else {
                // Cache is empty - show loading state
                _uiState.value = _uiState.value.copy(
                    listingType = listingType,
                    isLoading = false,
                    error = "No categories available"
                )
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
}

