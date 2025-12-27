package com.hingoli.hub.ui.ecommerce

import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.settings.SettingsManager
import com.hingoli.hub.data.model.CartItem
import com.hingoli.hub.data.model.UpdateCartRequest
import com.hingoli.hub.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val isMarathi: Boolean = true
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val apiService: ApiService,
    settingsManager: SettingsManager
) : BaseViewModel(settingsManager) {
    
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
    
    init {
        loadCart()
        // Sync language from BaseViewModel
        viewModelScope.launch {
            isMarathi.collect { value ->
                _uiState.value = _uiState.value.copy(isMarathi = value)
            }
        }
    }
    
    fun loadCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = apiService.getCart()
                if (response.isSuccessful && response.body()?.data != null) {
                    val cart = response.body()!!.data!!
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        items = cart.items,
                        total = cart.total,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load cart"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun updateQuantity(cartItemId: Long, newQuantity: Int) {
        if (newQuantity < 1) return
        
        // Optimistic update - update UI immediately
        val oldItems = _uiState.value.items
        val updatedItems = oldItems.map { item ->
            if (item.cartItemId == cartItemId) item.copy(quantity = newQuantity) else item
        }
        val newTotal = updatedItems.sumOf { it.price * it.quantity }
        _uiState.value = _uiState.value.copy(items = updatedItems, total = newTotal)
        
        // Sync with backend in background (no loading indicator)
        viewModelScope.launch {
            try {
                val response = apiService.updateCartItem(cartItemId, UpdateCartRequest(newQuantity))
                if (!response.isSuccessful) {
                    // Revert on failure
                    _uiState.value = _uiState.value.copy(items = oldItems, total = oldItems.sumOf { it.price * it.quantity })
                }
            } catch (e: Exception) {
                // Revert on error
                _uiState.value = _uiState.value.copy(items = oldItems, total = oldItems.sumOf { it.price * it.quantity })
            }
        }
    }
    
    fun removeItem(cartItemId: Long) {
        // Optimistic update - remove from UI immediately
        val oldItems = _uiState.value.items
        val updatedItems = oldItems.filter { it.cartItemId != cartItemId }
        val newTotal = updatedItems.sumOf { it.price * it.quantity }
        _uiState.value = _uiState.value.copy(items = updatedItems, total = newTotal)
        
        // Sync with backend in background
        viewModelScope.launch {
            try {
                val response = apiService.removeCartItem(cartItemId)
                if (!response.isSuccessful) {
                    // Revert on failure
                    _uiState.value = _uiState.value.copy(items = oldItems, total = oldItems.sumOf { it.price * it.quantity })
                }
            } catch (e: Exception) {
                // Revert on error
                _uiState.value = _uiState.value.copy(items = oldItems, total = oldItems.sumOf { it.price * it.quantity })
            }
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            try {
                apiService.clearCart()
                _uiState.value = CartUiState()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
