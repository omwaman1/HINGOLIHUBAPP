package com.hingoli.hub.ui.old

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.model.Banner
import com.hingoli.hub.data.model.OldCategory
import com.hingoli.hub.data.repository.SharedDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Section containing a main category and its subcategories
 */
data class OldCategorySection(
    val category: OldCategory,
    val subcategories: List<OldCategory>
)

data class OldCategoryUiState(
    val categories: List<OldCategory> = emptyList(),
    val categorySections: List<OldCategorySection> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val topBanners: List<Banner> = emptyList(),
    val bottomBanners: List<Banner> = emptyList()
)

/**
 * ViewModel for Old (Used Products) category screen.
 * Loads categories from old_categories table.
 */
@HiltViewModel
class OldCategoryViewModel @Inject constructor(
    private val sharedDataRepository: SharedDataRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OldCategoryUiState())
    val uiState: StateFlow<OldCategoryUiState> = _uiState.asStateFlow()
    
    private var hasLoaded = false
    
    fun loadCategories() {
        if (hasLoaded && _uiState.value.categorySections.isNotEmpty()) {
            return
        }
        hasLoaded = true
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load main old categories
                val mainCategories = sharedDataRepository.getOldCategories()
                
                // Load subcategories for each main category
                val sections = mainCategories.map { category ->
                    val subcategories = sharedDataRepository.getOldSubcategories(category.id)
                    OldCategorySection(
                        category = category,
                        subcategories = subcategories
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    categories = mainCategories,
                    categorySections = sections
                )
                
                // Load banners
                loadBanners()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load categories"
                )
            }
        }
    }
    
    private suspend fun loadBanners() {
        try {
            val topBanners = sharedDataRepository.getBannersForPlacement("old_top")
            val bottomBanners = sharedDataRepository.getBannersForPlacement("old_bottom")
            
            _uiState.value = _uiState.value.copy(
                topBanners = topBanners,
                bottomBanners = bottomBanners
            )
        } catch (e: Exception) {
            // Silently fail - banners are not critical
        }
    }
}
