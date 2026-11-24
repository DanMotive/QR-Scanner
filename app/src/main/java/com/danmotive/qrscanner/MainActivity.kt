
package com.danmotive.qrscanner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
        setContent {
            val context = LocalContext.current

            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(
                    SettingsRepository(context)
                )
            )

            val settings by settingsViewModel.settings.collectAsState()

            LaunchedEffect(settings.language) {
                val languageTag = when (settings.language) {
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

            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            QRScannerTheme(themeMode = settings.themeMode) {
                QrScannerApp(
                    themeMode = settings.themeMode,
                    onThemeModeChange = settingsViewModel::setThemeMode,
                    language = settings.language,
                    onLanguageChange = settingsViewModel::setLanguage
                )
            }
        }
    }
}
