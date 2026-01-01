package com.hingoli.hub.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.model.AddToCartRequest
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.data.model.Listing
import com.hingoli.hub.data.model.ShopProduct
import com.hingoli.hub.data.repository.AuthRepository
import com.hingoli.hub.data.repository.SharedDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder(val displayName: String) {
    NONE("Default"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low")
}

data class ProductGridUiState(
    val products: List<Listing> = emptyList(),
    val shopProducts: List<ShopProduct> = emptyList(),  // Products from business shops
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedCategoryId: Int? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true,
    val sortOrder: SortOrder = SortOrder.NONE,
    val condition: String = "new",  // 'new' for Shop tab, 'old' for Old tab
    // Cart state
    val cartItemCount: Int = 0,
    val productsInCart: Set<Long> = emptySet(), // Product IDs of items in cart
    val addingToCartProductId: Long? = null, // Currently adding this product
    val cartMessage: String? = null,
    val currentUserId: Long = 0L  // Current user ID for owner check
)

@HiltViewModel
class ProductGridViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sharedDataRepository: SharedDataRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProductGridUiState())
    val uiState: StateFlow<ProductGridUiState> = _uiState.asStateFlow()
    
    // Prevent duplicate API calls
    private var categoriesLoaded = false
    private var cartLoaded = false
    private var productsLoaded = false
    private var lastCondition: String? = null
    
    // Set condition for filtering (only triggers load if condition changed)
    fun setCondition(condition: String) {
        if (condition == lastCondition && productsLoaded) return // Skip if same condition
        lastCondition = condition
        _uiState.value = _uiState.value.copy(condition = condition)
        productsLoaded = false
        loadProducts()
    }
    
    init {
        loadCategories()
        loadCartCount()
        loadCurrentUserId()
        // Don't load here - let setCondition from Screen trigger the load
    }
    
    private fun loadCurrentUserId() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: 0L
            _uiState.value = _uiState.value.copy(currentUserId = userId)
        }
    }
    
    private fun loadCategories() {
        if (categoriesLoaded) return // Already loaded
        categoriesLoaded = true
        
        viewModelScope.launch {
            try {
                // Load shop_categories for new products (Groceries, Electronics, etc.)
                val shopCategories = sharedDataRepository.getShopCategories()
                if (shopCategories.isNotEmpty()) {
                    // Convert ShopCategory to Category for UI compatibility
                    val categories = shopCategories.map { it.toCategory() }
                    _uiState.value = _uiState.value.copy(categories = categories)
                    return@launch
                }
                
                // Fallback to API directly if shared data is empty
                val response = apiService.getShopCategories(level = 1)
                if (response.isSuccessful && response.body()?.data != null) {
                    val categories = response.body()!!.data!!.map { it.toCategory() }
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
            } catch (e: Exception) {
                // Silently fail - categories are loaded in tabs anyway
                categoriesLoaded = false // Allow retry on error
            }
        }
    }
    
    fun loadCartCount() {
        if (cartLoaded) return // Already loaded
        cartLoaded = true
        
        viewModelScope.launch {
            try {
                val response = apiService.getCart()
                if (response.isSuccessful && response.body()?.data != null) {
                    val cart = response.body()!!.data!!
                    // Extract product IDs from cart items (shop products have productId)
                    val productIds: Set<Long> = cart.items
                        .mapNotNull { item -> item.productId }
                        .toSet()
                    _uiState.value = _uiState.value.copy(
                        cartItemCount = cart.itemCount,
                        productsInCart = productIds
                    )
                }
            } catch (e: Exception) {
                // Silent fail - cart count is not critical
                cartLoaded = false // Allow retry on error
            }
        }
    }
    
    fun addToCart(product: ShopProduct) {
        // Don't add if already adding
        if (_uiState.value.addingToCartProductId != null) return
        
        viewModelScope.launch {
            // Check if user is logged in
            if (!authRepository.isLoggedIn()) {
                _uiState.value = _uiState.value.copy(
                    cartMessage = "Please login to add to cart"
                )
                return@launch
            }
            
            // Check if user is the product owner
            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId != null && product.userId == currentUserId) {
                _uiState.value = _uiState.value.copy(
                    cartMessage = "You cannot add your own product to cart"
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(
                addingToCartProductId = product.productId,
                cartMessage = null
            )
            
            try {
                val response = apiService.addShopProductToCart(
                    AddToCartRequest(productId = product.productId, quantity = 1)
                )
                
                if (response.isSuccessful) {
                    val newProductsInCart = _uiState.value.productsInCart + product.productId
                    _uiState.value = _uiState.value.copy(
                        addingToCartProductId = null,
                        productsInCart = newProductsInCart,
                        cartMessage = "Added to cart!"
                    )
                    loadCartCount()
                } else {
                    // Try to get error message from response body
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = try {
                        // Try to parse JSON error message
                        val jsonError = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                        jsonError?.get("message")?.toString() ?: "Failed to add to cart"
                    } catch (e: Exception) {
                        "Failed to add to cart (${response.code()})"
                    }
                    _uiState.value = _uiState.value.copy(
                        addingToCartProductId = null,
                        cartMessage = errorMsg
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    addingToCartProductId = null,
                    cartMessage = e.message ?: "Network error"
                )
            }
        }
    }
    
    fun clearCartMessage() {
        _uiState.value = _uiState.value.copy(cartMessage = null)
    }
    
    fun isProductInCart(productId: Long): Boolean {
        return productId in _uiState.value.productsInCart
    }
    
    fun loadProducts(refresh: Boolean = false) {
        // Prevent duplicate calls unless refresh is requested
        if (_uiState.value.isLoading) return
        if (productsLoaded && !refresh && _uiState.value.currentPage == 1) return
        
        productsLoaded = true
        
        val page = if (refresh) 1 else _uiState.value.currentPage
        val condition = _uiState.value.condition
        
        viewModelScope.launch {
            // For initial load (page 1, no refresh), check cache first
            if (page == 1 && !refresh && _uiState.value.selectedCategoryId == null && _uiState.value.searchQuery.isBlank()) {
                if (condition == "new") {
                    val cachedShopProducts = sharedDataRepository.getShopProducts()
                    if (cachedShopProducts.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            products = emptyList(),
                            shopProducts = cachedShopProducts,
                            isLoading = false,
                            hasMorePages = cachedShopProducts.size >= 20
                        )
                        return@launch
                    }
                } else {
                    val cached = sharedDataRepository.getListings("selling")
                    if (cached.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            products = cached,
                            isLoading = false,
                            hasMorePages = cached.size >= 20
                        )
                        return@launch
                    }
                }
            }
            
            // Show loading only if we need to fetch
            _uiState.value = _uiState.value.copy(
                isLoading = refresh || page == 1,
                isLoadingMore = !refresh && page > 1
            )
            
            try {
                // Shop tab - ONLY new products from shop_products table
                // Note: Old/used products use the Old Tab which uses OldCategoryScreen -> OldProductListScreen
                // and calls the old-products API which reads from old_products table
                
                val searchQuery = _uiState.value.searchQuery.takeIf { it.isNotBlank() }
                val shopResponse = apiService.getShopProducts(
                    // Note: condition param removed - shop_products is always new products
                    shopCategoryId = _uiState.value.selectedCategoryId, // Use shop_category_id
                    search = searchQuery,
                    page = page,
                    perPage = 30
                )
                
                if (shopResponse.isSuccessful && shopResponse.body()?.data != null) {
                    val shopProducts = shopResponse.body()!!.data!!.products
                    val pagination = shopResponse.body()!!.data!!.pagination
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        products = emptyList(), // No listings for Shop tab
                        shopProducts = if (refresh || page == 1) shopProducts else _uiState.value.shopProducts + shopProducts,
                        currentPage = page,
                        hasMorePages = pagination?.let { it.page < it.totalPages } ?: false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = "Failed to load products"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.message
                )
            }
        }
    }
    
    fun loadNextPage() {
        if (!_uiState.value.hasMorePages || _uiState.value.isLoadingMore) return
        _uiState.value = _uiState.value.copy(currentPage = _uiState.value.currentPage + 1)
        loadProducts()
    }
    
    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun onSearch() {
        _uiState.value = _uiState.value.copy(currentPage = 1)
        loadProducts(refresh = true)
    }
    
    fun onCategorySelected(categoryId: Int?) {
        _uiState.value = _uiState.value.copy(
            selectedCategoryId = categoryId,
            currentPage = 1
        )
        loadProducts(refresh = true)
    }
    
    fun refresh() {
        productsLoaded = false
        loadProducts(refresh = true)
    }
    
    fun onSortChange(sortOrder: SortOrder) {
        val currentProducts = _uiState.value.products
        val currentShopProducts = _uiState.value.shopProducts
        
        // Sort regular listings (products)
        val sortedProducts = when (sortOrder) {
            SortOrder.PRICE_LOW_TO_HIGH -> currentProducts.sortedBy { it.price ?: Double.MAX_VALUE }
            SortOrder.PRICE_HIGH_TO_LOW -> currentProducts.sortedByDescending { it.price ?: 0.0 }
            SortOrder.NONE -> currentProducts
        }
        
        // Sort shop products (this is what's displayed on Shop page)
        val sortedShopProducts = when (sortOrder) {
            SortOrder.PRICE_LOW_TO_HIGH -> currentShopProducts.sortedBy { it.price }
            SortOrder.PRICE_HIGH_TO_LOW -> currentShopProducts.sortedByDescending { it.price }
            SortOrder.NONE -> currentShopProducts
        }
        
        _uiState.value = _uiState.value.copy(
            sortOrder = sortOrder,
            products = sortedProducts,
            shopProducts = sortedShopProducts
        )
    }
}
