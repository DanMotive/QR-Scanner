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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danmotive.qrscanner.BuildConfig
import com.danmotive.qrscanner.R
import java.util.Locale

private const val GITHUB_URL = "https://github.com/DanMotive/QR-Scanner"
private const val TELEGRAM_URL = "https://t.me/danmotivestudio"

@Composable
fun OtherScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val soon = stringResource(R.string.settings_coming_soon)
    val openError = stringResource(R.string.error_link)
    val language = remember {
        Locale.getDefault()
            .getDisplayLanguage(Locale.getDefault())
            .replaceFirstChar { it.uppercase() }
    }

    val showSoon = {
        Toast.makeText(context, soon, Toast.LENGTH_SHORT).show()
    }
    val openUrl: (String) -> Unit = { url ->
        try {
            uriHandler.openUri(url)
        } catch (_: Exception) {
            Toast.makeText(context, openError, Toast.LENGTH_SHORT).show()
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
                text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
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
// ДОДЕЛАЙ ТУТА СНИЗУ
        SettingRow(
            title = stringResource(R.string.settings_theme),
            value = stringResource(R.string.settings_theme_system),
            onClick = showSoon
        )
        SettingRow(
            title = stringResource(R.string.settings_language),
            value = language,
            onClick = showSoon
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
        // ДОДЕЛАЙ ТУТА СНИЗУ, ВМЕСТЕ СКОРЕЕ ВСЕГО БУДЕМ ДЕЛАТЬ
        SettingRow(
            title = stringResource(R.string.settings_updates),
            onClick = showSoon
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
