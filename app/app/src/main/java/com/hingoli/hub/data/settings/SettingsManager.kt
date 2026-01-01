package com.hingoli.hub.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "ENGLISH"),
    MARATHI("mr", "मराठी")
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val USE_SYSTEM_THEME_KEY = booleanPreferencesKey("use_system_theme")
    }
    
    val languageFlow: Flow<AppLanguage> = context.settingsDataStore.data.map { preferences ->
        val code = preferences[LANGUAGE_KEY] ?: AppLanguage.MARATHI.code  // Default to Marathi
        AppLanguage.entries.find { it.code == code } ?: AppLanguage.MARATHI
    }
    
    val isDarkModeFlow: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }
    
    val useSystemThemeFlow: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[USE_SYSTEM_THEME_KEY] ?: true
    }
    
    suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.code
        }
    }
    
    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
            preferences[USE_SYSTEM_THEME_KEY] = false
        }
    }
    
    suspend fun setUseSystemTheme(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[USE_SYSTEM_THEME_KEY] = enabled
        }
    }
}
