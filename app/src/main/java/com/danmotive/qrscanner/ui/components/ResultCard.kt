package com.danmotive.qrscanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.result_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            SelectionContainer {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isWebUrl(value)) {
                    Button(
                        onClick = onOpenLink,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.open_link))
                    }
                }
                FilledTonalButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.copy))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onScanAgain,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.scan_again))
            }
        }
    }
}
