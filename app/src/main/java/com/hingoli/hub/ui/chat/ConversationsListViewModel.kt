package com.hingoli.hub.ui.chat

import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.TokenManager
import com.hingoli.hub.data.settings.SettingsManager
import com.hingoli.hub.data.model.Conversation
import com.hingoli.hub.data.repository.ChatRepository
import com.hingoli.hub.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationsListUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentUserId: Long = 0,
    val isMarathi: Boolean = true
)

@HiltViewModel
class ConversationsListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager,
    settingsManager: SettingsManager
) : BaseViewModel(settingsManager) {
    
    private val _uiState = MutableStateFlow(ConversationsListUiState())
    val uiState: StateFlow<ConversationsListUiState> = _uiState.asStateFlow()
    
    init {
        loadConversations()
        // Sync language from BaseViewModel
        viewModelScope.launch {
            isMarathi.collect { value ->
                _uiState.value = _uiState.value.copy(isMarathi = value)
            }
        }
    }
    
    private fun loadConversations() {
        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            
            if (userId == null || userId == 0L) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    conversations = emptyList(),
                    error = null
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(currentUserId = userId, isLoading = true)
            
            try {
                chatRepository.getConversations(userId).collect { conversations ->
                    _uiState.value = _uiState.value.copy(
                        conversations = conversations,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load conversations"
                )
            }
        }
    }
    
    fun refresh() {
        loadConversations()
    }
}
