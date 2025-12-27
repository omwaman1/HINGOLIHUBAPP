package com.hingoli.hub.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hingoli.hub.data.model.City
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.cityDataStore: DataStore<Preferences> by preferencesDataStore(name = "city_preferences")

sealed class CityResult {
    data class Success(val cities: List<City>) : CityResult()
    data class Error(val message: String) : CityResult()
}

/**
 * Repository for city operations.
 * Uses SharedDataRepository for city data and persists selected city to DataStore.
 * 
 * Note: SharedDataRepository is injected lazily via Hilt Provider to avoid circular dependency.
 */
@Singleton
class CityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedDataRepositoryProvider: dagger.Lazy<SharedDataRepository>
) {
    private val TAG = "CityRepository"
    
    private val sharedDataRepository: SharedDataRepository
        get() = sharedDataRepositoryProvider.get()
    
    companion object {
        private val KEY_SELECTED_CITY_ID = intPreferencesKey("selected_city_id")
        private val KEY_SELECTED_CITY_NAME = stringPreferencesKey("selected_city_name")
        private const val DEFAULT_CITY_NAME = "Hingoli"
        private const val DEFAULT_CITY_ID = 1
    }
    
    private val _selectedCity = MutableStateFlow<City?>(null)
    val selectedCityFlow: StateFlow<City?> = _selectedCity.asStateFlow()
    
    // Get stored city name (for quick access without loading full city)
    val selectedCityNameFlow: Flow<String> = context.cityDataStore.data.map { preferences ->
        preferences[KEY_SELECTED_CITY_NAME] ?: DEFAULT_CITY_NAME
    }
    
    /**
     * Load cities from SharedDataRepository cache.
     * Uses deduplication - won't make API call if already cached.
     */
    suspend fun loadCities(): CityResult {
        return try {
            // Use SharedDataRepository - it handles caching and deduplication
            val cities = sharedDataRepository.getCities()
            
            // If no city is selected yet, set the stored city or default
            if (_selectedCity.value == null && cities.isNotEmpty()) {
                loadSavedCity(cities)
            }
            
            Log.d(TAG, "📦 Returning ${cities.size} cities from SharedDataRepository")
            CityResult.Success(cities)
        } catch (e: Exception) {
            CityResult.Error(e.message ?: "Network error")
        }
    }
    
    /**
     * Get cached cities synchronously.
     * Returns empty list if not loaded yet.
     */
    fun getCitiesCached(): List<City> {
        return sharedDataRepository.cities.value
    }
    
    private suspend fun loadSavedCity(cities: List<City>) {
        val preferences = context.cityDataStore.data.first()
        val savedCityId = preferences[KEY_SELECTED_CITY_ID] ?: DEFAULT_CITY_ID
        
        // Find the saved city in the loaded cities or use default
        val city = cities.find { it.cityId == savedCityId }
            ?: cities.find { it.cityId == DEFAULT_CITY_ID }
            ?: cities.firstOrNull()
        
        _selectedCity.value = city
    }
    
    suspend fun selectCity(city: City) {
        _selectedCity.value = city
        
        // Persist selection
        context.cityDataStore.edit { preferences ->
            preferences[KEY_SELECTED_CITY_ID] = city.cityId
            preferences[KEY_SELECTED_CITY_NAME] = city.name
        }
    }
    
    fun getSelectedCityName(): String {
        return _selectedCity.value?.name ?: DEFAULT_CITY_NAME
    }
    
    /**
     * Clear cache (for logout)
     */
    fun clearCache() {
        _selectedCity.value = null
    }
}
