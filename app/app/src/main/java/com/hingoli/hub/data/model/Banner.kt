package com.hingoli.hub.data.model

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

/**
 * Banner model for promotional banners
 * Fetched from: GET /banners?placement=home_top
 */
@Stable
data class Banner(
    @SerializedName("banner_id") val bannerId: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("link_url") val linkUrl: String?,
    @SerializedName("link_type") val linkType: String?, // listing, category, external, screen
    @SerializedName("link_id") val linkId: Long?,
    @SerializedName("placement") val placement: String,
    @SerializedName("sort_order") val sortOrder: Int
)
