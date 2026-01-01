package com.hingoli.hub.data.repository

import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.model.LikeResponse
import com.hingoli.hub.data.model.Listing
import com.hingoli.hub.data.model.PriceListItem
import com.hingoli.hub.data.model.ReelActionRequest
import com.hingoli.hub.data.model.ReelsResponse
import com.hingoli.hub.data.model.Review
import javax.inject.Inject
import javax.inject.Singleton

sealed class ListingResult {
    data class Success(val listings: List<Listing>) : ListingResult()
    data class Error(val message: String) : ListingResult()
}

sealed class ListingDetailResult {
    data class Success(val listing: Listing) : ListingDetailResult()
    data class Error(val message: String) : ListingDetailResult()
}

sealed class PriceListResult {
    data class Success(val items: List<PriceListItem>) : PriceListResult()
    data class Error(val message: String) : PriceListResult()
}

sealed class ReviewsResult {
    data class Success(val reviews: List<Review>) : ReviewsResult()
    data class Error(val message: String) : ReviewsResult()
}

@Singleton
class ListingRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    suspend fun getListings(
        listingType: String? = null,
        categoryId: Int? = null,
        subcategoryId: Int? = null,
        city: String? = null,
        search: String? = null,
        page: Int = 1
    ): ListingResult {
        return try {
            val response = apiService.getListings(
                listingType = listingType,
                categoryId = categoryId,
                subcategoryId = subcategoryId,
                city = city,
                search = search,
                page = page
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                val listings = response.body()?.data ?: emptyList()
                ListingResult.Success(listings)
            } else {
                ListingResult.Error(response.body()?.message ?: "Failed to load listings")
            }
        } catch (e: Exception) {
            ListingResult.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun searchListings(
        query: String,
        listingType: String? = null,
        city: String? = null
    ): ListingResult {
        return getListings(
            listingType = listingType,
            search = query,
            city = city,
            page = 1
        )
    }
    
    suspend fun getListingById(listingId: Long): ListingDetailResult {
        return try {
            val response = apiService.getListingById(listingId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val listing = response.body()?.data
                if (listing != null) {
                    ListingDetailResult.Success(listing)
                } else {
                    ListingDetailResult.Error("Listing not found")
                }
            } else {
                ListingDetailResult.Error(response.body()?.message ?: "Failed to load listing")
            }
        } catch (e: Exception) {
            ListingDetailResult.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun getPriceList(listingId: Long): PriceListResult {
        return try {
            val response = apiService.getListingPriceList(listingId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val items = response.body()?.data ?: emptyList()
                PriceListResult.Success(items)
            } else {
                PriceListResult.Error(response.body()?.message ?: "Failed to load price list")
            }
        } catch (e: Exception) {
            PriceListResult.Error(e.message ?: "Network error")
        }
    }
    
    suspend fun getReviews(listingId: Long, page: Int = 1): ReviewsResult {
        return try {
            val response = apiService.getListingReviews(listingId, page)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val reviews = response.body()?.data ?: emptyList()
                ReviewsResult.Success(reviews)
            } else {
                ReviewsResult.Error(response.body()?.message ?: "Failed to load reviews")
            }
        } catch (e: Exception) {
            ReviewsResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getMyListings(type: String? = null): Result<List<Listing>> {
        return try {
            val response = apiService.getMyListings(type)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load listings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteListing(listingId: Long): Result<Unit> {
        return try {
            val response = apiService.deleteListing(listingId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to delete listing"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getReels(page: Int = 1): ReelsResponse {
        return try {
            val response = apiService.getReels(page = page)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                ReelsResponse(
                    success = false,
                    message = response.body()?.message ?: "Failed to load reels",
                    data = null
                )
            }
        } catch (e: Exception) {
            ReelsResponse(
                success = false,
                message = e.message ?: "Network error",
                data = null
            )
        }
    }
    
    suspend fun likeReel(request: ReelActionRequest): LikeResponse {
        return try {
            val response = apiService.likeReel(request)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                LikeResponse(success = false, data = null)
            }
        } catch (e: Exception) {
            LikeResponse(success = false, data = null)
        }
    }
    
    suspend fun markReelWatched(request: ReelActionRequest) {
        try {
            apiService.markReelWatched(request)
        } catch (e: Exception) {
            // Silently ignore
        }
    }
}
