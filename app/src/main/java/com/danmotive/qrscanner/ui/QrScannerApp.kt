package com.danmotive.qrscanner.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.danmotive.qrscanner.R
import com.danmotive.qrscanner.ui.screens.GeneratorScreen
import com.danmotive.qrscanner.ui.screens.OtherScreen
import com.danmotive.qrscanner.ui.screens.ScanScreen
import com.danmotive.qrscanner.ui.theme.QRScannerTheme

/**
 * Вкладки нижней навигации.
 *
 * Иконки — плейсхолдеры, позже их можно заменить на собственные
 * векторные иконки (например, QrCodeScanner из material-icons-extended).
 */
enum class AppTab(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector
) {
    Scan(R.string.tab_scan, Icons.Default.Search),
    Generator(R.string.tab_generate, Icons.Default.Add),
    Other(R.string.tab_other, Icons.Default.MoreVert)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerApp(modifier: Modifier = Modifier) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Scan) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) }
            )
        },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null
                            )
                        },
                        label = { Text(text = stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.Scan -> ScanScreen()
                AppTab.Generator -> GeneratorScreen()
                AppTab.Other -> OtherScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QrScannerAppPreview() {
    QRScannerTheme {
        QrScannerApp()
    }
}
