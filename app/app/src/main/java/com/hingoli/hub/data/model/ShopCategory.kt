package com.hingoli.hub.data.model

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

/**
 * Shop category model for e-commerce section.
 * Used for NEW products (groceries, electronics, etc.)
 * OLD/used products continue to use the regular Category model.
 */
@Stable
data class ShopCategory(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("parent_id")
    val parentId: Int? = null,
    
    @SerializedName("level")
    val level: Int = 1,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("name_mr")
    val nameMr: String? = null,
    
    @SerializedName("slug")
    val slug: String,
    
    @SerializedName("icon")
    val icon: String? = null,
    
    @SerializedName("color")
    val color: String? = null,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("sort_order")
    val sortOrder: Int = 0,
    
    @SerializedName("product_count")
    val productCount: Int = 0,
    
    @SerializedName("subcategories")
    val subcategories: List<ShopCategory>? = null
) {
    /**
     * Get the localized name based on language preference.
     */
    fun getLocalizedName(isMarathi: Boolean): String {
        return if (isMarathi && !nameMr.isNullOrBlank()) nameMr else name
    }
    
    /**
     * Convert to Category for UI compatibility with existing dialogs.
     */
    fun toCategory(): Category {
        return Category(
            categoryId = id,
            parentId = parentId,
            name = name,
            nameMr = nameMr,
            slug = slug,
            listingType = "shop",
            iconUrl = null,
            imageUrl = imageUrl,
            description = null,
            listingCount = productCount,
            depth = level - 1
        )
    }
}
