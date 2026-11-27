
package com.danmotive.qrscanner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danmotive.qrscanner.BuildConfig
import com.danmotive.qrscanner.R
import com.danmotive.qrscanner.data.AppLanguage
import com.danmotive.qrscanner.data.ThemeMode
import com.danmotive.qrscanner.ui.updates.UpdateState
import java.text.DateFormat
import java.util.Date

private const val GITHUB_URL =
    "https://github.com/DanMotive/QR-Scanner"

private const val TELEGRAM_URL =
    "https://t.me/danmotivestudio"

@Composable
fun OtherScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    updateState: UpdateState,
    nextUpdateCheckAt: Long?,
    onCheckUpdates: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val openError = stringResource(R.string.error_link)
    val checkUpdatesTitle = stringResource(R.string.settings_updates)
    val openReleaseTitle = stringResource(R.string.updates_open_release)
    val checkingUpdatesText = stringResource(R.string.updates_checking)
    val noUpdatesText = stringResource(R.string.updates_no_updates)
    val updateErrorText = stringResource(R.string.updates_check_failed)
    val rateLimitedText = if (nextUpdateCheckAt != null) {
        val locale = LocalConfiguration.current.locales[0]
        val availableAt = DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT,
            locale
        ).format(Date(nextUpdateCheckAt))
        stringResource(R.string.updates_rate_limited, availableAt)
    } else {
        null
    }
    val availableUpdateText = stringResource(
        R.string.updates_available_version,
        (updateState as? UpdateState.Available)?.version.orEmpty()
    )

    val updatesValue = when {
        updateState is UpdateState.Checking -> checkingUpdatesText
        updateState is UpdateState.Available -> availableUpdateText
        nextUpdateCheckAt != null -> rateLimitedText
        updateState == UpdateState.NoUpdates -> noUpdatesText
        updateState == UpdateState.Error -> updateErrorText
        else -> null
    }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val themeName = stringResource(
        when (themeMode) {
            ThemeMode.SYSTEM -> R.string.settings_theme_system
            ThemeMode.LIGHT -> R.string.settings_theme_light
            ThemeMode.DARK -> R.string.settings_theme_dark
        }
    )

    val languageName = stringResource(
        when (language) {
            AppLanguage.SYSTEM -> R.string.settings_language_system
            AppLanguage.RU -> R.string.language_russian
            AppLanguage.EN -> R.string.language_english
        }
    )

    val openUrl: (String) -> Unit = { url ->
        try {
            uriHandler.openUri(url)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                openError,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    R.string.about_version,
                    BuildConfig.VERSION_NAME
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider()

        SettingRow(
            title = stringResource(R.string.settings_theme),
            value = themeName,
            onClick = { showThemeDialog = true }
        )

        SettingRow(
            title = stringResource(R.string.settings_language),
            value = languageName,
            onClick = { showLanguageDialog = true }
        )

        HorizontalDivider()

        SettingRow(
            title = stringResource(R.string.settings_github),
            onClick = { openUrl(GITHUB_URL) }
        )

        SettingRow(
            title = stringResource(R.string.settings_telegram),
            onClick = { openUrl(TELEGRAM_URL) }
        )

        SettingRow(
            title = if (updateState is UpdateState.Available) {
                openReleaseTitle
            } else {
                checkUpdatesTitle
            },
            value = updatesValue,
            enabled = updateState != UpdateState.Checking &&
                    (updateState is UpdateState.Available || nextUpdateCheckAt == null),
            onClick = {
                if (updateState is UpdateState.Available) {
                    openUrl("$GITHUB_URL/releases/latest")
                } else {
                    onCheckUpdates()
                }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.settings_theme_dialog_title)) },
            text = {
                Column {
                    OptionRow(
                        title = stringResource(R.string.settings_theme_system),
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = {
                            onThemeModeChange(ThemeMode.SYSTEM)
                            showThemeDialog = false
                        }
                    )

                    OptionRow(
                        title = stringResource(R.string.settings_theme_light),
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = {
                            onThemeModeChange(ThemeMode.LIGHT)
                            showThemeDialog = false
                        }
                    )

                    OptionRow(
                        title = stringResource(R.string.settings_theme_dark),
                        selected = themeMode == ThemeMode.DARK,
                        onClick = {
                            onThemeModeChange(ThemeMode.DARK)
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_language_dialog_title)) },
            text = {
                Column {
                    OptionRow(
                        title = stringResource(R.string.settings_language_system),
                        selected = language == AppLanguage.SYSTEM,
                        onClick = {
                            onLanguageChange(AppLanguage.SYSTEM)
                            showLanguageDialog = false
                        }
                    )

                    OptionRow(
                        title = stringResource(R.string.language_russian),
                        selected = language == AppLanguage.RU,
                        onClick = {
                            onLanguageChange(AppLanguage.RU)
                            showLanguageDialog = false
                        }
                    )

                    OptionRow(
                        title = stringResource(R.string.language_english),
                        selected = language == AppLanguage.EN,
                        onClick = {
                            onLanguageChange(AppLanguage.EN)
                            showLanguageDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}

@Composable
private fun OptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Text(
            text = title,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    value: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
