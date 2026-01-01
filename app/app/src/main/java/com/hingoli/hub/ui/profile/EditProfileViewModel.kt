package com.hingoli.hub.ui.profile

import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.api.TokenManager
import com.hingoli.hub.data.settings.SettingsManager
import com.hingoli.hub.data.model.UpdateProfileRequest
import com.hingoli.hub.data.model.UserProfile
import com.hingoli.hub.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val isMarathi: Boolean = true
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    settingsManager: SettingsManager
) : BaseViewModel(settingsManager) {
    
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
        // Sync language from BaseViewModel
        viewModelScope.launch {
            isMarathi.collect { value ->
                _uiState.value = _uiState.value.copy(isMarathi = value)
            }
        }
    }
    
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getProfile()
                if (response.isSuccessful && response.body()?.data != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        profile = response.body()!!.data
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.body()?.message ?: "Failed to load profile"
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
    
    fun updateProfile(username: String, email: String?, gender: String?, dateOfBirth: String?, password: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)
            try {
                val request = UpdateProfileRequest(
                    username = username.takeIf { it.isNotBlank() },
                    email = email?.takeIf { it.isNotBlank() },
                    gender = gender?.takeIf { it.isNotBlank() },
                    dateOfBirth = dateOfBirth?.takeIf { it.isNotBlank() },
                    password = password?.takeIf { it.isNotBlank() }
                )
                val response = apiService.updateProfile(request)
                if (response.isSuccessful && response.body()?.data != null) {
                    val updatedProfile = response.body()!!.data!!
                    // Update TokenManager with new username so isProfileComplete() returns true
                    tokenManager.updateUserName(updatedProfile.username)
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        profile = updatedProfile,
                        saveSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = response.body()?.message ?: "Failed to update profile"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Network error"
                )
            }
        }
    }
    
    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}
