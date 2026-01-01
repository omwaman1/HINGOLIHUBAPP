package com.hingoli.delivery.ui.earnings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.delivery.data.model.EarningsSummary
import com.hingoli.delivery.data.model.MyDelivery
import com.hingoli.delivery.data.repository.DeliveryRepository
import com.hingoli.delivery.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EarningsUiState(
    val earnings: EarningsSummary? = null,
    val deliveryHistory: List<MyDelivery> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingHistory: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EarningsViewModel @Inject constructor(
    private val repository: DeliveryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EarningsUiState())
    val uiState: StateFlow<EarningsUiState> = _uiState.asStateFlow()
    
    init {
        loadEarnings()
        loadDeliveryHistory()
    }
    
    fun loadEarnings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getEarnings()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, earnings = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
    
    fun loadDeliveryHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHistory = true) }
            when (val result = repository.getMyOrders("completed")) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoadingHistory = false, deliveryHistory = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoadingHistory = false) }
                }
            }
        }
    }
    
    fun refresh() {
        loadEarnings()
        loadDeliveryHistory()
    }
}
