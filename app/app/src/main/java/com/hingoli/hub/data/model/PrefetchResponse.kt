package com.hingoli.hub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response from /api/prefetch endpoint
 * Contains all app startup data in a single response
 */
data class PrefetchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PrefetchData?,
    @SerializedName("message") val message: String?
)

data class PrefetchData(
    @SerializedName("categories") val categories: PrefetchCategories,
    @SerializedName("listings") val listings: PrefetchListings,
    @SerializedName("shop_products") val shopProducts: List<ShopProduct>? = emptyList(),
    @SerializedName("old_products") val oldProducts: List<ShopProduct>? = emptyList(),
    @SerializedName("banners") val banners: PrefetchBanners,
    @SerializedName("cities") val cities: List<City>
)

data class PrefetchCategories(
    @SerializedName("services") val services: List<CategoryWithSubcategories>,
    @SerializedName("business") val business: List<CategoryWithSubcategories>,
    @SerializedName("selling") val selling: List<CategoryWithSubcategories> = emptyList(),
    @SerializedName("jobs") val jobs: List<CategoryWithSubcategories>,
    @SerializedName("old") val old: List<OldCategory>? = emptyList()
)

data class CategoryWithSubcategories(
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("parent_id") val parentId: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("name_mr") val nameMr: String?,
    @SerializedName("slug") val slug: String?,
    @SerializedName("listing_type") val listingType: String?,
    @SerializedName("depth") val depth: Int,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("listing_count") val listingCount: Int,
    @SerializedName("subcategories") val subcategories: List<Category>?
) {
    /**
     * Convert to regular Category (without subcategories)
     */
    fun toCategory(): Category = Category(
        categoryId = categoryId,
        parentId = parentId,
        name = name,
        nameMr = nameMr,
        slug = slug ?: "",
        listingType = listingType ?: "",
        depth = depth,
        iconUrl = iconUrl,
        imageUrl = imageUrl,
        description = description,
        listingCount = listingCount
    )
}

data class PrefetchListings(
    @SerializedName("services") val services: List<Listing>,
    @SerializedName("business") val business: List<Listing>,
    @SerializedName("selling") val selling: List<Listing> = emptyList(),
    @SerializedName("jobs") val jobs: List<Listing>
)

data class PrefetchBanners(
    @SerializedName("home_top") val homeTop: List<Banner> = emptyList(),
    @SerializedName("home_bottom") val homeBottom: List<Banner> = emptyList(),
    @SerializedName("services_top") val servicesTop: List<Banner> = emptyList(),
    @SerializedName("services_bottom") val servicesBottom: List<Banner> = emptyList(),
    @SerializedName("business_top") val businessTop: List<Banner> = emptyList(),
    @SerializedName("business_bottom") val businessBottom: List<Banner> = emptyList(),
    @SerializedName("selling_top") val sellingTop: List<Banner> = emptyList(),
    @SerializedName("selling_bottom") val sellingBottom: List<Banner> = emptyList(),
    @SerializedName("jobs_top") val jobsTop: List<Banner> = emptyList(),
    @SerializedName("jobs_bottom") val jobsBottom: List<Banner> = emptyList(),
    @SerializedName("listing_detail_bottom") val listingDetailBottom: List<Banner> = emptyList(),
    @SerializedName("category_bottom") val categoryBottom: List<Banner> = emptyList(),
    @SerializedName("search_bottom") val searchBottom: List<Banner> = emptyList()
) {
    /**
     * Get banners for a specific placement
     */
    fun getBannersForPlacement(placement: String): List<Banner> {
        return when (placement) {
            "home_top" -> homeTop
            "home_bottom" -> homeBottom
            "services_top" -> servicesTop
            "services_bottom" -> servicesBottom
            "business_top" -> businessTop
            "business_bottom" -> businessBottom
            "selling_top" -> sellingTop
            "selling_bottom" -> sellingBottom
            "jobs_top" -> jobsTop
            "jobs_bottom" -> jobsBottom
            "listing_detail_bottom" -> listingDetailBottom
            "category_bottom" -> categoryBottom
            "search_bottom" -> searchBottom
            else -> emptyList()
        }
    }
}
