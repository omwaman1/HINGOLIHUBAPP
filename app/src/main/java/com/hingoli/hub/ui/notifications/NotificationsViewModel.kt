package com.hingoli.hub.ui.notifications

import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.ApiService
import com.hingoli.hub.data.settings.SettingsManager
import com.hingoli.hub.data.model.MarkReadRequest
import com.hingoli.hub.data.model.NotificationItem
import com.hingoli.hub.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = false,
    val isMarathi: Boolean = true
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val apiService: ApiService,
    settingsManager: SettingsManager
) : BaseViewModel(settingsManager) {
    
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()
    
    init {
        loadNotifications()
        // Sync language from BaseViewModel
        viewModelScope.launch {
            isMarathi.collect { value ->
                _uiState.value = _uiState.value.copy(isMarathi = value)
            }
        }
    }
    
    fun loadNotifications(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val response = apiService.getNotificationHistory(page = page)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    _uiState.value = _uiState.value.copy(
                        notifications = if (page == 1) {
                            data?.notifications ?: emptyList()
                        } else {
                            _uiState.value.notifications + (data?.notifications ?: emptyList())
                        },
                        isLoading = false,
                        page = page,
                        hasMore = data?.hasMore ?: false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load notifications"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading notifications"
                )
            }
        }
    }
    
    fun loadMore() {
        if (_uiState.value.hasMore && !_uiState.value.isLoading) {
            loadNotifications(_uiState.value.page + 1)
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                // Send empty request body to mark all as read
                apiService.markNotificationsRead(MarkReadRequest())
                // Update local state
                _uiState.value = _uiState.value.copy(
                    notifications = _uiState.value.notifications.map { it.copy(isReadInt = 1) }
                )
            } catch (e: Exception) {
                // Ignore error
            }
        }
    }
    
    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            try {
                apiService.markNotificationsRead(MarkReadRequest(listOf(notificationId)))
                // Update local state
                _uiState.value = _uiState.value.copy(
                    notifications = _uiState.value.notifications.map { 
                        if (it.id == notificationId) it.copy(isReadInt = 1) else it 
                    }
                )
            } catch (e: Exception) {
                // Ignore error
            }
        }
    }
    
    fun refresh() {
        loadNotifications(page = 1)
    }
}
