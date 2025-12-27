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
    val imageUrl: String = "",
    val selectedImageUri: Uri? = null,
    val categoryId: Int? = null,
    val subcategoryId: Int? = null,
    val categories: List<Category> = emptyList(),
    val subcategories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
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
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            val categories = sharedDataRepository.getCategories("selling")
            val mainCategories = categories.filter { it.parentId == null }
            _uiState.value = _uiState.value.copy(categories = mainCategories)
        }
    }
    
    fun onCategoryChange(categoryId: Int?) {
        _uiState.value = _uiState.value.copy(
            categoryId = categoryId,
            subcategoryId = null,
            subcategories = emptyList(),
            error = null
        )
        // Load subcategories for selected category
        categoryId?.let { loadSubcategories(it) }
    }
    
    private fun loadSubcategories(categoryId: Int) {
        viewModelScope.launch {
            val subcats = sharedDataRepository.getSubcategoriesForParent(categoryId)
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
                        _uiState.value = _uiState.value.copy(
                            productId = product.productId,
                            productName = product.productName,
                            description = product.description ?: "",
                            price = product.price.toLong().toString(),
                            discountedPrice = product.discountedPrice?.toLong()?.toString() ?: "",
                            condition = product.condition ?: "new",
                            sellOnline = product.sellOnline,
                            stockQty = product.stockQty?.toString() ?: "",
                            imageUrl = product.imageUrl ?: "",
                            categoryId = product.categoryId,
                            subcategoryId = product.subcategoryId,
                            isLoading = false
                        )
                        // Load subcategories if product has a category
                        product.categoryId?.let { loadSubcategories(it) }
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
        _uiState.value = _uiState.value.copy(condition = value, error = null)
    }
    
    fun onSellOnlineChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(sellOnline = value, error = null)
    }
    
    fun onStockQtyChange(value: String) {
        _uiState.value = _uiState.value.copy(stockQty = value, error = null)
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
                state.categoryId?.let { addField("category_id", it.toString()) }
                state.subcategoryId?.let { addField("subcategory_id", it.toString()) }
                
                val response = apiService.updateProduct(state.productId, requestMap, imagePart)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isSuccess = true
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
