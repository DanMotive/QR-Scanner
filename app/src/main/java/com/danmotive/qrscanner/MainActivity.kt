
package com.danmotive.qrscanner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danmotive.qrscanner.data.AppLanguage
import com.danmotive.qrscanner.data.SettingsRepository
import com.danmotive.qrscanner.data.ThemeMode
import com.danmotive.qrscanner.ui.QrScannerApp
import com.danmotive.qrscanner.ui.settings.SettingsViewModel
import com.danmotive.qrscanner.ui.settings.SettingsViewModelFactory
import com.danmotive.qrscanner.ui.theme.QRScannerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val STATE_FADE_IN = "fade_in_from_black"
private const val FADE_OUT_MS = 400
private const val BLACK_HOLD_MS = 200L
private const val FADE_IN_MS = 450

class MainActivity : AppCompatActivity() {

    private var pendingLanguageChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ProcessCameraProvider.getInstance(this)

        val fadeInFromBlack = savedInstanceState?.getBoolean(STATE_FADE_IN) == true
        if (fadeInFromBlack) {
            window.setBackgroundDrawable(0xFF000000.toInt().toDrawable())
        }

        setContent {
            val context = LocalContext.current

            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(
                    SettingsRepository(context)
                )
            )

            val settings by settingsViewModel.settings.collectAsState()

            val scope = rememberCoroutineScope()
            val blackout = remember {
                Animatable(if (fadeInFromBlack) 1f else 0f)
            }

            LaunchedEffect(Unit) {
                if (fadeInFromBlack) {
                    blackout.animateTo(0f, tween(FADE_IN_MS))
                }
            }

            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            LaunchedEffect(darkTheme) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            Box {
                QRScannerTheme(themeMode = settings.themeMode) {
                    QrScannerApp(
                        themeMode = settings.themeMode,
                        onThemeModeChange = { mode ->
                            if (mode != settings.themeMode &&
                                !blackout.isRunning &&
                                blackout.value == 0f
                            ) {
                                scope.launch {
                                    blackout.animateTo(1f, tween(FADE_OUT_MS))
                                    settingsViewModel.setThemeMode(mode)
                                    delay(BLACK_HOLD_MS.milliseconds)
                                    blackout.animateTo(0f, tween(FADE_OUT_MS))
                                }
                            }
                        },
                        language = settings.language,
                        onLanguageChange = { newLanguage ->
                            if (newLanguage != settings.language &&
                                !blackout.isRunning &&
                                blackout.value == 0f
                            ) {
                                scope.launch {
                                    blackout.animateTo(1f, tween(FADE_OUT_MS))
                                    settingsViewModel.setLanguage(newLanguage)
                                    pendingLanguageChange = true
                                    AppCompatDelegate.setApplicationLocales(
                                        LocaleListCompat.forLanguageTags(newLanguage.tag)
                                    )
                                    delay(FADE_IN_MS.toLong().milliseconds)
                                    blackout.animateTo(0f, tween(FADE_IN_MS))
                                }
                            }
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = blackout.value))
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (pendingLanguageChange) {
            outState.putBoolean(STATE_FADE_IN, true)
        }
    }
}

private val AppLanguage.tag: String
    get() = when (this) {
        AppLanguage.SYSTEM -> ""
        AppLanguage.RU -> "ru"
        AppLanguage.EN -> "en"
    }
