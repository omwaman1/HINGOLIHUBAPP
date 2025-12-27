package com.hingoli.hub

import android.app.Application
import android.util.Log
import com.hingoli.hub.data.repository.SharedDataRepository
import com.hingoli.hub.util.ImageCacheManager
import com.hingoli.hub.util.ImagePreloader
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HingoliHubApp : Application() {
    
    @Inject
    lateinit var imagePreloader: ImagePreloader
    
    @Inject
    lateinit var imageCacheManager: ImageCacheManager
    
    @Inject
    lateinit var sharedDataRepository: SharedDataRepository
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        Log.d("HingoliHubApp", "🚀 App started, initiating background prefetch")
        
        // Start preloading images in background
        imagePreloader.startPreloading(applicationScope)
        
        // Prefetch all API data in background (shared cache)
        applicationScope.launch {
            sharedDataRepository.prefetchAllData()
        }
        
        // Check and refresh persistent image caches if needed
        applicationScope.launch {
            if (imageCacheManager.shouldRefreshBanners()) {
                imageCacheManager.cacheBanners()
            }
            if (imageCacheManager.shouldRefreshCategories()) {
                imageCacheManager.cacheAllCategoryImages()
            }
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        Log.d("HingoliHubApp", "🗑️ App terminating, clearing session cache")
        
        // Clear caches when app closes
        imageCacheManager.clearSessionCache()
        sharedDataRepository.clearCache()
        imagePreloader.stopPreloading()
    }
}

