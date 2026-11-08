package com.danmotive.qrscanner.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.danmotive.qrscanner.R
import com.danmotive.qrscanner.ui.components.PlaceholderContent

@Composable
fun GeneratorScreen(modifier: Modifier = Modifier) {
    PlaceholderContent(
        icon = Icons.Default.Add,
        title = stringResource(R.string.generator_placeholder_title),
        description = stringResource(R.string.generator_placeholder_description),
        modifier = modifier
    )
}
