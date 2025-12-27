package com.hingoli.hub.ui.product

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.model.AddReviewRequest
import com.hingoli.hub.data.model.AddToCartRequest
import com.hingoli.hub.data.model.Review
import com.hingoli.hub.data.model.ShopProduct
import com.hingoli.hub.data.repository.ChatRepository
import com.hingoli.hub.data.api.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

data class ProductDetailUiState(
    val product: ShopProduct? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val quantity: Int = 1,
    val minQty: Int = 1,
    val isAddingToCart: Boolean = false,
    val addToCartSuccess: Boolean = false,
    val cartMessage: String? = null,
    val cartItemCount: Int = 0,
    val buyNowReady: Boolean = false,
    val isCreatingChat: Boolean = false,
    val currentUserId: Long = 0L,
    val currentUserName: String = "User",
    // Reviews
    val reviews: List<Review> = emptyList(),
    val isLoadingReviews: Boolean = false,
    val reviewCount: Int = 0,
    val avgRating: Double = 0.0,
    val canWriteReview: Boolean = false,
    val isSubmittingReview: Boolean = false,
    val reviewSubmitMessage: String? = null
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val apiService: ApiService,
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val productId: Long = checkNotNull(savedStateHandle["productId"])
    
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadProduct()
        loadCartCount()
        loadCurrentUser()
        loadReviews()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: 0L
            val userName = tokenManager.getUserName() ?: "User"
            _uiState.value = _uiState.value.copy(
                currentUserId = userId,
                currentUserName = userName
            )
        }
    }
    
    fun getCurrentUserName(): String {
        return _uiState.value.currentUserName
    }
    
    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = apiService.getShopProduct(productId)
                if (response.isSuccessful && response.body()?.data != null) {
                    val product = response.body()!!.data!!
                    val minQty = product.minQty.coerceAtLeast(1)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        product = product,
                        quantity = minQty,
                        minQty = minQty,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Product not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    private fun loadReviews() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingReviews = true)
            try {
                val response = apiService.getProductReviews(productId)
                if (response.isSuccessful && response.body()?.data != null) {
                    val reviews = response.body()!!.data!!
                    val avgRating = if (reviews.isNotEmpty()) {
                        reviews.map { it.rating }.average()
                    } else 0.0
                    
                    // Check if current user can write review (logged in, not owner, hasn't reviewed)
                    val currentUserId = _uiState.value.currentUserId
                    val productOwnerId = _uiState.value.product?.userId ?: 0
                    val hasAlreadyReviewed = reviews.any { it.reviewer?.userId?.toLong() == currentUserId }
                    val canWriteReview = currentUserId > 0 && currentUserId != productOwnerId.toLong() && !hasAlreadyReviewed
                    
                    _uiState.value = _uiState.value.copy(
                        isLoadingReviews = false,
                        reviews = reviews,
                        reviewCount = reviews.size,
                        avgRating = avgRating,
                        canWriteReview = canWriteReview
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingReviews = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingReviews = false)
            }
        }
    }
    
    fun submitReview(
        context: Context,
        rating: Int,
        title: String?,
        content: String?,
        imageUris: List<Uri>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingReview = true, reviewSubmitMessage = null)
            try {
                // Build multipart request
                val fields = mutableMapOf<String, okhttp3.RequestBody>()
                fields["rating"] = rating.toString().toRequestBody("text/plain".toMediaType())
                title?.let { fields["title"] = it.toRequestBody("text/plain".toMediaType()) }
                content?.let { fields["content"] = it.toRequestBody("text/plain".toMediaType()) }
                
                // Handle images
                val imageParts = mutableListOf<MultipartBody.Part>()
                imageUris.forEach { uri ->
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val tempFile = File.createTempFile("review_image", ".jpg", context.cacheDir)
                        tempFile.outputStream().use { output ->
                            inputStream.copyTo(output)
                        }
                        val requestBody = tempFile.asRequestBody("image/jpeg".toMediaType())
                        imageParts.add(MultipartBody.Part.createFormData("images[]", tempFile.name, requestBody))
                    }
                }
                
                val response = apiService.addProductReview(
                    productId = productId,
                    fields = fields,
                    images = if (imageParts.isEmpty()) null else imageParts
                )
                
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isSubmittingReview = false,
                        reviewSubmitMessage = "Review submitted successfully!",
                        canWriteReview = false
                    )
                    // Reload reviews to show the new one
                    loadReviews()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = _uiState.value.copy(
                        isSubmittingReview = false,
                        reviewSubmitMessage = "Failed to submit review: ${errorBody ?: "Unknown error"}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmittingReview = false,
                    reviewSubmitMessage = "Error: ${e.message}"
                )
            }
        }
    }
    
    fun clearReviewSubmitMessage() {
        _uiState.value = _uiState.value.copy(reviewSubmitMessage = null)
    }
    
    fun loadCartCount() {
        viewModelScope.launch {
            try {
                val response = apiService.getCart()
                if (response.isSuccessful && response.body()?.data != null) {
                    val cart = response.body()!!.data!!
                    _uiState.value = _uiState.value.copy(cartItemCount = cart.itemCount)
                }
            } catch (e: Exception) {
                // Ignore errors for cart count
            }
        }
    }
    
    fun incrementQuantity() {
        val current = _uiState.value.quantity
        if (current < 10) {
            _uiState.value = _uiState.value.copy(quantity = current + 1)
        }
    }
    
    fun decrementQuantity() {
        val current = _uiState.value.quantity
        val minQty = _uiState.value.minQty
        if (current > minQty) {
            _uiState.value = _uiState.value.copy(quantity = current - 1)
        }
    }
    
    fun addToCart() {
        val product = _uiState.value.product ?: return
        
        // Check if user is trying to add their own product
        val currentUserId = _uiState.value.currentUserId
        if (currentUserId > 0 && product.userId == currentUserId) {
            _uiState.value = _uiState.value.copy(
                cartMessage = "You cannot add your own product to cart"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddingToCart = true, cartMessage = null)
            try {
                val response = apiService.addShopProductToCart(
                    AddToCartRequest(
                        productId = product.productId,
                        quantity = _uiState.value.quantity
                    )
                )
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isAddingToCart = false,
                        addToCartSuccess = true,
                        cartMessage = "Added to cart!"
                    )
                    loadCartCount()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isAddingToCart = false,
                        cartMessage = "Failed to add to cart"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAddingToCart = false,
                    cartMessage = e.message
                )
            }
        }
    }
    
    fun buyNow() {
        val product = _uiState.value.product ?: return
        
        // Check if user is trying to buy their own product
        val currentUserId = _uiState.value.currentUserId
        if (currentUserId > 0 && product.userId == currentUserId) {
            _uiState.value = _uiState.value.copy(
                cartMessage = "You cannot buy your own product"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddingToCart = true, cartMessage = null, buyNowReady = false)
            try {
                // Add item to cart with selected quantity
                val addResponse = apiService.addShopProductToCart(
                    AddToCartRequest(
                        productId = product.productId,
                        quantity = _uiState.value.quantity
                    )
                )
                if (addResponse.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isAddingToCart = false,
                        buyNowReady = true
                    )
                    loadCartCount()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isAddingToCart = false,
                        cartMessage = "Failed to add to cart"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAddingToCart = false,
                    cartMessage = e.message
                )
            }
        }
    }
    
    fun clearBuyNowReady() {
        _uiState.value = _uiState.value.copy(buyNowReady = false)
    }
    
    fun clearCartMessage() {
        _uiState.value = _uiState.value.copy(cartMessage = null, addToCartSuccess = false)
    }
    
    /**
     * Start chat with seller (for old products)
     */
    fun startChat(onSuccess: (conversationId: String, sellerName: String) -> Unit) {
        val product = _uiState.value.product ?: return
        val userId = _uiState.value.currentUserId
        val sellerId = product.userId?.toLong() ?: return
        val sellerName = product.businessName ?: "Seller"
        
        if (userId == 0L || userId == sellerId) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingChat = true)
            
            try {
                val conversationId = chatRepository.getOrCreateConversation(
                    currentUserId = userId,
                    otherUserId = sellerId,
                    listingId = product.listingId,
                    listingTitle = product.productName,
                    listingImage = product.imageUrl,
                    otherUserName = sellerName
                )
                
                _uiState.value = _uiState.value.copy(isCreatingChat = false)
                onSuccess(conversationId, sellerName)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingChat = false,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * Initiate call with seller (for old products)
     * Returns the seller's phone number and user ID for the call system
     */
    fun getSellerCallInfo(): Triple<String?, Long?, String?> {
        val product = _uiState.value.product
        return Triple(
            product?.businessPhone,
            product?.userId?.toLong(),
            product?.businessName
        )
    }
    
    /**
     * Send call invitation to seller
     */
    fun sendCallInvitation(callId: String, onSuccess: () -> Unit) {
        val product = _uiState.value.product ?: return
        val sellerId = product.userId?.toLong() ?: return
        val senderName = _uiState.value.currentUserName
        
        viewModelScope.launch {
            try {
                // Create a conversation ID for the call
                val conversationId = "call_${_uiState.value.currentUserId}_${sellerId}"
                
                chatRepository.sendCallInvitation(
                    recipientId = sellerId,
                    conversationId = conversationId,
                    callId = callId,
                    senderName = senderName
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to initiate call")
            }
        }
    }
}
