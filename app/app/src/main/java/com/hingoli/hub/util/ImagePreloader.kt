package com.hingoli.hub.util

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.hingoli.hub.data.repository.SharedDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preloads images for all app screens in background.
 * Uses SharedDataRepository to get cached data instead of making direct API calls.
 * Runs after prefetch completes to preload images into memory/disk cache.
 */
@Singleton
class ImagePreloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedDataRepository: SharedDataRepository,
    private val imageLoader: ImageLoader
) {
    private val TAG = "ImagePreloader"
    private var preloadJob: Job? = null
    
    /**
     * Start preloading images for all screen data in background.
     * Waits for SharedDataRepository prefetch to complete, then preloads images.
     */
    fun startPreloading(scope: CoroutineScope) {
        if (preloadJob?.isActive == true) {
            Log.d(TAG, "Preload already in progress, skipping")
            return
        }
        
        preloadJob = scope.launch(Dispatchers.IO) {
            Log.d(TAG, "🚀 Starting background image preload...")
            
            try {
                // Wait a short delay for prefetch to start
                delay(500)
                
                // Preload images from cached data (waits for prefetch to complete)
                preloadBanners()
                preloadListingsImages("services")
                preloadListingsImages("selling")
                preloadListingsImages("business")
                preloadListingsImages("jobs")
                preloadCategoryImages("services")
                preloadCategoryImages("business")
                preloadCategoryImages("selling")
                preloadCategoryImages("jobs")
                
                Log.d(TAG, "✅ Background image preload completed")
                
            } catch (e: CancellationException) {
                Log.d(TAG, "Preload cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Preload error: ${e.message}")
            }
        }
    }
    
    /**
     * Stop any ongoing preload (e.g., when user logs in)
     */
    fun stopPreloading() {
        preloadJob?.cancel()
        preloadJob = null
    }
    
    private suspend fun preloadBanners() {
        try {
            // Use cached banners from SharedDataRepository
            val banners = sharedDataRepository.getBanners()
            banners.forEach { banner ->
                banner.imageUrl?.let { url ->
                    preloadImage(url)
                }
            }
            Log.d(TAG, "📦 Preloaded ${banners.size} banner images from cache")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to preload banners: ${e.message}")
        }
    }
    
    private suspend fun preloadListingsImages(type: String) {
        try {
            // Use cached listings from SharedDataRepository
            val listings = sharedDataRepository.getListings(type)
            listings.forEach { listing ->
                listing.mainImageUrl?.let { url ->
                    preloadImage(url)
                }
            }
            Log.d(TAG, "📦 Preloaded ${listings.size} $type listing images from cache")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to preload $type listings: ${e.message}")
        }
    }
    
    private suspend fun preloadCategoryImages(type: String) {
        try {
            // Use cached categories from SharedDataRepository
            // This includes both parent categories and subcategories
            val categories = sharedDataRepository.getCategories(type)
            
            categories.forEach { category ->
                category.iconUrl?.let { preloadImage(it) }
                category.imageUrl?.let { preloadImage(it) }
            }
            
            Log.d(TAG, "📦 Preloaded ${categories.size} $type category images from cache")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to preload $type categories: ${e.message}")
        }
    }
    
    private fun preloadImage(url: String) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
        
        imageLoader.enqueue(request)
    }
}
