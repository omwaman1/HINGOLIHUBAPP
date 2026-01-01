package com.hingoli.hub.data.model

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

@Stable
data class City(
    @SerializedName("city_id")
    val cityId: Int,
    
    @SerializedName("state_id")
    val stateId: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("name_mr")
    val nameMr: String? = null,
    
    @SerializedName("slug")
    val slug: String,
    
    @SerializedName("is_popular")
    val isPopular: Boolean = false,
    
    @SerializedName("listing_count")
    val listingCount: Int = 0
) {
    /**
     * Get the localized name based on language preference.
     * Returns Marathi name if available and language is Marathi, otherwise English.
     */
    fun getLocalizedName(isMarathi: Boolean): String {
        return if (isMarathi && !nameMr.isNullOrBlank()) nameMr else name
    }
}

