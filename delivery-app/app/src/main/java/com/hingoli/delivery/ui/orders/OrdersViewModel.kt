package com.hingoli.delivery.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.delivery.data.model.AvailableOrder
import com.hingoli.delivery.data.model.MyDelivery
import com.hingoli.delivery.data.repository.DeliveryRepository
import com.hingoli.delivery.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersUiState(
    val availableOrders: List<AvailableOrder> = emptyList(),
    val myDeliveries: List<MyDelivery> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val loadingOrderId: Long? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: DeliveryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Load both in parallel
            val ordersDeferred = async { repository.getAvailableOrders() }
            val deliveriesDeferred = async { repository.getMyOrders("active") }
            
            // Wait for both to complete
            val ordersResult = ordersDeferred.await()
            val deliveriesResult = deliveriesDeferred.await()
            
            // Update state
            var newOrders = _uiState.value.availableOrders
            var newDeliveries = _uiState.value.myDeliveries
            var error: String? = null
            
            when (ordersResult) {
                is Result.Success -> newOrders = ordersResult.data
                is Result.Error -> error = ordersResult.message
            }
            
            when (deliveriesResult) {
                is Result.Success -> newDeliveries = deliveriesResult.data
                is Result.Error -> if (error == null) error = deliveriesResult.message
            }
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    availableOrders = newOrders,
                    myDeliveries = newDeliveries,
                    error = error
                ) 
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            // Load both in parallel and wait for completion
            val ordersDeferred = async { repository.getAvailableOrders() }
            val deliveriesDeferred = async { repository.getMyOrders("active") }
            
            val ordersResult = ordersDeferred.await()
            val deliveriesResult = deliveriesDeferred.await()
            
            var newOrders = _uiState.value.availableOrders
            var newDeliveries = _uiState.value.myDeliveries
            var error: String? = null
            
            when (ordersResult) {
                is Result.Success -> newOrders = ordersResult.data
                is Result.Error -> error = ordersResult.message
            }
            
            when (deliveriesResult) {
                is Result.Success -> newDeliveries = deliveriesResult.data
                is Result.Error -> if (error == null) error = deliveriesResult.message
            }
            
            _uiState.update { 
                it.copy(
                    isRefreshing = false,
                    availableOrders = newOrders,
                    myDeliveries = newDeliveries,
                    error = error
                ) 
            }
        }
    }
    
    fun acceptOrder(orderId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingOrderId = orderId) }
            when (val result = repository.acceptOrder(orderId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(loadingOrderId = null, successMessage = result.data) }
                    refresh()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(loadingOrderId = null, error = result.message) }
                }
            }
        }
    }
    
    fun cancelOrder(orderId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingOrderId = orderId) }
            when (val result = repository.cancelOrder(orderId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(loadingOrderId = null, successMessage = result.data) }
                    refresh()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(loadingOrderId = null, error = result.message) }
                }
            }
        }
    }
    
    fun updateStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingOrderId = orderId) }
            when (val result = repository.updateStatus(orderId, status)) {
                is Result.Success -> {
                    _uiState.update { it.copy(loadingOrderId = null, successMessage = result.data) }
                    refresh()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(loadingOrderId = null, error = result.message) }
                }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
