package com.hingoli.hub.data.repository

import android.util.Log
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.model.Category
import javax.inject.Inject
import javax.inject.Singleton

sealed class CategoryResult {
    data class Success(val categories: List<Category>) : CategoryResult()
    data class Error(val message: String) : CategoryResult()
}

/**
 * Repository for category operations.
 * Uses SharedDataRepository cache exclusively - no direct API calls.
 * All data is prefetched at app startup.
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val apiService: ApiService,
    private val sharedDataRepository: SharedDataRepository
) {
    private val TAG = "CategoryRepository"
    
    /**
     * Get categories for a listing type.
     * Returns from shared cache only - waits for prefetch if needed.
     */
    suspend fun getCategories(listingType: String): CategoryResult {
        return try {
            // Use shared cache - this waits for prefetch if not complete
            val cached = sharedDataRepository.getCategories(listingType)
            // Filter to only parent categories
            val parents = cached.filter { it.parentId == null }
            Log.d(TAG, "📦 Returning ${parents.size} cached $listingType parent categories")
            CategoryResult.Success(parents)
        } catch (e: Exception) {
            CategoryResult.Error(e.message ?: "Network error")
        }
    }
    
    /**
     * Get subcategories for a parent category.
     * Returns from shared cache only - no API fallback.
     */
    suspend fun getSubcategories(categoryId: Int): CategoryResult {
        return try {
            // Wait for prefetch to complete first
            sharedDataRepository.getCategories("services") // This ensures prefetch is done
            
            // Get from cache (indexed by parent ID)
            val cached = sharedDataRepository.getSubcategoriesForParent(categoryId)
            Log.d(TAG, "📦 Returning ${cached.size} cached subcategories for parent $categoryId")
            CategoryResult.Success(cached)
        } catch (e: Exception) {
            CategoryResult.Error(e.message ?: "Network error")
        }
    }
    
    /**
     * Get a specific category by ID.
     * Searches through all cached categories.
     */
    suspend fun getCategoryById(categoryId: Int): CategoryResult {
        return try {
            // Search in all cached categories
            val allCached = listOf(
                sharedDataRepository.getCategories("services"),
                sharedDataRepository.getCategories("business"),
                sharedDataRepository.getCategories("selling"),
                sharedDataRepository.getCategories("jobs")
            ).flatten()
            
            val found = allCached.find { it.categoryId == categoryId }
            if (found != null) {
                Log.d(TAG, "📦 Found category $categoryId in cache")
                return CategoryResult.Success(listOf(found))
            }
            
            // Not found in cache - this should rarely happen if prefetch worked
            Log.w(TAG, "⚠️ Category $categoryId not found in cache")
            CategoryResult.Error("Category not found")
        } catch (e: Exception) {
            CategoryResult.Error(e.message ?: "Network error")
        }
    }
}
