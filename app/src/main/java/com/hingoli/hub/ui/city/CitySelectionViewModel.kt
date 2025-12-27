package com.hingoli.hub.ui.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.model.City
import com.hingoli.hub.data.repository.CityRepository
import com.hingoli.hub.data.repository.CityResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CityUiState(
    val cities: List<City> = emptyList(),
    val selectedCity: City? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class CitySelectionViewModel @Inject constructor(
    private val cityRepository: CityRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CityUiState())
    val uiState: StateFlow<CityUiState> = _uiState.asStateFlow()
    
    init {
        loadCities()
        
        // Observe selected city changes from repository
        viewModelScope.launch {
            cityRepository.selectedCityFlow.collect { city ->
                _uiState.value = _uiState.value.copy(selectedCity = city)
            }
        }
    }
    
    fun loadCities() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = cityRepository.loadCities()) {
                is CityResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cities = result.cities
                    )
                }
                is CityResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    fun selectCity(city: City) {
        viewModelScope.launch {
            cityRepository.selectCity(city)
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun getFilteredCities(): List<City> {
        val query = _uiState.value.searchQuery.lowercase()
        if (query.isEmpty()) {
            return _uiState.value.cities
        }
        return _uiState.value.cities.filter { 
            it.name.lowercase().contains(query) || 
            (it.nameMr?.lowercase()?.contains(query) == true)
        }
    }
    
    fun getSelectedCityName(): String {
        return _uiState.value.selectedCity?.name ?: "Hingoli"
    }
}
