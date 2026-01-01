package com.hingoli.hub.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.TokenManager
import com.hingoli.hub.data.model.Banner
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.data.model.Listing
import com.hingoli.hub.data.model.PriceListItem
import com.hingoli.hub.data.model.Review
import com.hingoli.hub.data.model.ShopProduct
import com.hingoli.hub.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.util.Calendar
import javax.inject.Inject

data class ListingDetailUiState(
    val listing: Listing? = null,
    val priceList: List<PriceListItem> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val shopProducts: List<ShopProduct> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingPriceList: Boolean = false,
    val isLoadingReviews: Boolean = false,
    val isLoadingShopProducts: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0,
    val currentUserId: Long = 0,
    val currentUserName: String = "",
    val isOwnListing: Boolean = false,
    val isCreatingChat: Boolean = false,
    val chatConversationId: String? = null,
    val isLoggedIn: Boolean = false,
    val isProfileComplete: Boolean = false,
    // For Add Product dialog
    val productCategories: List<Category> = emptyList(),
    val productSubcategories: List<Category> = emptyList(),
    val bottomBanners: List<Banner> = emptyList(),
    // Call timing configuration
    val callTimingEnabled: Boolean = true,
    val callStartHour: Int = 8,
    val callEndHour: Int = 22,
    val callTimingMessage: String = "Call service available from 8 AM to 10 PM",
    val callTimingMessageMr: String = "कॉल सेवा सकाळी 8 ते रात्री 10 वाजेपर्यंत उपलब्ध"
)

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager,
    private val apiService: com.hingoli.hub.data.api.ApiService,
    private val sharedDataRepository: SharedDataRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListingDetailUiState())
    val uiState: StateFlow<ListingDetailUiState> = _uiState.asStateFlow()
    
    private var currentListingId: Long? = null
    
    init {
        loadCurrentUser()
        loadProductCategories()
        loadCallTimingConfig()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: 0L
            val userName = tokenManager.getUserName() ?: ""
            val isProfileComplete = checkProfileComplete()
            _uiState.value = _uiState.value.copy(
                currentUserId = userId,
                currentUserName = userName,
                isLoggedIn = userId > 0,
                isProfileComplete = isProfileComplete
            )
        }
    }
    
    /**
     * Load call timing configuration from app-config API
     */
    private fun loadCallTimingConfig() {
        viewModelScope.launch {
            try {
                val response = apiService.getAppConfig()
                if (response.isSuccessful && response.body()?.data != null) {
                    val config = response.body()!!.data!!
                    _uiState.value = _uiState.value.copy(
                        callTimingEnabled = config.callTimingEnabled,
                        callStartHour = config.callStartHour,
                        callEndHour = config.callEndHour,
                        callTimingMessage = config.callTimingMessage,
                        callTimingMessageMr = config.callTimingMessageMr
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("CallTiming", "Error loading call timing: ${e.message}")
            }
        }
    }
    
    /**
     * Check if calls are allowed at current time
     * Returns true if calls are allowed, false if outside allowed hours
     */
    fun isCallAllowed(): Boolean {
        val state = _uiState.value
        if (!state.callTimingEnabled) return true // Timing check disabled
        
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return currentHour >= state.callStartHour && currentHour < state.callEndHour
    }
    
    /**
     * Get appropriate call timing message based on language
     */
    fun getCallTimingMessage(isMarathi: Boolean): String {
        return if (isMarathi) _uiState.value.callTimingMessageMr else _uiState.value.callTimingMessage
    }
    
    /**
     * Check if user profile has all required fields filled
     * Required: username (not auto-generated), gender, date_of_birth
     * Note: email is optional
     */
    private suspend fun checkProfileComplete(): Boolean {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body()?.data != null) {
                val profile = response.body()!!.data!!
                val usernameOk = !profile.username.startsWith("User") || !profile.username.contains("_")
                val genderOk = !profile.gender.isNullOrBlank()
                val dobOk = !profile.dateOfBirth.isNullOrBlank()
                usernameOk && genderOk && dobOk
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileCheck", "Error checking profile: ${e.message}")
            false
        }
    }
    
    fun loadListing(listingId: Long) {
        currentListingId = listingId
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = listingRepository.getListingById(listingId)) {
                is ListingDetailResult.Success -> {
                    val listing = result.listing
                    val isOwn = listing.user?.userId == _uiState.value.currentUserId
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        listing = listing,
                        isOwnListing = isOwn,
                        bottomBanners = sharedDataRepository.getBannersForPlacement("listing_detail_bottom")
                    )
                    // Load additional data
                    loadPriceList(listingId)
                    loadReviews(listingId)
                    
                    // Load shop products for business and services listings
                    if (listing.listingType == "business" || listing.listingType == "services") {
                        loadShopProducts(listingId)
                        // Reload categories based on listing type
                        currentListingType = listing.listingType
                        loadProductCategoriesForCondition("new")
                    }
                }
                is ListingDetailResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    private fun loadPriceList(listingId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPriceList = true)
            
            when (val result = listingRepository.getPriceList(listingId)) {
                is PriceListResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingPriceList = false,
                        priceList = result.items
                    )
                }
                is PriceListResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingPriceList = false)
                }
            }
        }
    }
    
    private fun loadReviews(listingId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingReviews = true)
            
            when (val result = listingRepository.getReviews(listingId)) {
                is ReviewsResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingReviews = false,
                        reviews = result.reviews
                    )
                }
                is ReviewsResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingReviews = false)
                }
            }
        }
    }
    
    private fun loadShopProducts(listingId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingShopProducts = true)
            
            try {
                android.util.Log.d("DebugProduct", "loadShopProducts called for listingId: $listingId")
                // Get products for this listing (API filters by listing_id, bypasses sell_online filter)
                val response = apiService.getShopProducts(listingId = listingId, page = 1, perPage = 50)
                android.util.Log.d("DebugProduct", "loadShopProducts - HTTP: ${response.code()}, success: ${response.body()?.success}")
                if (response.isSuccessful && response.body()?.data != null) {
                    val listingProducts = response.body()!!.data!!.products
                    android.util.Log.d("DebugProduct", "loadShopProducts - loaded ${listingProducts.size} products")
                    listingProducts.forEachIndexed { i, p ->
                        android.util.Log.d("DebugProduct", "Product[$i]: ${p.productName}, condition=${p.condition}, sellOnline=${p.sellOnline}")
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoadingShopProducts = false,
                        shopProducts = listingProducts
                    )
                } else {
                    android.util.Log.d("DebugProduct", "loadShopProducts - failed or no data")
                    _uiState.value = _uiState.value.copy(isLoadingShopProducts = false)
                }
            } catch (e: Exception) {
                android.util.Log.e("DebugProduct", "loadShopProducts error: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoadingShopProducts = false)
            }
        }
    }
    
    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }
    
    fun retry() {
        currentListingId?.let { loadListing(it) }
    }
    
    /**
     * Refresh profile completion status (called when returning from Edit Profile)
     */
    fun refreshProfileStatus() {
        viewModelScope.launch {
            val isProfileComplete = checkProfileComplete()
            _uiState.value = _uiState.value.copy(isProfileComplete = isProfileComplete)
        }
    }
    
    /**
     * Log a contact attempt (call, chat, whatsapp) to the backend
     * Called when user taps Call or Chat button
     * This is fire-and-forget - we don't block user action on this
     */
    fun logEnquiry(enquiryType: String) {
        val listingId = currentListingId ?: return
        if (_uiState.value.isOwnListing) return // Don't log own listing contacts
        
        viewModelScope.launch {
            try {
                val request = com.hingoli.hub.data.model.LogEnquiryRequest(
                    listingId = listingId,
                    enquiryType = enquiryType
                )
                apiService.logEnquiry(request)
            } catch (e: Exception) {
                // Silent fail - don't block user action
            }
        }
    }
    
    fun startChat(onSuccess: (conversationId: String, listingTitle: String) -> Unit) {
        val listing = _uiState.value.listing ?: return
        val userId = _uiState.value.currentUserId
        val ownerId = listing.user?.userId ?: return
        val ownerName = listing.user?.username ?: "User"
        
        if (userId == 0L || userId == ownerId) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingChat = true)
            
            try {
                val conversationId = chatRepository.getOrCreateConversation(
                    currentUserId = userId,
                    otherUserId = ownerId,
                    listingId = listing.listingId,
                    listingTitle = listing.title,
                    listingImage = listing.mainImageUrl,
                    otherUserName = ownerName
                )
                
                _uiState.value = _uiState.value.copy(
                    isCreatingChat = false,
                    chatConversationId = conversationId
                )
                
                // Use owner name as chat title
                onSuccess(conversationId, ownerName)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingChat = false,
                    error = e.message
                )
            }
        }
    }
    
    // ==================== OWNER CRUD OPERATIONS ====================
    
    fun addPriceListItem(
        itemName: String,
        price: Double,
        description: String? = null,
        category: String? = null,
        discountedPrice: Double? = null,
        durationMinutes: Int? = null
    ) {
        val listingId = currentListingId ?: return
        
        viewModelScope.launch {
            try {
                val request = com.hingoli.hub.data.model.AddPriceListItemRequest(
                    itemName = itemName,
                    price = price,
                    itemDescription = description,
                    itemCategory = category,
                    discountedPrice = discountedPrice,
                    durationMinutes = durationMinutes
                )
                
                val response = apiService.addPriceListItem(listingId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Reload price list
                    loadPriceList(listingId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.body()?.message ?: "Failed to add item"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deletePriceListItem(itemId: Long) {
        val listingId = currentListingId ?: return
        
        viewModelScope.launch {
            try {
                val response = apiService.deletePriceListItem(listingId, itemId)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Update the list locally without reloading
                    val updatedList = _uiState.value.priceList.filter { it.itemId != itemId }
                    _uiState.value = _uiState.value.copy(priceList = updatedList)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.body()?.message ?: "Failed to delete item"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun addGalleryImage(context: android.content.Context, imageUri: android.net.Uri) {
        val listingId = currentListingId ?: return
        
        viewModelScope.launch {
            try {
                // Compress and optimize image before upload
                val compressedFile = com.hingoli.hub.util.ImageCompressor.compressImage(
                    context = context,
                    imageUri = imageUri,
                    maxWidth = 1920,
                    maxHeight = 1080,
                    quality = 80,
                    useWebP = true
                )
                
                if (compressedFile == null) {
                    _uiState.value = _uiState.value.copy(error = "Failed to process image")
                    return@launch
                }
                
                val mimeType = if (compressedFile.extension == "webp") "image/webp" else "image/jpeg"
                val requestBody = okhttp3.RequestBody.create(
                    mimeType.toMediaTypeOrNull(),
                    compressedFile
                )
                val imagePart = okhttp3.MultipartBody.Part.createFormData("image", compressedFile.name, requestBody)
                
                val response = apiService.addListingImage(listingId, imagePart)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Reload listing to get updated images
                    loadListing(listingId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.body()?.message ?: "Failed to upload image"
                    )
                }
                
                // Clean up temp file
                compressedFile.delete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deleteGalleryImage(imageId: Long) {
        val listingId = currentListingId ?: return
        
        viewModelScope.launch {
            try {
                val response = apiService.deleteListingImage(listingId, imageId)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Reload listing to get updated images
                    loadListing(listingId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.body()?.message ?: "Failed to delete image"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun updateDescription(newDescription: String) {
        val listingId = currentListingId ?: return
        
        viewModelScope.launch {
            try {
                val requestMap = mutableMapOf<String, okhttp3.RequestBody>()
                requestMap["description"] = okhttp3.RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    newDescription
                )
                
                val response = apiService.updateListing(listingId, requestMap, null)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Update local state
                    _uiState.value.listing?.let { listing ->
                        _uiState.value = _uiState.value.copy(
                            listing = listing.copy(description = newDescription)
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.body()?.message ?: "Failed to update description"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun addReview(rating: Int, title: String?, content: String?) {
        val listingId = currentListingId ?: return
        
        viewModelScope.launch {
            try {
                val request = com.hingoli.hub.data.model.AddReviewRequest(
                    rating = rating,
                    title = title,
                    content = content
                )
                
                val response = apiService.addReview(listingId, request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Add the new review optimistically
                    val newReview = response.body()?.data
                    if (newReview != null) {
                        val currentReviews = _uiState.value.reviews.toMutableList()
                        currentReviews.add(0, newReview)
                        _uiState.value = _uiState.value.copy(reviews = currentReviews)
                    }
                    loadReviews(listingId)
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to submit review"
                    _uiState.value = _uiState.value.copy(error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    // ==================== SHOP PRODUCTS (Business Owners) ====================
    
    fun addBusinessProduct(
        context: android.content.Context,
        productName: String,
        description: String?,
        price: Double,
        imageUri: android.net.Uri?,
        condition: String,
        sellOnline: Boolean,
        categoryId: Int?,
        subcategoryId: Int?
    ) {
        val listingId = currentListingId ?: return
        
        viewModelScope.launch {
            try {
                // Build request map
                val requestMap = mutableMapOf<String, okhttp3.RequestBody>()
                
                fun addField(key: String, value: String) {
                    requestMap[key] = okhttp3.RequestBody.create(
                        "text/plain".toMediaTypeOrNull(),
                        value
                    )
                }
                
                addField("listing_id", listingId.toString())
                addField("product_name", productName)
                addField("price", price.toString())
                addField("condition", condition)
                addField("sell_online", if (sellOnline) "1" else "0")
                if (!description.isNullOrBlank()) {
                    addField("description", description)
                }
                // For NEW products, send shop_category_id; for OLD products, send category_id
                if (categoryId != null) {
                    if (condition == "new") {
                        addField("shop_category_id", categoryId.toString())
                    } else {
                        addField("category_id", categoryId.toString())
                    }
                }
                if (subcategoryId != null) {
                    addField("subcategory_id", subcategoryId.toString())
                }
                
                // DEBUG: Log all product fields
                android.util.Log.d("DebugProduct", "=== ADD PRODUCT FROM BUSINESS DETAIL ===")
                android.util.Log.d("DebugProduct", "listingId: $listingId")
                android.util.Log.d("DebugProduct", "productName: $productName")
                android.util.Log.d("DebugProduct", "price: $price")
                android.util.Log.d("DebugProduct", "condition: $condition")
                android.util.Log.d("DebugProduct", "sellOnline: $sellOnline")
                android.util.Log.d("DebugProduct", "categoryId: $categoryId (sent as: ${if (condition == "new") "shop_category_id" else "category_id"})")
                android.util.Log.d("DebugProduct", "subcategoryId: $subcategoryId")
                android.util.Log.d("DebugProduct", "imageUri: $imageUri")
                
                // Handle image if provided
                var imagePart: okhttp3.MultipartBody.Part? = null
                if (imageUri != null) {
                    val compressedFile = com.hingoli.hub.util.ImageCompressor.compressImage(
                        context = context,
                        imageUri = imageUri,
                        maxWidth = 800,
                        maxHeight = 800,
                        quality = 80,
                        useWebP = true
                    )
                    
                    if (compressedFile != null) {
                        val mimeType = if (compressedFile.extension == "webp") "image/webp" else "image/jpeg"
                        val requestBody = okhttp3.RequestBody.create(
                            mimeType.toMediaTypeOrNull(),
                            compressedFile
                        )
                        imagePart = okhttp3.MultipartBody.Part.createFormData("image", compressedFile.name, requestBody)
                    }
                }
                android.util.Log.d("DebugProduct", "imagePart is null: ${imagePart == null}")
                android.util.Log.d("DebugProduct", "=========================================")
                
                val response = apiService.addBusinessProduct(requestMap, imagePart)
                android.util.Log.d("DebugProduct", "=== API RESPONSE ===")
                android.util.Log.d("DebugProduct", "HTTP Code: ${response.code()}")
                android.util.Log.d("DebugProduct", "isSuccessful: ${response.isSuccessful}")
                android.util.Log.d("DebugProduct", "success: ${response.body()?.success}")
                android.util.Log.d("DebugProduct", "message: ${response.body()?.message}")
                if (!response.isSuccessful) {
                    android.util.Log.d("DebugProduct", "errorBody: ${response.errorBody()?.string()}")
                }
                android.util.Log.d("DebugProduct", "====================")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Reload shop products
                    loadShopProducts(listingId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.body()?.message ?: "Failed to add product"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.d("DebugProduct", "Add product EXCEPTION: ${e.message}")
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deleteBusinessProduct(productId: Long) {
        val listingId = currentListingId ?: return
        
        // Find the product to get its condition
        val product = _uiState.value.shopProducts.find { it.productId == productId }
        val isOldProduct = product?.condition == "old" || product?.isOldProduct == true
        
        viewModelScope.launch {
            try {
                android.util.Log.d("DebugDelete", "deleteBusinessProduct - productId: $productId, isOldProduct: $isOldProduct, condition: ${product?.condition}")
                
                val response = if (isOldProduct) {
                    // Old products use DELETE /old-products/{id}
                    apiService.deleteOldProduct(productId)
                } else {
                    // New products use DELETE /products/{id}
                    apiService.deleteBusinessProduct(productId)
                }
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Update list locally
                    val updatedProducts = _uiState.value.shopProducts.filter { it.productId != productId }
                    _uiState.value = _uiState.value.copy(shopProducts = updatedProducts)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.body()?.message ?: "Failed to delete product"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    // ==================== CATEGORY LOADING (For Add Product Dialog) ====================
    
    // Track current condition for subcategory loading
    private var currentProductCondition: String = "new"
    // Track current listing type for category loading
    private var currentListingType: String = "business"
    
    private fun loadProductCategories() {
        // Will be called with correct parameters when listing loads
        // Default to shop categories for NEW products
        loadProductCategoriesForCondition("new")
    }
    
    /**
     * Load categories based on listing type and condition:
     * - Services listing -> load services categories
     * - Business/selling listing with condition "new" -> load shop_categories
     * - Business/selling listing with condition "old" -> load old_categories
     */
    fun loadProductCategoriesForCondition(condition: String) {
        currentProductCondition = condition
        val listingType = _uiState.value.listing?.listingType ?: currentListingType
        currentListingType = listingType
        
        viewModelScope.launch {
            try {
                when {
                    // For SERVICES listings, always use services categories
                    listingType == "services" -> {
                        val servicesCategories = sharedDataRepository.getCategories("services")
                        val mainCategories = servicesCategories.filter { it.parentId == null }
                        _uiState.value = _uiState.value.copy(
                            productCategories = mainCategories,
                            productSubcategories = emptyList()
                        )
                    }
                    // For NEW products (business listings), use shop_categories
                    condition == "new" -> {
                        val shopCategories = sharedDataRepository.getShopCategories()
                        val mainCategories = shopCategories.map { it.toCategory() }
                        _uiState.value = _uiState.value.copy(
                            productCategories = mainCategories,
                            productSubcategories = emptyList()
                        )
                    }
                    // For OLD/used products, use old_categories
                    else -> {
                        val oldCategories = sharedDataRepository.getOldCategories()
                        val mainCategories = oldCategories.map { 
                            Category(
                                categoryId = it.id,
                                parentId = it.parentId,
                                name = it.name,
                                nameMr = it.nameMr,
                                slug = it.slug ?: "",
                                listingType = "old",
                                iconUrl = it.imageUrl,
                                imageUrl = it.imageUrl,
                                description = null,
                                listingCount = 0,
                                depth = 0
                            )
                        }
                        _uiState.value = _uiState.value.copy(
                            productCategories = mainCategories,
                            productSubcategories = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ListingDetail", "Failed to load product categories: ${e.message}")
            }
        }
    }
    
    fun loadProductSubcategories(categoryId: Int) {
        viewModelScope.launch {
            try {
                when {
                    // For SERVICES listings, load from services categories
                    currentListingType == "services" -> {
                        val subcategories = sharedDataRepository.getSubcategoriesForParent(categoryId)
                        _uiState.value = _uiState.value.copy(productSubcategories = subcategories)
                    }
                    // For NEW products (business), load from shop_categories
                    currentProductCondition == "new" -> {
                        val shopSubcategories = sharedDataRepository.getShopSubcategories(categoryId)
                        val subcategories = shopSubcategories.map { it.toCategory() }
                        _uiState.value = _uiState.value.copy(productSubcategories = subcategories)
                    }
                    // For OLD/used products, load from old_categories
                    else -> {
                        val oldSubcategories = sharedDataRepository.getOldSubcategories(categoryId)
                        val subcategories = oldSubcategories.map { 
                            Category(
                                categoryId = it.id,
                                parentId = it.parentId,
                                name = it.name,
                                nameMr = it.nameMr,
                                slug = it.slug ?: "",
                                listingType = "old",
                                iconUrl = it.imageUrl,
                                imageUrl = it.imageUrl,
                                description = null,
                                listingCount = 0,
                                depth = 1
                            )
                        }
                        _uiState.value = _uiState.value.copy(productSubcategories = subcategories)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ListingDetail", "Failed to load product subcategories: ${e.message}")
            }
        }
    }
    
    fun clearProductSubcategories() {
        _uiState.value = _uiState.value.copy(productSubcategories = emptyList())
    }
}

