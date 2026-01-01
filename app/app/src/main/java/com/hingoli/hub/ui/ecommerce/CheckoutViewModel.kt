package com.hingoli.hub.ui.ecommerce

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val cartItems: List<CartItem> = emptyList(),
    val cartTotal: Double = 0.0,
    val addresses: List<UserAddress> = emptyList(),
    val selectedAddressId: Long? = null,
    val paymentMethod: String = "razorpay", // "razorpay" or "cod"
    val isLoading: Boolean = false,
    val isPlacingOrder: Boolean = false,
    val error: String? = null,
    val orderResult: CreateOrderResponse? = null,
    val showAddAddress: Boolean = false
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val cartResponse = apiService.getCart()
                if (cartResponse.isSuccessful && cartResponse.body()?.data != null) {
                    val cart = cartResponse.body()!!.data!!
                    _uiState.value = _uiState.value.copy(
                        cartItems = cart.items,
                        cartTotal = cart.total
                    )
                }
                
                val addressResponse = apiService.getAddresses()
                if (addressResponse.isSuccessful && addressResponse.body()?.data != null) {
                    val addresses = addressResponse.body()!!.data!!
                    val defaultAddress = addresses.find { it.isDefault }
                    _uiState.value = _uiState.value.copy(
                        addresses = addresses,
                        selectedAddressId = defaultAddress?.addressId ?: addresses.firstOrNull()?.addressId
                    )
                }
                
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun selectAddress(addressId: Long) {
        _uiState.value = _uiState.value.copy(selectedAddressId = addressId)
    }
    
    fun setPaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }
    
    fun toggleAddAddress() {
        _uiState.value = _uiState.value.copy(showAddAddress = !_uiState.value.showAddAddress)
    }
    
    fun addAddress(name: String, phone: String, addressLine1: String, addressLine2: String, city: String, pincode: String) {
        viewModelScope.launch {
            try {
                val response = apiService.addAddress(
                    AddAddressRequest(
                        name = name,
                        phone = phone,
                        addressLine1 = addressLine1,
                        addressLine2 = addressLine2.takeIf { it.isNotBlank() },
                        city = city,
                        pincode = pincode
                    )
                )
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(showAddAddress = false)
                    loadData() // Reload addresses
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun placeOrder(onRazorpayRequired: (orderId: Long, razorpayOrderId: String, amount: Double) -> Unit) {
        val addressId = _uiState.value.selectedAddressId
        if (addressId == null) {
            _uiState.value = _uiState.value.copy(error = "Please select a delivery address")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPlacingOrder = true, error = null)
            try {
                val response = apiService.createOrder(
                    CreateOrderRequest(
                        addressId = addressId,
                        paymentMethod = _uiState.value.paymentMethod
                    )
                )
                
                if (response.isSuccessful && response.body()?.data != null) {
                    val orderResult = response.body()!!.data!!
                    
                    _uiState.value = _uiState.value.copy(
                        isPlacingOrder = false,
                        orderResult = orderResult
                    )
                    
                    // If Razorpay, trigger payment
                    if (_uiState.value.paymentMethod == "razorpay" && orderResult.razorpayOrderId != null) {
                        onRazorpayRequired(
                            orderResult.orderId,
                            orderResult.razorpayOrderId,
                            orderResult.totalAmount
                        )
                    } else if (_uiState.value.paymentMethod == "razorpay" && orderResult.razorpayOrderId == null) {
                        val errorMsg = orderResult.razorpayError ?: "Payment gateway unavailable"
                        _uiState.value = _uiState.value.copy(error = "Payment failed: $errorMsg")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isPlacingOrder = false,
                        error = "Failed to place order: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPlacingOrder = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun verifyPayment(orderId: Long, paymentId: String, signature: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.verifyPayment(
                    orderId,
                    VerifyPaymentRequest(paymentId, signature)
                )
                if (response.isSuccessful) {
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Payment verification failed")
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
