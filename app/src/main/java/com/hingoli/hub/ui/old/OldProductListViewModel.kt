package com.hingoli.hub.ui.old

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.model.OldProduct
import com.hingoli.hub.data.repository.SharedDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OldProductListUiState(
    val categoryName: String = "",
    val products: List<OldProduct> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true
)

/**
 * ViewModel for Old Product List screen.
 * Loads products from old_products table by category.
 */
@HiltViewModel
class OldProductListViewModel @Inject constructor(
    private val sharedDataRepository: SharedDataRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OldProductListUiState())
    val uiState: StateFlow<OldProductListUiState> = _uiState.asStateFlow()
    
    private var currentCategoryId: Int? = null
    
    fun loadProducts(categoryId: Int, categoryName: String) {
        if (categoryId == currentCategoryId && _uiState.value.products.isNotEmpty()) {
            return
        }
        currentCategoryId = categoryId
        
        viewModelScope.launch {
            _uiState.value = OldProductListUiState(
                categoryName = categoryName,
                isLoading = true
            )
            
            try {
                val products = sharedDataRepository.getOldProductsList(
                    categoryId = categoryId,
                    page = 1
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    products = products,
                    currentPage = 1,
                    hasMorePages = products.size >= 20
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load products"
                )
            }
        }
    }
    
    fun loadMoreProducts() {
        val categoryId = currentCategoryId ?: return
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMorePages) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            
            try {
                val nextPage = _uiState.value.currentPage + 1
                val moreProducts = sharedDataRepository.getOldProductsList(
                    categoryId = categoryId,
                    page = nextPage
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    products = _uiState.value.products + moreProducts,
                    currentPage = nextPage,
                    hasMorePages = moreProducts.size >= 20
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
            }
        }
    }
    
    fun refresh() {
        currentCategoryId?.let { categoryId ->
            val categoryName = _uiState.value.categoryName
            currentCategoryId = null
            loadProducts(categoryId, categoryName)
        }
    }
}
