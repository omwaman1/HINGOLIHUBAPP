package com.hingoli.hub.ui.product

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.data.repository.SharedDataRepository
import com.hingoli.hub.util.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

data class ProductFormUiState(
    val productId: Long = 0L,
    val productName: String = "",
    val description: String = "",
    val price: String = "",
    val discountedPrice: String = "",
    val condition: String = "new",
    val sellOnline: Boolean = true,
    val stockQty: String = "",
    val deliveryBy: Int = 3, // Delivery days: 1=Today, 2=Tomorrow, 3-8=days
    val imageUrl: String = "",
    val selectedImageUri: Uri? = null,
    val categoryId: Int? = null,
    val subcategoryId: Int? = null,
    val shopCategoryId: Int? = null, // For new products using shop_categories
    val categories: List<Category> = emptyList(),
    val subcategories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProductFormViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sharedDataRepository: SharedDataRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "ProductFormViewModel"
    
    private val _uiState = MutableStateFlow(ProductFormUiState())
    
    init {
        loadCategoriesForCondition()
    }
    
    /**
 * Load categories based on current condition:
 * - NEW products: use shop_categories (Groceries, Electronics, etc.)
 * - OLD products: use old_categories (Mobile Phones, Vehicles, etc.)
 */
private fun loadCategoriesForCondition() {
    viewModelScope.launch {
        val condition = _uiState.value.condition
        if (condition == "new") {
            // Load shop categories for NEW products
            val shopCategories = sharedDataRepository.getShopCategories()
            val categories = shopCategories.map { it.toCategory() }
            _uiState.value = _uiState.value.copy(categories = categories)
        } else {
            // Load old_categories for OLD/used products
            val oldCategories = sharedDataRepository.getOldCategories()
            val categories = oldCategories.map { 
                Category(
                    categoryId = it.id,
                    parentId = it.parentId,
                    name = it.name,
                    nameMr = it.nameMr,
                    slug = it.slug,
                    listingType = "old",
                    iconUrl = it.imageUrl,
                    imageUrl = it.imageUrl,
                    description = null,
                    listingCount = 0,
                    depth = 0
                )
            }
            _uiState.value = _uiState.value.copy(categories = categories)
        }
    }
}
    
    fun onCategoryChange(categoryId: Int?) {
        _uiState.value = _uiState.value.copy(
            categoryId = categoryId,
            shopCategoryId = if (_uiState.value.condition == "new") categoryId else null,
            subcategoryId = null,
            subcategories = emptyList(),
            error = null
        )
        // Load subcategories for selected category
        categoryId?.let { loadSubcategories(it) }
    }
    
    private fun loadSubcategories(categoryId: Int) {
        viewModelScope.launch {
            val condition = _uiState.value.condition
            val subcats = if (condition == "new") {
                // Load shop subcategories for NEW products
                val shopSubcats = sharedDataRepository.getShopSubcategories(categoryId)
                shopSubcats.map { it.toCategory() }
            } else {
                // Load old_categories subcategories for OLD products
                val oldSubcats = sharedDataRepository.getOldSubcategories(categoryId)
                oldSubcats.map { 
                    Category(
                        categoryId = it.id,
                        parentId = it.parentId,
                        name = it.name,
                        nameMr = it.nameMr,
                        slug = it.slug,
                        listingType = "old",
                        iconUrl = it.imageUrl,
                        imageUrl = it.imageUrl,
                        description = null,
                        listingCount = 0,
                        depth = 1
                    )
                }
            }
            _uiState.value = _uiState.value.copy(subcategories = subcats)
        }
    }
    
    fun onSubcategoryChange(subcategoryId: Int?) {
        _uiState.value = _uiState.value.copy(subcategoryId = subcategoryId, error = null)
    }
    val uiState: StateFlow<ProductFormUiState> = _uiState.asStateFlow()
    
    fun loadProduct(productId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getShopProduct(productId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val product = response.body()?.data
                    if (product != null) {
                        // Use isOldProduct flag to determine product type
                        val isOldProduct = product.isOldProduct
                        
                        // Map the condition - for old products use "old", for new use actual condition
                        val productCondition = if (isOldProduct) "old" else (product.condition ?: "new")
                        
                        // Determine which category ID to use based on product type
                        // NEW products use shop_category_id, OLD products use category_id
                        val selectedCategoryId = if (!isOldProduct) {
                            product.shopCategoryId ?: product.categoryId
                        } else {
                            product.categoryId
                        }
                        
                        // First, update condition and basic product info
                        _uiState.value = _uiState.value.copy(
                            productId = product.productId,
                            productName = product.productName,
                            description = product.description ?: "",
                            price = product.price.toLong().toString(),
                            discountedPrice = product.discountedPrice?.toLong()?.toString() ?: "",
                            condition = productCondition,
                            sellOnline = product.sellOnline,
                            stockQty = product.stockQty?.toString() ?: "",
                            deliveryBy = product.deliveryBy,
                            imageUrl = product.imageUrl ?: "",
                            isLoading = false
                        )
                        
                        // Load correct categories based on product type
                        val categories = if (!isOldProduct) {
                            val shopCategories = sharedDataRepository.getShopCategories()
                            shopCategories.map { it.toCategory() }
                        } else {
                            // Load old_categories for OLD products
                            val oldCategories = sharedDataRepository.getOldCategories()
                            oldCategories.map { 
                                Category(
                                    categoryId = it.id,
                                    parentId = it.parentId,
                                    name = it.name,
                                    nameMr = it.nameMr,
                                    slug = it.slug,
                                    listingType = "old",
                                    iconUrl = it.imageUrl,
                                    imageUrl = it.imageUrl,
                                    description = null,
                                    listingCount = 0,
                                    depth = 0
                                )
                            }
                        }
                        
                        // Update state with categories and selected category/subcategory
                        // For OLD products, validate category exists in old_categories
                        val validCategoryId = if (isOldProduct) {
                            // Only keep category if it exists in old_categories (IDs 1-100)
                            if (selectedCategoryId in 1..100) selectedCategoryId else null
                        } else {
                            selectedCategoryId
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            categories = categories,
                            categoryId = validCategoryId,
                            shopCategoryId = if (!isOldProduct) selectedCategoryId else null,
                            subcategoryId = if (validCategoryId != null) product.subcategoryId else null
                        )
                        
                        // Load subcategories for selected category
                        if (validCategoryId != null) {
                            val subcats = if (!isOldProduct) {
                                val shopSubcats = sharedDataRepository.getShopSubcategories(validCategoryId)
                                shopSubcats.map { it.toCategory() }
                            } else {
                                // Load old_categories subcategories
                                val oldSubcats = sharedDataRepository.getOldSubcategories(validCategoryId)
                                oldSubcats.map { 
                                    Category(
                                        categoryId = it.id,
                                        parentId = it.parentId,
                                        name = it.name,
                                        nameMr = it.nameMr,
                                        slug = it.slug,
                                        listingType = "old",
                                        iconUrl = it.imageUrl,
                                        imageUrl = it.imageUrl,
                                        description = null,
                                        listingCount = 0,
                                        depth = 1
                                    )
                                }
                            }
                            _uiState.value = _uiState.value.copy(subcategories = subcats)
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Product not found"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.body()?.message ?: "Failed to load product"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading product: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load product"
                )
            }
        }
    }
    
    fun onProductNameChange(value: String) {
        _uiState.value = _uiState.value.copy(productName = value, error = null)
    }
    
    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value, error = null)
    }
    
    fun onPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(price = value, error = null)
    }
    
    fun onDiscountedPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(discountedPrice = value, error = null)
    }
    
    fun onConditionChange(value: String) {
        val currentCondition = _uiState.value.condition
        _uiState.value = _uiState.value.copy(
            condition = value, 
            error = null,
            // Clear category selection when switching between NEW and OLD
            categoryId = null,
            subcategoryId = null,
            shopCategoryId = null,
            subcategories = emptyList()
        )
        // Reload categories if condition changed
        if (value != currentCondition) {
            loadCategoriesForCondition()
        }
    }
    
    fun onSellOnlineChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(sellOnline = value, error = null)
    }
    
    fun onStockQtyChange(value: String) {
        _uiState.value = _uiState.value.copy(stockQty = value, error = null)
    }
    
    fun onDeliveryByChange(value: Int) {
        _uiState.value = _uiState.value.copy(deliveryBy = value, error = null)
    }
    
    fun onImageSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri, error = null)
    }
    
    fun saveProduct() {
        val state = _uiState.value
        
        // Validation
        if (state.productName.isBlank()) {
            _uiState.value = state.copy(error = "Product name is required")
            return
        }
        if (state.price.isBlank() || state.price.toDoubleOrNull() == null || state.price.toDouble() <= 0) {
            _uiState.value = state.copy(error = "Valid price is required")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            try {
                // Prepare image if selected (compressed to WebP)
                var imagePart: MultipartBody.Part? = null
                state.selectedImageUri?.let { uri ->
                    val file = compressImage(uri)
                    if (file != null) {
                        val mimeType = if (file.extension == "webp") "image/webp" else "image/jpeg"
                        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                        imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                    }
                }
                
                // Build request body as multipart fields
                val requestMap = mutableMapOf<String, RequestBody>()
                
                fun addField(key: String, value: String) {
                    if (value.isNotBlank()) {
                        requestMap[key] = value.toRequestBody("text/plain".toMediaTypeOrNull())
                    }
                }
                
                addField("product_name", state.productName)
                addField("description", state.description)
                addField("price", state.price)
                if (state.discountedPrice.isNotBlank()) {
                    addField("discounted_price", state.discountedPrice)
                }
                addField("condition", state.condition)
                addField("sell_online", if (state.sellOnline) "1" else "0")
                if (state.stockQty.isNotBlank()) {
                    addField("stock_qty", state.stockQty)
                }
                // For NEW products, send shop_category_id; for OLD, send category_id
                if (state.condition == "new") {
                    state.shopCategoryId?.let { addField("shop_category_id", it.toString()) }
                } else {
                    state.categoryId?.let { addField("category_id", it.toString()) }
                }
                state.subcategoryId?.let { addField("subcategory_id", it.toString()) }
                addField("delivery_by", state.deliveryBy.toString())
                
                // Call correct API based on condition
                val response = if (state.condition == "old") {
                    // Old products use different API
                    apiService.updateOldProduct(state.productId, requestMap, imagePart)
                } else {
                    // New products (shop products)
                    apiService.updateProduct(state.productId, requestMap, imagePart)
                }
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val successMsg = response.body()?.message ?: "Product updated successfully"
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isSuccess = true,
                        successMessage = successMsg
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = response.body()?.message ?: "Failed to update product"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving product: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save product"
                )
            }
        }
    }
    
    /**
     * Compress image from URI before upload
     * Converts to WebP format and resizes to max 1920x1080
     */
    private fun compressImage(uri: Uri): File? {
        return ImageCompressor.compressImage(
            context = context,
            imageUri = uri,
            maxWidth = 1920,
            maxHeight = 1080,
            quality = 80,
            useWebP = true
        )
    }
}
