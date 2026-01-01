package com.hingoli.hub.data.model

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

/**
 * Old product model for used/second-hand items (C2C marketplace).
 * Different from ShopProduct as it's sold by individuals, not businesses.
 */
@Stable
data class OldProduct(
    @SerializedName("product_id")
    val productId: Long,
    
    @SerializedName("user_id")
    val userId: Long? = null,
    
    @SerializedName("product_name")
    val productName: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("original_price")
    val originalPrice: Double? = null,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("additional_images")
    val additionalImages: List<String>? = null,
    
    @SerializedName("condition")
    val condition: String = "good", // like_new, good, fair, poor
    
    @SerializedName("age_months")
    val ageMonths: Int? = null,
    
    @SerializedName("has_warranty")
    val hasWarranty: Boolean = false,
    
    @SerializedName("warranty_months")
    val warrantyMonths: Int? = null,
    
    @SerializedName("has_bill")
    val hasBill: Boolean = false,
    
    @SerializedName("reason_for_selling")
    val reasonForSelling: String? = null,
    
    @SerializedName("brand")
    val brand: String? = null,
    
    @SerializedName("model")
    val model: String? = null,
    
    @SerializedName("city")
    val city: String? = null,
    
    @SerializedName("pincode")
    val pincode: String? = null,
    
    @SerializedName("show_phone")
    val showPhone: Boolean = true,
    
    @SerializedName("accept_offers")
    val acceptOffers: Boolean = true,
    
    @SerializedName("status")
    val status: String = "active", // active, sold, expired, deleted
    
    @SerializedName("view_count")
    val viewCount: Int = 0,
    
    @SerializedName("inquiry_count")
    val inquiryCount: Int = 0,
    
    @SerializedName("category_name")
    val categoryName: String? = null,
    
    @SerializedName("category_name_mr")
    val categoryNameMr: String? = null,
    
    @SerializedName("seller_name")
    val sellerName: String? = null,
    
    @SerializedName("seller")
    val seller: OldProductSeller? = null,
    
    @SerializedName("delivery_by")
    val deliveryBy: Int = 3,
    
    @SerializedName("created_at")
    val createdAt: String? = null
) {
    fun getConditionLabel(): String {
        return when (condition) {
            "like_new" -> "Like New"
            "good" -> "Good"
            "fair" -> "Fair"
            "poor" -> "Poor"
            else -> condition.replaceFirstChar { it.uppercase() }
        }
    }
    
    fun getDiscount(): Int? {
        if (originalPrice != null && originalPrice > price) {
            return ((originalPrice - price) / originalPrice * 100).toInt()
        }
        return null
    }
}

@Stable
data class OldProductSeller(
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("avatar_url")
    val avatarUrl: String? = null
)

data class OldProductsResponse(
    @SerializedName("products")
    val products: List<OldProduct>,
    
    @SerializedName("pagination")
    val pagination: Pagination? = null
)
