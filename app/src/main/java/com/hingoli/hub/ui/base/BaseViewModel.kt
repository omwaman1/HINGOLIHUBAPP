package com.hingoli.hub.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hingoli.hub.data.settings.AppLanguage
import com.hingoli.hub.data.settings.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Base ViewModel that provides common functionality across all ViewModels.
 * Includes language settings (isMarathi) that can be observed by UI.
 */
abstract class BaseViewModel(
    settingsManager: SettingsManager
) : ViewModel() {
    
    /**
     * Language setting - true if Marathi, false if English.
     * Use this in your UiState to access localized strings.
     */
    val isMarathi: StateFlow<Boolean> = settingsManager.languageFlow
        .map { it == AppLanguage.MARATHI }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true  // Default to Marathi
        )
}
