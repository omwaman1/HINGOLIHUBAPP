package com.hingoli.hub.data.model

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

@Stable
data class Category(
    @SerializedName("category_id")
    val categoryId: Int,
    
    @SerializedName("parent_id")
    val parentId: Int? = null,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("name_mr")
    val nameMr: String? = null,
    
    @SerializedName("slug")
    val slug: String,
    
    @SerializedName("listing_type")
    val listingType: String, // "services" or "business"
    
    @SerializedName("icon_url")
    val iconUrl: String? = null,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("listing_count")
    val listingCount: Int = 0,
    
    @SerializedName("depth")
    val depth: Int = 0
) {
    /**
     * Get the localized name based on language preference.
     * Returns Marathi name if available and language is Marathi, otherwise English.
     */
    fun getLocalizedName(isMarathi: Boolean): String {
        return if (isMarathi && !nameMr.isNullOrBlank()) nameMr else name
    }
}

