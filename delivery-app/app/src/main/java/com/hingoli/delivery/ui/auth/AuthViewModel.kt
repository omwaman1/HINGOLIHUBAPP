package com.hingoli.delivery.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.delivery.data.repository.DeliveryRepository
import com.hingoli.delivery.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val phone: String = "",
    val otp: String = "",
    val isNewUser: Boolean = false,
    val isOtpSent: Boolean = false,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isCheckingAuth: Boolean = true,  // Start true to prevent login flash
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: DeliveryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            val loggedIn = repository.isLoggedIn()
            _uiState.update { it.copy(isLoggedIn = loggedIn, isCheckingAuth = false) }
        }
    }
    
    fun updatePhone(phone: String) {
        if (phone.length <= 10 && phone.all { it.isDigit() }) {
            _uiState.update { it.copy(phone = phone, error = null) }
        }
    }
    
    fun updateOtp(otp: String) {
        if (otp.length <= 6 && otp.all { it.isDigit() }) {
            _uiState.update { it.copy(otp = otp, error = null) }
        }
    }
    
    fun resetOtp() {
        _uiState.update { it.copy(isOtpSent = false, otp = "", error = null, isNewUser = false) }
    }
    
    fun sendOtp() {
        val phone = _uiState.value.phone
        if (phone.length != 10) {
            _uiState.update { it.copy(error = "Enter valid 10-digit phone number") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.sendOtp(phone)) {
                is Result.Success -> {
                    // Check if API returned is_new_user flag
                    val isNewUser = result.data?.get("is_new_user") as? Boolean ?: false
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            isOtpSent = true,
                            isNewUser = isNewUser
                        ) 
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
    
    fun verifyOtp() {
        val state = _uiState.value
        if (state.otp.length != 6) {
            _uiState.update { it.copy(error = "Enter 6-digit OTP") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.verifyOtp(state.phone, state.otp)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { AuthUiState() }
        }
    }
}
