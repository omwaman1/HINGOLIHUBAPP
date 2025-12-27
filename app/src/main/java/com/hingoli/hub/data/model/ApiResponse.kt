package com.hingoli.hub.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("pagination")
    val pagination: Pagination? = null
)

data class Pagination(
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("per_page")
    val perPage: Int,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("total_pages")
    val totalPages: Int
)

data class PriceListItem(
    @SerializedName("item_id")
    val itemId: Long,
    
    @SerializedName("item_name")
    val itemName: String,
    
    @SerializedName("item_description")
    val itemDescription: String? = null,
    
    @SerializedName("item_category")
    val itemCategory: String? = null,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("discounted_price")
    val discountedPrice: Double? = null,
    
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("is_popular")
    val isPopular: Boolean = false
)

data class Review(
    @SerializedName("review_id")
    val reviewId: Long,
    
    @SerializedName("rating")
    val rating: Int,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("content")
    val content: String? = null,
    
    @SerializedName("images")
    val images: List<String>? = null,
    
    @SerializedName("reviewer")
    val reviewer: User? = null,
    
    @SerializedName("seller_response")
    val sellerResponse: String? = null,
    
    @SerializedName("helpful_count")
    val helpfulCount: Int = 0,
    
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class AddPriceListItemRequest(
    @SerializedName("item_name")
    val itemName: String,
    
    @SerializedName("item_description")
    val itemDescription: String? = null,
    
    @SerializedName("item_category")
    val itemCategory: String? = null,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("discounted_price")
    val discountedPrice: Double? = null,
    
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null
)

data class AddReviewRequest(
    @SerializedName("rating")
    val rating: Int,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("content")
    val content: String? = null
)

data class LogEnquiryRequest(
    @SerializedName("listing_id")
    val listingId: Long,
    
    @SerializedName("enquiry_type")
    val enquiryType: String, // "call", "chat", "whatsapp", "contact_form"
    
    @SerializedName("message")
    val message: String? = null
)
