package com.danmotive.qrscanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.danmotive.qrscanner.R
import com.danmotive.qrscanner.ui.core.isWebUrl

@Composable
fun ResultCard(
    value: String,
    onOpenLink: () -> Unit,
    onCopy: () -> Unit,
    onScanAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.result_format, value))

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isWebUrl(value)) {
                    Button(onClick = onOpenLink) {
                        Text(stringResource(R.string.open_link))
                    }
                }
                FilledTonalButton(onClick = onCopy) {
                    Text(stringResource(R.string.copy))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onScanAgain) {
                Text(stringResource(R.string.scan_again))
            }
        }
    }
}
