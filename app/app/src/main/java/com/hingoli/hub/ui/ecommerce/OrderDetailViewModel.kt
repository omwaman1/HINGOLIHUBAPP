package com.hingoli.hub.ui.ecommerce

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.model.OrderDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderDetailUiState(
    val orderDetail: OrderDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()
    
    private var pollingJob: Job? = null
    private var currentOrderId: Long = 0
    
    // Poll interval in milliseconds (15 seconds)
    companion object {
        private const val POLL_INTERVAL_MS = 15000L
    }
    
    fun loadOrderDetail(orderId: Long) {
        currentOrderId = orderId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val response = apiService.getOrderById(orderId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orderDetail = response.body()?.data
                    )
                    
                    // Start polling for status updates if order is not in final state
                    val status = response.body()?.data?.orderStatus?.lowercase()
                    if (status != "delivered" && status != "cancelled") {
                        startPolling(orderId)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.body()?.message ?: "Failed to load order details"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Network error"
                )
            }
        }
    }
    
    /**
     * Start polling for order status updates every 15 seconds
     * This provides real-time status updates to the customer
     */
    private fun startPolling(orderId: Long) {
        stopPolling() // Cancel any existing polling job
        
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS)
                
                try {
                    val response = apiService.getOrderById(orderId)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val newDetail = response.body()?.data
                        val currentStatus = _uiState.value.orderDetail?.orderStatus
                        val newStatus = newDetail?.orderStatus
                        
                        // Update UI if status changed
                        if (newStatus != currentStatus) {
                            _uiState.value = _uiState.value.copy(orderDetail = newDetail)
                        }
                        
                        // Stop polling if order reached final state
                        if (newStatus?.lowercase() == "delivered" || newStatus?.lowercase() == "cancelled") {
                            stopPolling()
                            break
                        }
                    }
                } catch (e: Exception) {
                    // Silently ignore polling errors, keep trying
                }
            }
        }
    }
    
    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }
    
    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
