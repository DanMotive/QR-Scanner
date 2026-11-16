package com.danmotive.qrscanner.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.danmotive.qrscanner.R

@Composable
fun OtherScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize())

    Row {
        Text("в разработке")
    }
}
