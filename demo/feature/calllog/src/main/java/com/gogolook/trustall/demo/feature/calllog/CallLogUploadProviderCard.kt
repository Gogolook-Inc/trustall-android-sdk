package com.gogolook.trustall.demo.feature.calllog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gogolook.trustall.demo.core.ui.ProviderSection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The provider picker for call log upload, shown at the top of the screen.
 *
 * The rows here are not editable, unlike the other three seams. This one receives data rather
 * than answering questions, so there is nothing to pre-populate: the list is what the custom
 * provider captured instead of sending it to Gogolook.
 */
@Composable
fun CallLogUploadProviderCard() {
    val context = LocalContext.current
    var selection by remember { mutableStateOf(CallLogUploadProviders.selection(context)) }
    var captured by remember { mutableStateOf(CallLogUploadProviders.captured(context)) }

    ProviderSection(
            isChain = false,
            selection = selection,
            onSelection = {
                CallLogUploadProviders.setSelection(context, it)
                selection = it
            }
    ) {
        Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HorizontalDivider()

            Text(
                    text = "Captured instead of uploaded",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
            )

            if (captured.isEmpty()) {
                Text(
                        text = "Nothing yet. Upload from this screen and the records land here " +
                                "rather than at Gogolook.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val format = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
                captured.asReversed().forEach { record ->
                    Text(
                            text = record.number + "  ·  " + format.format(Date(record.date)),
                            style = MaterialTheme.typography.bodySmall
                    )
                }
                OutlinedButton(
                        onClick = {
                            CallLogUploadProviders.clearCaptured(context)
                            captured = CallLogUploadProviders.captured(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                ) { Text("Clear captured records") }
            }
        }
    }
}
