package com.hingoli.hub.ui.reels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.model.Reel
import com.hingoli.hub.data.model.ReelActionRequest
import com.hingoli.hub.data.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReelsUiState(
    val isLoading: Boolean = true,
    val reels: List<Reel> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ReelsViewModel @Inject constructor(
    private val repository: ListingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReelsUiState())
    val uiState: StateFlow<ReelsUiState> = _uiState.asStateFlow()
    
    init {
        loadReels()
    }
    
    fun loadReels() {
        viewModelScope.launch {
            _uiState.value = ReelsUiState(isLoading = true)
            android.util.Log.d("ReelsVM", "Loading reels...")
            
            try {
                val response = repository.getReels()
                android.util.Log.d("ReelsVM", "Response: success=${response.success}, message=${response.message}")
                android.util.Log.d("ReelsVM", "Data: ${response.data?.reels?.size ?: 0} reels")
                
                if (response.success && response.data != null) {
                    _uiState.value = ReelsUiState(
                        isLoading = false,
                        reels = response.data.reels
                    )
                    android.util.Log.d("ReelsVM", "Loaded ${response.data.reels.size} reels successfully")
                } else {
                    val errorMsg = response.message ?: "Failed to load reels"
                    android.util.Log.e("ReelsVM", "Load failed: $errorMsg")
                    _uiState.value = ReelsUiState(
                        isLoading = false,
                        error = errorMsg
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ReelsVM", "Exception loading reels: ${e.javaClass.simpleName} - ${e.message}")
                e.printStackTrace()
                _uiState.value = ReelsUiState(
                    isLoading = false,
                    error = e.message ?: "Network error"
                )
            }
        }
    }
    
    /**
     * Toggle like on a reel. Updates local state immediately for responsiveness.
     */
    fun toggleLike(reelId: Int) {
        viewModelScope.launch {
            // Optimistic update
            _uiState.update { state ->
                state.copy(
                    reels = state.reels.map { reel ->
                        if (reel.reelId == reelId) {
                            reel.copy(
                                isLiked = !reel.isLiked,
                                likesCount = if (reel.isLiked) reel.likesCount - 1 else reel.likesCount + 1
                            )
                        } else reel
                    }
                )
            }
            
            // Call API
            try {
                val response = repository.likeReel(ReelActionRequest(reelId))
                if (response.success && response.data != null) {
                    // Update with actual server values
                    _uiState.update { state ->
                        state.copy(
                            reels = state.reels.map { reel ->
                                if (reel.reelId == reelId) {
                                    reel.copy(
                                        isLiked = response.data.isLiked,
                                        likesCount = response.data.likesCount
                                    )
                                } else reel
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                // Revert on error
                _uiState.update { state ->
                    state.copy(
                        reels = state.reels.map { reel ->
                            if (reel.reelId == reelId) {
                                reel.copy(
                                    isLiked = !reel.isLiked,
                                    likesCount = if (reel.isLiked) reel.likesCount - 1 else reel.likesCount + 1
                                )
                            } else reel
                        }
                    )
                }
            }
        }
    }
    
    /**
     * Mark a reel as watched so it won't appear again until all are watched
     */
    fun markWatched(reelId: Int) {
        viewModelScope.launch {
            try {
                repository.markReelWatched(ReelActionRequest(reelId))
            } catch (e: Exception) {
                // Silently ignore - not critical if this fails
            }
        }
    }
}
