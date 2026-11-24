
package com.danmotive.qrscanner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ProcessCameraProvider.getInstance(this)
        val activityWindow = window
        setContent {
            val context = LocalContext.current

            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(
                    SettingsRepository(context)
                )
            )

            val settings by settingsViewModel.settings.collectAsState()

            // Локальное состояние меняется сразу по клику, не ждём Flow/DataStore.
            var themeMode by remember { mutableStateOf(settings.themeMode) }
            var language by remember { mutableStateOf(settings.language) }

            LaunchedEffect(settings.themeMode) {
                themeMode = settings.themeMode
            }
            LaunchedEffect(settings.language) {
                language = settings.language
            }

            LaunchedEffect(language) {
                val languageTag = when (language) {
                    AppLanguage.SYSTEM -> ""
                    AppLanguage.RU -> "ru"
                    AppLanguage.EN -> "en"
                }

                val currentLanguage =
                    AppCompatDelegate.getApplicationLocales().toLanguageTags()

                if (currentLanguage != languageTag) {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(languageTag)
                    )
                }
            }

            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            QRScannerTheme(themeMode = themeMode) {
                LaunchedEffect(darkTheme) {
                    WindowCompat.getInsetsController(
                        activityWindow,
                        activityWindow.decorView
                    ).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }

                QrScannerApp(
                    themeMode = themeMode,
                    onThemeModeChange = { mode ->
                        themeMode = mode
                        settingsViewModel.setThemeMode(mode)
                    },
                    language = language,
                    onLanguageChange = { newLanguage ->
                        language = newLanguage
                        settingsViewModel.setLanguage(newLanguage)
                    }
                )
            }
        }
    }
}
