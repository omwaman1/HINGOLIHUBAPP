package com.hingoli.hub.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.model.Banner
import com.hingoli.hub.data.model.Category
import com.hingoli.hub.data.model.City
import com.hingoli.hub.data.model.Listing
import com.hingoli.hub.data.model.PrefetchBanners
import com.hingoli.hub.data.model.ShopProduct
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

private val Context.prefetchDataStore: DataStore<Preferences> by preferencesDataStore(name = "prefetch_cache")

/**
 * Shared repository that caches ALL API data from a single /prefetch endpoint.
 * 
 * Features:
 * - Single API call on startup fetches everything
 * - Categories with embedded subcategories (no separate calls needed)
 * - All banner placements cached
 * - 24-hour banner cache with DataStore persistence
 * - Listings by type
 * - Cities
 */
@Singleton
class SharedDataRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService
) {
    private val TAG = "SharedDataRepository"
    private val gson = Gson()
    
    companion object {
        private val LAST_BANNERS_FETCH = longPreferencesKey("last_banners_fetch")
        private val CACHED_BANNERS_JSON = stringPreferencesKey("cached_banners_json")
        private const val BANNER_CACHE_DURATION_MS = 0L // No caching - always fetch fresh banners
    }
    
    private val dataStore = context.prefetchDataStore
    
    // ==================== Synchronization ====================
    private val prefetchStarted = AtomicBoolean(false)
    private val prefetchCompleted = AtomicBoolean(false)
    
    // ==================== Cache Storage ====================
    
    // Banners cache (by placement)
    private val _bannersByPlacement = MutableStateFlow<Map<String, List<Banner>>>(emptyMap())
    val bannersByPlacement: StateFlow<Map<String, List<Banner>>> = _bannersByPlacement
    
    // For backward compatibility
    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners
    
    // Category caches by type (includes subcategories)
    private val _servicesCategories = MutableStateFlow<List<Category>>(emptyList())
    val servicesCategories: StateFlow<List<Category>> = _servicesCategories
    
    private val _businessCategories = MutableStateFlow<List<Category>>(emptyList())
    val businessCategories: StateFlow<List<Category>> = _businessCategories
    
    private val _sellingCategories = MutableStateFlow<List<Category>>(emptyList())
    val sellingCategories: StateFlow<List<Category>> = _sellingCategories
    
    private val _jobsCategories = MutableStateFlow<List<Category>>(emptyList())
    val jobsCategories: StateFlow<List<Category>> = _jobsCategories
    
    // Subcategories indexed by parent ID for quick lookup
    private val _subcategoriesByParent = MutableStateFlow<Map<Int, List<Category>>>(emptyMap())
    val subcategoriesByParent: StateFlow<Map<Int, List<Category>>> = _subcategoriesByParent
    
    // Listings cache by type
    private val _servicesListings = MutableStateFlow<List<Listing>>(emptyList())
    val servicesListings: StateFlow<List<Listing>> = _servicesListings
    
    private val _sellingListings = MutableStateFlow<List<Listing>>(emptyList())
    val sellingListings: StateFlow<List<Listing>> = _sellingListings
    
    private val _businessListings = MutableStateFlow<List<Listing>>(emptyList())
    val businessListings: StateFlow<List<Listing>> = _businessListings
    
    private val _jobsListings = MutableStateFlow<List<Listing>>(emptyList())
    val jobsListings: StateFlow<List<Listing>> = _jobsListings
    
    // Shop products cache (for selling screen)
    private val _shopProducts = MutableStateFlow<List<ShopProduct>>(emptyList())
    val shopProducts: StateFlow<List<ShopProduct>> = _shopProducts
    
    // Old products cache (for Buy/Sell Old section - condition='old')
    private val _oldProducts = MutableStateFlow<List<ShopProduct>>(emptyList())
    val oldProducts: StateFlow<List<ShopProduct>> = _oldProducts
    
    // Cities cache
    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private var lastFetchTime = 0L
    private val CACHE_VALIDITY_MS = 5 * 60 * 1000L // 5 minutes
    
    /**
     * Check if cache is valid
     */
    fun isCacheValid(): Boolean {
        return prefetchCompleted.get() && 
               System.currentTimeMillis() - lastFetchTime < CACHE_VALIDITY_MS
    }
    
    /**
     * Prefetch ALL data with a single API call.
     * Uses AtomicBoolean to ensure only one prefetch runs.
     */
    suspend fun prefetchAllData(city: String? = null) {
        // Quick check - if already started by another caller, just wait
        if (!prefetchStarted.compareAndSet(false, true)) {
            Log.d(TAG, "⏳ Prefetch already started, waiting for completion...")
            while (!prefetchCompleted.get()) {
                kotlinx.coroutines.delay(50)
            }
            return
        }
        
        _isLoading.value = true
        Log.d(TAG, "🚀 Prefetching ALL data with single API call...")
        
        try {
            // Check if banners are cached (24-hour cache)
            val bannersFromCache = loadBannersFromCache()
            
            // Make single API call to get everything
            val response = apiService.prefetch(city)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    // Process categories with embedded subcategories
                    processCategoriesWithSubcats(data.categories.services, "services") { _servicesCategories.value = it }
                    processCategoriesWithSubcats(data.categories.business, "business") { _businessCategories.value = it }
                    processCategoriesWithSubcats(data.categories.selling, "selling") { _sellingCategories.value = it }
                    processCategoriesWithSubcats(data.categories.jobs, "jobs") { _jobsCategories.value = it }
                    
                    // Store listings
                    _servicesListings.value = data.listings.services
                    _businessListings.value = data.listings.business
                    _sellingListings.value = data.listings.selling
                    _jobsListings.value = data.listings.jobs
                    
                    // Store banners (use cached if within 24 hours)
                    if (bannersFromCache != null) {
                        Log.d(TAG, "📦 Using 24-hour cached banners")
                        _bannersByPlacement.value = bannersFromCache
                        _banners.value = bannersFromCache["home_top"] ?: emptyList()
                    } else {
                        Log.d(TAG, "🌐 Caching new banners for 24 hours")
                        val bannersMap = mapOf(
                            "home_top" to data.banners.homeTop,
                            "home_bottom" to data.banners.homeBottom,
                            "services_top" to data.banners.servicesTop,
                            "services_bottom" to data.banners.servicesBottom,
                            "business_top" to data.banners.businessTop,
                            "business_bottom" to data.banners.businessBottom,
                            "selling_top" to data.banners.sellingTop,
                            "selling_bottom" to data.banners.sellingBottom,
                            "jobs_top" to data.banners.jobsTop,
                            "jobs_bottom" to data.banners.jobsBottom,
                            "listing_detail_bottom" to data.banners.listingDetailBottom,
                            "category_bottom" to data.banners.categoryBottom,
                            "search_bottom" to data.banners.searchBottom
                        )
                        _bannersByPlacement.value = bannersMap
                        _banners.value = data.banners.homeTop
                        saveBannersToCache(bannersMap)
                    }
                    
                    // Store shop products (handle null from API)
                    _shopProducts.value = data.shopProducts ?: emptyList()
                    
                    // Store old products (condition='old' for Buy/Sell Old section)
                    _oldProducts.value = data.oldProducts ?: emptyList()
                    
                    // Store cities
                    _cities.value = data.cities
                    
                    lastFetchTime = System.currentTimeMillis()
                    Log.d(TAG, "✅ Prefetch complete - loaded ${data.cities.size} cities, " +
                            "${_shopProducts.value.size} shop products, " +
                            "${_oldProducts.value.size} old products, " +
                            "${_servicesCategories.value.size} service cats, " +
                            "${_subcategoriesByParent.value.size} parents with subcats")
                }
            } else {
                Log.e(TAG, "Prefetch failed: ${response.message()}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Prefetch error: ${e.message}", e)
        } finally {
            _isLoading.value = false
            prefetchCompleted.set(true)
        }
    }
    
    /**
     * Process categories with embedded subcategories
     */
    private fun processCategoriesWithSubcats(
        categoriesWithSubs: List<com.hingoli.hub.data.model.CategoryWithSubcategories>,
        type: String,
        setter: (List<Category>) -> Unit
    ) {
        val allCategories = mutableListOf<Category>()
        val subcatsMap = _subcategoriesByParent.value.toMutableMap()
        
        categoriesWithSubs.forEach { catWithSubs ->
            // Add parent category
            val parent = catWithSubs.toCategory()
            allCategories.add(parent)
            
            // Add subcategories and index by parent
            catWithSubs.subcategories?.let { subs ->
                allCategories.addAll(subs)
                subcatsMap[catWithSubs.categoryId] = subs
            }
        }
        
        setter(allCategories)
        _subcategoriesByParent.value = subcatsMap
    }
    
    // ==================== Banner 24-hour Cache ====================
    
    private suspend fun loadBannersFromCache(): Map<String, List<Banner>>? {
        try {
            val prefs = dataStore.data.first()
            val lastFetch = prefs[LAST_BANNERS_FETCH] ?: 0L
            val now = System.currentTimeMillis()
            
            if (now - lastFetch < BANNER_CACHE_DURATION_MS) {
                val json = prefs[CACHED_BANNERS_JSON] ?: return null
                val cached = gson.fromJson(json, CachedBanners::class.java)
                return cached.banners
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading banners from cache: ${e.message}")
        }
        return null
    }
    
    private suspend fun saveBannersToCache(banners: Map<String, List<Banner>>) {
        try {
            val json = gson.toJson(CachedBanners(banners))
            dataStore.edit { prefs ->
                prefs[LAST_BANNERS_FETCH] = System.currentTimeMillis()
                prefs[CACHED_BANNERS_JSON] = json
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving banners to cache: ${e.message}")
        }
    }
    
    private data class CachedBanners(val banners: Map<String, List<Banner>>)
    
    // ==================== Force Refresh ====================
    
    suspend fun refreshAll(city: String? = null) {
        // Reset flags
        prefetchStarted.set(false)
        prefetchCompleted.set(false)
        lastFetchTime = 0L
        
        // Clear banner cache to force refresh
        dataStore.edit { prefs ->
            prefs[LAST_BANNERS_FETCH] = 0L
        }
        
        prefetchAllData(city)
    }
    
    // ==================== Public Accessors ====================
    
    /**
     * Get cached listings by type
     */
    suspend fun getListings(type: String, city: String? = null, forceRefresh: Boolean = false): List<Listing> {
        if (!prefetchCompleted.get()) {
            Log.d(TAG, "⏳ Waiting for prefetch to complete...")
            while (!prefetchCompleted.get()) {
                kotlinx.coroutines.delay(50)
            }
        }
        
        return when (type) {
            "services" -> _servicesListings.value
            "selling" -> _sellingListings.value
            "business" -> _businessListings.value
            "jobs" -> _jobsListings.value
            else -> emptyList()
        }
    }
    
    /**
     * Get cached categories by type (includes parent and subcategories)
     */
    suspend fun getCategories(type: String): List<Category> {
        if (!prefetchCompleted.get()) {
            Log.d(TAG, "⏳ Waiting for prefetch to complete...")
            while (!prefetchCompleted.get()) {
                kotlinx.coroutines.delay(50)
            }
        }
        
        return when (type) {
            "services" -> _servicesCategories.value
            "business" -> _businessCategories.value
            "selling" -> _sellingCategories.value
            "jobs" -> _jobsCategories.value
            else -> emptyList()
        }
    }
    
    /**
     * Get subcategories for a parent category ID
     */
    fun getSubcategoriesForParent(parentId: Int): List<Category> {
        return _subcategoriesByParent.value[parentId] ?: emptyList()
    }
    
    /**
     * Get cached banners (home_top placement)
     */
    suspend fun getBanners(): List<Banner> {
        if (!prefetchCompleted.get()) {
            while (!prefetchCompleted.get()) {
                kotlinx.coroutines.delay(50)
            }
        }
        return _banners.value
    }
    
    /**
     * Get banners for a specific placement
     */
    suspend fun getBannersForPlacement(placement: String): List<Banner> {
        if (!prefetchCompleted.get()) {
            while (!prefetchCompleted.get()) {
                kotlinx.coroutines.delay(50)
            }
        }
        return _bannersByPlacement.value[placement] ?: emptyList()
    }
    
    /**
     * Get cached cities
     */
    suspend fun getCities(): List<City> {
        if (!prefetchCompleted.get()) {
            while (!prefetchCompleted.get()) {
                kotlinx.coroutines.delay(50)
            }
        }
        return _cities.value
    }
    
    /**
     * Get cached shop products (for selling feed)
     */
    suspend fun getShopProducts(): List<ShopProduct> {
        if (!prefetchCompleted.get()) {
            while (!prefetchCompleted.get()) {
                kotlinx.coroutines.delay(50)
            }
        }
        return _shopProducts.value
    }
    
    /**
     * Get cached old products (for Buy/Sell Old section)
     */
    suspend fun getOldProducts(): List<ShopProduct> {
        if (!prefetchCompleted.get()) {
            while (!prefetchCompleted.get()) {
                kotlinx.coroutines.delay(50)
            }
        }
        return _oldProducts.value
    }
    
    /**
     * Clear all cache
     */
    fun clearCache() {
        prefetchStarted.set(false)
        prefetchCompleted.set(false)
        lastFetchTime = 0L
        
        _banners.value = emptyList()
        _bannersByPlacement.value = emptyMap()
        _servicesCategories.value = emptyList()
        _businessCategories.value = emptyList()
        _sellingCategories.value = emptyList()
        _jobsCategories.value = emptyList()
        _subcategoriesByParent.value = emptyMap()
        _servicesListings.value = emptyList()
        _businessListings.value = emptyList()
        _sellingListings.value = emptyList()
        _jobsListings.value = emptyList()
        _shopProducts.value = emptyList()
        _oldProducts.value = emptyList()
        _cities.value = emptyList()
        
        Log.d(TAG, "🗑️ Cache cleared")
    }
    
    /**
     * Delete a product (selling item) from shop_products
     * Calls DELETE /products/{id}
     */
    suspend fun deleteProduct(productId: Long): Result<Unit> {
        return try {
            val response = apiService.deleteBusinessProduct(productId)
            if (response.isSuccessful && response.body()?.success == true) {
                // Remove from local cache
                _shopProducts.value = _shopProducts.value.filter { it.productId != productId }
                _oldProducts.value = _oldProducts.value.filter { it.productId != productId }
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to delete product"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
