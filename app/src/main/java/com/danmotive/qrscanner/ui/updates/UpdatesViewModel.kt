package com.danmotive.qrscanner.ui.updates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danmotive.qrscanner.BuildConfig
import com.danmotive.qrscanner.data.UpdatesRepository
import com.danmotive.qrscanner.ui.core.isNewerVersion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object NoUpdates : UpdateState
    data object Error : UpdateState
    data class Available(val version: String) : UpdateState
}

class UpdatesViewModel : ViewModel() {
    private val repository = UpdatesRepository()

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state = _state.asStateFlow()

    init {
        checkUpdates()
    }

    fun checkUpdates() {
        if (_state.value == UpdateState.Checking) return

        _state.value = UpdateState.Checking

        viewModelScope.launch {
            try {
                val remoteVersion = repository.latestVersion()

                _state.value = if (
                    remoteVersion != null &&
                    isNewerVersion(BuildConfig.VERSION_NAME, remoteVersion)
                ) {
                    UpdateState.Available(remoteVersion)
                } else {
                    UpdateState.NoUpdates
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.value = UpdateState.Error
            }
        }
    }
}
