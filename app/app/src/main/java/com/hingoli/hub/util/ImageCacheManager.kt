package com.hingoli.hub.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.hingoli.hub.data.repository.SharedDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

private val Context.imageCacheDataStore: DataStore<Preferences> by preferencesDataStore(name = "image_cache_prefs")

/**
 * Manages local image caching with different refresh intervals:
 * - Banners: 1 day refresh
 * - Categories: 7 days refresh
 * - Listings: Session only (cleared on app terminate)
 * 
 * Uses SharedDataRepository for category data to avoid duplicate API calls.
 */
@Singleton
class ImageCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedDataRepository: SharedDataRepository,
    private val imageLoader: ImageLoader
) {
    private val TAG = "ImageCacheManager"
    
    companion object {
        private val LAST_BANNER_REFRESH = longPreferencesKey("last_banner_refresh")
        private val LAST_CATEGORY_REFRESH = longPreferencesKey("last_category_refresh")
        
        private const val BANNER_CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 1 day
        private const val CATEGORY_CACHE_DURATION_MS = 7 * 24 * 60 * 60 * 1000L // 7 days
    }
    
    private val dataStore = context.imageCacheDataStore
    
    // Cache directories
    private val bannerCacheDir: File
        get() = File(context.cacheDir, "banners").also { it.mkdirs() }
    
    private val categoryCacheDir: File
        get() = File(context.cacheDir, "categories").also { it.mkdirs() }
    
    private val listingCacheDir: File
        get() = File(context.cacheDir, "listings").also { it.mkdirs() }
    
    // ==================== Banner Cache (1 day) ====================
    
    suspend fun shouldRefreshBanners(): Boolean {
        val lastRefresh = dataStore.data.map { preferences ->
            preferences[LAST_BANNER_REFRESH] ?: 0L
        }.first()
        
        val now = System.currentTimeMillis()
        return (now - lastRefresh) > BANNER_CACHE_DURATION_MS
    }
    
    suspend fun cacheBanners() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Refreshing banner cache...")
                
                // Use SharedDataRepository instead of direct API call
                val banners = sharedDataRepository.getBanners()
                banners.forEach { banner ->
                    banner.imageUrl?.let { url ->
                        downloadAndCacheImage(url, bannerCacheDir, "banner_${banner.bannerId}")
                    }
                }
                
                // Update refresh timestamp
                dataStore.edit { preferences ->
                    preferences[LAST_BANNER_REFRESH] = System.currentTimeMillis()
                }
                
                Log.d(TAG, "✅ Cached ${banners.size} banners")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cache banners: ${e.message}")
            }
        }
    }
    
    // ==================== Category Cache (7 days) ====================
    
    suspend fun shouldRefreshCategories(): Boolean {
        val lastRefresh = dataStore.data.map { preferences ->
            preferences[LAST_CATEGORY_REFRESH] ?: 0L
        }.first()
        
        val now = System.currentTimeMillis()
        return (now - lastRefresh) > CATEGORY_CACHE_DURATION_MS
    }
    
    suspend fun cacheAllCategoryImages() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Refreshing category cache...")
                
                var totalCached = 0
                
                // Use SharedDataRepository for all category types
                // This uses cached data that was already prefetched
                val allTypes = listOf("services", "business", "selling", "jobs")
                
                for (type in allTypes) {
                    val categories = sharedDataRepository.getCategories(type)
                    
                    categories.forEach { category ->
                        category.iconUrl?.let { 
                            downloadAndCacheImage(it, categoryCacheDir, "cat_${category.categoryId}_icon")
                        }
                        category.imageUrl?.let { 
                            downloadAndCacheImage(it, categoryCacheDir, "cat_${category.categoryId}_image")
                        }
                        totalCached++
                    }
                }
                
                // Update refresh timestamp
                dataStore.edit { preferences ->
                    preferences[LAST_CATEGORY_REFRESH] = System.currentTimeMillis()
                }
                
                Log.d(TAG, "✅ Cached $totalCached category images")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cache categories: ${e.message}")
            }
        }
    }
    
    // ==================== Listing Cache (Session only) ====================
    
    /**
     * Cache a listing image. Called during app usage.
     */
    fun cacheListingImage(url: String, listingId: Long) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
        
        imageLoader.enqueue(request)
    }
    
    /**
     * Clear all listing image cache. Call on app terminate.
     */
    fun clearListingCache() {
        try {
            listingCacheDir.listFiles()?.forEach { it.delete() }
            Log.d(TAG, "🗑️ Cleared listing cache")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear listing cache: ${e.message}")
        }
    }
    
    /**
     * Clear Coil's memory and disk cache for listings only.
     * Since Coil shares cache, we clear the temp listing files we explicitly saved.
     */
    fun clearSessionCache() {
        clearListingCache()
    }
    
    // ==================== Helpers ====================
    
    private fun downloadAndCacheImage(url: String, cacheDir: File, filename: String) {
        try {
            val extension = url.substringAfterLast('.', "jpg").take(4)
            val file = File(cacheDir, "$filename.$extension")
            
            if (!file.exists()) {
                URL(url).openStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        } catch (e: Exception) {
            // Silent fail - image will load from network
        }
    }
    
    /**
     * Get cached image path if exists
     */
    fun getCachedBannerPath(bannerId: Long): File? {
        val file = bannerCacheDir.listFiles()?.find { 
            it.name.startsWith("banner_$bannerId") 
        }
        return if (file?.exists() == true) file else null
    }
    
    fun getCachedCategoryPath(categoryId: Int, isIcon: Boolean = true): File? {
        val suffix = if (isIcon) "icon" else "image"
        val file = categoryCacheDir.listFiles()?.find { 
            it.name.startsWith("cat_${categoryId}_$suffix") 
        }
        return if (file?.exists() == true) file else null
    }
}
