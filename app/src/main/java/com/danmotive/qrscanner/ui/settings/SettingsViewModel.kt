package com.danmotive.qrscanner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danmotive.qrscanner.data.AppLanguage
import com.danmotive.qrscanner.data.AppSettings
import com.danmotive.qrscanner.data.SettingsRepository
import com.danmotive.qrscanner.data.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private var pendingWrites = 0

    init {
        viewModelScope.launch {
            // Читаем сохранённые настройки один раз: дальше источник истины —
            // оптимистичное состояние в памяти, без лишних повторных эмиссий DataStore.
            val stored = repository.settings.first()
            if (pendingWrites == 0) {
                _settings.value = stored
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        if (_settings.value.themeMode == mode) return
        _settings.update { it.copy(themeMode = mode) }
        persist { repository.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        if (_settings.value.language == language) return
        _settings.update { it.copy(language = language) }
        persist { repository.setLanguage(language) }
    }

    private fun persist(write: suspend () -> Unit) {
        pendingWrites++
        viewModelScope.launch {
            try {
                write()
            } finally {
                pendingWrites--
            }
        }
    }
}
