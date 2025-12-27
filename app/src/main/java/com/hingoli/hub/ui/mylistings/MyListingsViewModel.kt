package com.hingoli.hub.ui.mylistings

import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.settings.SettingsManager
import com.hingoli.hub.data.model.Listing
import com.hingoli.hub.data.repository.ListingRepository
import com.hingoli.hub.data.repository.SharedDataRepository
import com.hingoli.hub.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyListingsUiState(
    val isLoading: Boolean = true,
    val listings: List<Listing> = emptyList(),
    val error: String? = null,
    val selectedFilter: String = "all",
    val isMarathi: Boolean = true
)

@HiltViewModel
class MyListingsViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val sharedDataRepository: SharedDataRepository,
    settingsManager: SettingsManager
) : BaseViewModel(settingsManager) {

    private val _uiState = MutableStateFlow(MyListingsUiState())
    val uiState: StateFlow<MyListingsUiState> = _uiState.asStateFlow()

    init {
        loadMyListings()
        // Sync language from BaseViewModel
        viewModelScope.launch {
            isMarathi.collect { value ->
                _uiState.value = _uiState.value.copy(isMarathi = value)
            }
        }
    }

    fun setFilter(filter: String) {
        if (_uiState.value.selectedFilter != filter) {
            _uiState.value = _uiState.value.copy(selectedFilter = filter)
            loadMyListings()
        }
    }

    fun loadMyListings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val typeFilter = if (_uiState.value.selectedFilter == "all") null else _uiState.value.selectedFilter
                val result = listingRepository.getMyListings(typeFilter)
                result.fold(
                    onSuccess = { listings ->
                        // Deduplicate by listingId to prevent LazyColumn key crash
                        val uniqueListings = listings.distinctBy { it.listingId }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            listings = uniqueListings
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load listings"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load listings"
                )
            }
        }
    }

    fun deleteListing(listingId: Long, listingType: String) {
        viewModelScope.launch {
            try {
                // Route to correct endpoint based on listing type
                val result = if (listingType == "selling") {
                    // Products use DELETE /products/{id}
                    sharedDataRepository.deleteProduct(listingId)
                } else {
                    // Regular listings use DELETE /listings/{id}
                    listingRepository.deleteListing(listingId)
                }
                result.fold(
                    onSuccess = {
                        // Remove from local list
                        _uiState.value = _uiState.value.copy(
                            listings = _uiState.value.listings.filter { it.listingId != listingId }
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            error = e.message ?: "Failed to delete"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete"
                )
            }
        }
    }
}
