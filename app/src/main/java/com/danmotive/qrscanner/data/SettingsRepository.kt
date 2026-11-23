package com.danmotive.qrscanner.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(
    name = "settings"
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage {
    SYSTEM,
    RU,
    EN
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM
)

class SettingsRepository(
    private val context: Context
) {

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            themeMode = preferences[THEME_MODE]
                ?.let { value ->
                    ThemeMode.entries.firstOrNull { it.name == value }
                } ?: ThemeMode.SYSTEM,

            language = preferences[LANGUAGE]
                ?.let { value ->
                    AppLanguage.entries.firstOrNull { it.name == value }
                } ?: AppLanguage.SYSTEM
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { preferences ->
            preferences[LANGUAGE] = language.name
        }
    }
}

