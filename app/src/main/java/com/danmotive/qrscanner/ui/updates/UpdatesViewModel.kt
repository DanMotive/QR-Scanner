package com.danmotive.qrscanner.ui.updates

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.danmotive.qrscanner.BuildConfig
import com.danmotive.qrscanner.data.UpdatesRepository
import com.danmotive.qrscanner.ui.core.isNewerVersion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object NoUpdates : UpdateState
    data object Error : UpdateState
    data class Available(val version: String) : UpdateState
}

class UpdatesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UpdatesRepository(application)
    private val initialNextCheckAt = nextUpdateCheckAt(
        repository.lastCheckedAt(),
        System.currentTimeMillis()
    )
    private val _nextCheckAt = MutableStateFlow(initialNextCheckAt)
    val nextCheckAt = _nextCheckAt.asStateFlow()

    private val cachedAvailableVersion = repository.cachedLatestVersion()
        ?.takeIf {
            initialNextCheckAt != null &&
                    isNewerVersion(BuildConfig.VERSION_NAME, it)
        }

    private val _state = MutableStateFlow<UpdateState>(
        cachedAvailableVersion?.let(UpdateState::Available) ?: UpdateState.Idle
    )
    val state = _state.asStateFlow()

    private var cooldownJob: Job? = null

    init {
        if (initialNextCheckAt == null) {
            checkUpdates()
        } else {
            scheduleCooldownExpiry(initialNextCheckAt)
        }
    }

    fun checkUpdates() {
        if (_state.value == UpdateState.Checking) return

        val now = System.currentTimeMillis()
        val nextAllowedAt = nextUpdateCheckAt(repository.lastCheckedAt(), now)
        if (nextAllowedAt != null) {
            scheduleCooldownExpiry(nextAllowedAt)
            return
        }

        val cooldownUntil = now + UPDATE_CHECK_COOLDOWN_MS
        repository.markCheckStarted(now)
        scheduleCooldownExpiry(cooldownUntil)
        _state.value = UpdateState.Checking

        viewModelScope.launch {
            try {
                val remoteVersion = repository.latestVersion()
                repository.cacheLatestVersion(remoteVersion)

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

    private fun scheduleCooldownExpiry(availableAt: Long) {
        cooldownJob?.cancel()
        _nextCheckAt.value = availableAt
        cooldownJob = viewModelScope.launch {
            delay((availableAt - System.currentTimeMillis()).coerceAtLeast(0L).milliseconds)
            if (_nextCheckAt.value == availableAt) {
                _nextCheckAt.value = null
            }
        }
    }
}
