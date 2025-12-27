package com.hingoli.hub.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.api.TokenManager
import com.hingoli.hub.data.model.ChatMessage
import com.hingoli.hub.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentUserId: Long = 0,
    val currentUserName: String = "",
    val otherUserId: Long = 0,
    val conversationId: String = "",
    val messageText: String = "",
    val isSending: Boolean = false
)

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val conversationId: String = savedStateHandle["conversationId"] ?: ""
    
    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()
    
    init {
        loadMessages()
    }
    
    private fun loadMessages() {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: return@launch
            val userName = tokenManager.getUserName() ?: ""
            _uiState.value = _uiState.value.copy(
                currentUserId = userId,
                currentUserName = userName,
                conversationId = conversationId,
                isLoading = true
            )
            
            try {
                // Mark messages as read when entering conversation
                chatRepository.markMessagesAsRead(conversationId, userId)
                
                chatRepository.getMessages(conversationId).collect { messages ->
                    // Find other user ID from messages
                    val otherUserId = messages.firstOrNull { it.senderId != userId }?.senderId ?: 0
                    
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        otherUserId = otherUserId,
                        isLoading = false,
                        error = null
                    )
                    
                    // Mark as read whenever we receive new messages
                    chatRepository.markMessagesAsRead(conversationId, userId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load messages"
                )
            }
        }
    }
    
    fun updateMessageText(text: String) {
        _uiState.value = _uiState.value.copy(messageText = text)
    }
    
    fun sendMessage() {
        val text = _uiState.value.messageText.trim()
        if (text.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            
            try {
                chatRepository.sendMessage(
                    conversationId = conversationId,
                    senderId = _uiState.value.currentUserId,
                    text = text
                )
                _uiState.value = _uiState.value.copy(
                    messageText = "",
                    isSending = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = e.message
                )
            }
        }
    }
}
