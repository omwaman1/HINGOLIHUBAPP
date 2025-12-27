package com.hingoli.hub.data.api

import com.hingoli.hub.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ==================== PREFETCH (Batch endpoint) ====================
    
    @GET("prefetch")
    suspend fun prefetch(
        @Query("city") city: String? = null
    ): Response<PrefetchResponse>
    
    // ==================== AUTH ====================
    
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
    
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String
    ): Response<LoginResponse>
    
    @POST("auth/check-phone")
    suspend fun checkPhone(
        @Body request: CheckPhoneRequest
    ): Response<ApiResponse<CheckPhoneData>>
    
    @POST("auth/send-otp")
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): Response<ApiResponse<SendOtpData>>
    
    @POST("auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<OtpLoginResponse>
    
    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiResponse<ResetPasswordData>>
    
    @POST("auth/verify-otp")
    suspend fun signupWithOtp(
        @Body request: SignupWithOtpRequest
    ): Response<OtpLoginResponse>
    
    // ==================== CATEGORIES ====================
    
    @GET("categories")
    suspend fun getCategories(
        @Query("type") listingType: String, // "services" or "business"
        @Query("parent_id") parentId: Int? = null
    ): Response<ApiResponse<List<Category>>>
    
    @GET("categories/{id}")
    suspend fun getCategoryById(
        @Path("id") categoryId: Int
    ): Response<ApiResponse<Category>>
    
    @GET("categories/{id}/subcategories")
    suspend fun getSubcategories(
        @Path("id") categoryId: Int
    ): Response<ApiResponse<List<Category>>>
    
    // ==================== LISTINGS ====================
    
    @GET("listings")
    suspend fun getListings(
        @Query("type") listingType: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("subcategory_id") subcategoryId: Int? = null,
        @Query("city") city: String? = null,
        @Query("search") search: String? = null,
        @Query("condition") condition: String? = null, // "old" or "new"
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<ApiResponse<List<Listing>>>
    
    @GET("listings")
    suspend fun getFeaturedListings(
        @Query("featured") featured: Int = 1,
        @Query("per_page") limit: Int = 10
    ): Response<ApiResponse<List<Listing>>>
    
    @GET("listings/{id}")
    suspend fun getListingById(
        @Path("id") listingId: Long
    ): Response<ApiResponse<Listing>>
    
    @GET("listings/{id}/price-list")
    suspend fun getListingPriceList(
        @Path("id") listingId: Long
    ): Response<ApiResponse<List<PriceListItem>>>
    
    @GET("listings/{id}/reviews")
    suspend fun getListingReviews(
        @Path("id") listingId: Long,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ): Response<ApiResponse<List<Review>>>
    
    @POST("listings/{id}/reviews")
    suspend fun addReview(
        @Path("id") listingId: Long,
        @Body request: AddReviewRequest
    ): Response<ApiResponse<Review>>
    
    @Multipart
    @POST("listings")
    suspend fun createListing(
        @PartMap fields: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part image: okhttp3.MultipartBody.Part? = null
    ): Response<ApiResponse<Listing>>
    
    @Multipart
    @PUT("listings/{id}")
    suspend fun updateListing(
        @Path("id") listingId: Long,
        @PartMap fields: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part image: okhttp3.MultipartBody.Part? = null
    ): Response<ApiResponse<Listing>>
    
    // ==================== CITIES ====================
    
    @GET("cities")
    suspend fun getCities(
        @Query("state_id") stateId: Int? = null
    ): Response<ApiResponse<List<City>>>
    
    // ==================== BANNERS ====================
    
    @GET("banners")
    suspend fun getBanners(
        @Query("placement") placement: String? = null,
        @Query("city") city: String? = null
    ): Response<ApiResponse<List<Banner>>>


    // ==================== USER LISTINGS ====================
    
    @GET("user/listings")
    suspend fun getMyListings(
        @Query("type") type: String? = null
    ): Response<ApiResponse<List<Listing>>>
    
    @DELETE("listings/{id}")
    suspend fun deleteListing(
        @Path("id") listingId: Long
    ): Response<ApiResponse<Unit>>
    
    // ==================== PRICE LIST ====================
    
    @POST("listings/{id}/price-list")
    suspend fun addPriceListItem(
        @Path("id") listingId: Long,
        @Body request: AddPriceListItemRequest
    ): Response<ApiResponse<PriceListItem>>
    
    @DELETE("listings/{id}/price-list/{itemId}")
    suspend fun deletePriceListItem(
        @Path("id") listingId: Long,
        @Path("itemId") itemId: Long
    ): Response<ApiResponse<Unit>>
    
    // ==================== GALLERY IMAGES ====================
    
    @Multipart
    @POST("listings/{id}/images")
    suspend fun addListingImage(
        @Path("id") listingId: Long,
        @Part image: okhttp3.MultipartBody.Part
    ): Response<ApiResponse<ListingImage>>
    
    @DELETE("listings/{id}/images/{imageId}")
    suspend fun deleteListingImage(
        @Path("id") listingId: Long,
        @Path("imageId") imageId: Long
    ): Response<ApiResponse<Unit>>
    
    // ==================== USER PROFILE ====================
    
    @GET("user/profile")
    suspend fun getProfile(): Response<ApiResponse<UserProfile>>
    
    @PUT("user/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<UserProfile>>
    
    // ==================== NOTIFICATIONS ====================
    
    @POST("notifications/register-token")
    suspend fun registerFcmToken(
        @Body request: RegisterFcmTokenRequest
    ): Response<ApiResponse<Any>>
    
    @GET("notifications/history")
    suspend fun getNotificationHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<NotificationHistoryResponse>
    
    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>
    
    @POST("notifications/mark-read")
    suspend fun markNotificationsRead(
        @Body request: MarkReadRequest? = null
    ): Response<ApiResponse<Any>>
    
    // ==================== CART ====================
    
    @GET("cart")
    suspend fun getCart(): Response<ApiResponse<CartResponse>>
    
    @POST("cart")
    suspend fun addToCart(
        @Body request: AddToCartRequest
    ): Response<ApiResponse<Any>>
    
    @PUT("cart/{id}")
    suspend fun updateCartItem(
        @Path("id") cartItemId: Long,
        @Body request: UpdateCartRequest
    ): Response<ApiResponse<Any>>
    
    @DELETE("cart/{id}")
    suspend fun removeCartItem(
        @Path("id") cartItemId: Long
    ): Response<ApiResponse<Any>>
    
    @DELETE("cart")
    suspend fun clearCart(): Response<ApiResponse<Any>>
    
    // ==================== ADDRESSES ====================
    
    @GET("addresses")
    suspend fun getAddresses(): Response<ApiResponse<List<UserAddress>>>
    
    @POST("addresses")
    suspend fun addAddress(
        @Body request: AddAddressRequest
    ): Response<ApiResponse<Any>>
    
    @PUT("addresses/{id}")
    suspend fun updateAddress(
        @Path("id") addressId: Int,
        @Body request: AddAddressRequest
    ): Response<ApiResponse<Any>>
    
    @DELETE("addresses/{id}")
    suspend fun deleteAddress(
        @Path("id") addressId: Int
    ): Response<ApiResponse<Any>>
    
    @PUT("addresses/{id}/default")
    suspend fun setDefaultAddress(
        @Path("id") addressId: Int
    ): Response<ApiResponse<Any>>
    
    // ==================== ORDERS ====================
    
    @GET("orders")
    suspend fun getOrders(): Response<ApiResponse<List<Order>>>
    
    @GET("orders/{id}")
    suspend fun getOrderById(
        @Path("id") orderId: Long
    ): Response<ApiResponse<OrderDetail>>
    
    @POST("orders")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): Response<ApiResponse<CreateOrderResponse>>
    
    @POST("orders/{id}/verify")
    suspend fun verifyPayment(
        @Path("id") orderId: Long,
        @Body request: VerifyPaymentRequest
    ): Response<ApiResponse<Any>>
    
    // ==================== DELIVERY ====================
    
    @GET("delivery/check")
    suspend fun checkDelivery(
        @Query("pincode") pincode: String
    ): Response<ApiResponse<DeliveryEstimate>>
    
    // ==================== ENQUIRIES ====================
    
    @POST("enquiries")
    suspend fun logEnquiry(
        @Body request: LogEnquiryRequest
    ): Response<ApiResponse<Any>>
    
    // ==================== SHOP PRODUCTS ====================
    
    @GET("products")
    suspend fun getShopProducts(
        @Query("listing_id") listingId: Long? = null, // Get all products for specific listing
        @Query("condition") condition: String? = null, // 'new' or 'old'
        @Query("category_id") categoryId: Int? = null,
        @Query("subcategory_id") subcategoryId: Int? = null,
        @Query("city") city: String? = null,
        @Query("search") search: String? = null, // Search by product name
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<ApiResponse<ShopProductsResponse>>
    
    @GET("products/{id}")
    suspend fun getShopProduct(
        @Path("id") productId: Long
    ): Response<ApiResponse<ShopProduct>>
    
    // Add product to business listing (showcase only, sell_online=0)
    @Multipart
    @POST("products")
    suspend fun addBusinessProduct(
        @PartMap fields: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part image: okhttp3.MultipartBody.Part? = null
    ): Response<ApiResponse<ShopProduct>>
    
    // Update product (PUT /products/{id}) - supports multipart image upload
    @Multipart
    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: Long,
        @PartMap fields: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part image: okhttp3.MultipartBody.Part? = null
    ): Response<ApiResponse<ShopProduct>>
    
    // Delete product from business listing
    @DELETE("products/{id}")
    suspend fun deleteBusinessProduct(
        @Path("id") productId: Long
    ): Response<ApiResponse<Any>>
    
    // Add shop product to cart (uses same endpoint with product_id)
    @POST("cart")
    suspend fun addShopProductToCart(
        @Body request: AddToCartRequest
    ): Response<ApiResponse<Any>>
    
    // ==================== PRODUCT REVIEWS ====================
    
    @GET("products/{id}/reviews")
    suspend fun getProductReviews(
        @Path("id") productId: Long,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ): Response<ApiResponse<List<Review>>>
    
    @Multipart
    @POST("products/{id}/reviews")
    suspend fun addProductReview(
        @Path("id") productId: Long,
        @PartMap fields: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part images: List<okhttp3.MultipartBody.Part>? = null
    ): Response<ApiResponse<Review>>
    
    // Simple JSON version without images
    @POST("products/{id}/reviews")
    suspend fun addProductReviewSimple(
        @Path("id") productId: Long,
        @Body request: AddReviewRequest
    ): Response<ApiResponse<Review>>
}
