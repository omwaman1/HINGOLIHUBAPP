package com.hingoli.delivery.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.delivery.data.model.DeliveryUser
import com.hingoli.delivery.data.repository.DeliveryRepository
import com.hingoli.delivery.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: DeliveryUser? = null,
    val name: String = "",
    val email: String = "",
    val address: String = "",
    val vehicleType: String = "bike",
    val vehicleNumber: String = "",
    val upiId: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: DeliveryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getProfile()) {
                is Result.Success -> {
                    val user = result.data
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            user = user,
                            name = user.name,
                            email = user.email ?: "",
                            address = user.address ?: "",
                            upiId = user.upiId ?: "",
                            vehicleType = user.vehicleType ?: "bike",
                            vehicleNumber = user.vehicleNumber ?: ""
                        ) 
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
    
    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }
    
    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }
    
    fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address) }
    }
    
    fun updateVehicleType(type: String) {
        _uiState.update { it.copy(vehicleType = type) }
    }
    
    fun updateVehicleNumber(number: String) {
        _uiState.update { it.copy(vehicleNumber = number) }
    }
    
    fun updateUpiId(upiId: String) {
        _uiState.update { it.copy(upiId = upiId) }
    }
    
    fun saveProfile() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            val updates = mapOf(
                "name" to state.name,
                "vehicle_type" to state.vehicleType,
                "vehicle_number" to state.vehicleNumber,
                "email" to state.email,
                "address" to state.address,
                "upi_id" to state.upiId
            ).filterValues { it.isNotEmpty() }
            
            when (val result = repository.updateProfile(updates)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isSaving = false, 
                            successMessage = "Profile updated successfully"
                        ) 
                    }
                    loadProfile()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.message) }
                }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
    
    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onLogout()
        }
    }
}
