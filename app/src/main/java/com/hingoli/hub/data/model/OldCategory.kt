package com.hingoli.hub.data.model

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

/**
 * Old category model for used/second-hand items section.
 * Similar to ShopCategory but for OLD products (OLX/Quikr style).
 */
@Stable
data class OldCategory(
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
    val subcategories: List<OldCategory>? = null
) {
    fun getLocalizedName(isMarathi: Boolean): String {
        return if (isMarathi && !nameMr.isNullOrBlank()) nameMr else name
    }
}
