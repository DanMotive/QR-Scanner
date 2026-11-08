package com.danmotive.qrscanner.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.danmotive.qrscanner.R
import com.danmotive.qrscanner.ui.components.PlaceholderContent

@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    PlaceholderContent(
        icon = Icons.Default.Search,
        title = stringResource(R.string.scan_placeholder_title),
        description = stringResource(R.string.scan_placeholder_description),
        modifier = modifier
    )
}
