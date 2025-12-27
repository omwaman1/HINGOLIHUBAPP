package com.hingoli.hub.data.model

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

/**
 * Shop Product - Products sold by businesses
 */
@Stable
data class ShopProduct(
    @SerializedName("product_id") val productId: Long,
    @SerializedName("listing_id") val listingId: Long,
    @SerializedName("product_name") val productName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("subcategory_id") val subcategoryId: Int?,
    @SerializedName("subcategory_name") val subcategoryName: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("discounted_price") val discountedPrice: Double?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("stock_qty") val stockQty: Int?,
    @SerializedName("min_qty") val minQty: Int = 1,
    @SerializedName("sell_online") val sellOnline: Boolean = false,
    @SerializedName("condition") val condition: String? = null,
    @SerializedName("business_name") val businessName: String?,
    @SerializedName("business_phone") val businessPhone: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("user_id") val userId: Long? = null,
    @SerializedName("created_at") val createdAt: String?
)

/**
 * Response wrapper for products list
 * Uses Pagination from ApiResponse.kt
 */
data class ShopProductsResponse(
    @SerializedName("products") val products: List<ShopProduct>,
    @SerializedName("pagination") val pagination: Pagination?
)
