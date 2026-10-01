package com.gogolook.trustall.demo.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gogolook.trustall.demo.core.util.ProviderSelection

/**
 * The provider picker that sits at the top of a feature screen.
 *
 * [isChain] decides both the control and what is representable. Number search and URL scan take an
 * ordered list, so the two boxes are independent: both on means the custom provider is asked first
 * and the SDK's own answers what it does not cover, and neither on means the SDK has no source at
 * all. The offline database and call log upload take a single provider, so the choice is exclusive
 * and one of them is always on.
 *
 * Every change applies at once — the SDK reads the registered provider on each call, including
 * the one it is about to make. Swapping the built-in provider out after it has already run works
 * the same way; what it cannot undo is that those earlier calls reached Gogolook, which is a
 * decision for the app to get right rather than something the SDK objects to.
 */
@Composable
fun ProviderSection(
        isChain: Boolean,
        selection: ProviderSelection,
        onSelection: (ProviderSelection) -> Unit,
        customContent: @Composable () -> Unit,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Provider",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                )
                Text(
                        text = if (isChain) "setProviders(list)" else "setProvider(one)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                    text = "Changes apply immediately, to the next call and every one after " +
                            "it. Anything already looked up through the default provider has " +
                            "gone to Gogolook and stays there.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isChain) {
                Toggle(
                        label = "Custom provider",
                        checked = selection.custom,
                        isChain = true,
                        onClick = { onSelection(selection.copy(custom = !selection.custom)) }
                )
                Toggle(
                        label = "Default provider (Gogolook)",
                        checked = selection.default,
                        isChain = true,
                        onClick = { onSelection(selection.copy(default = !selection.default)) }
                )
                if (!selection.custom && !selection.default) {
                    Text(
                            text = "No source at all: every lookup returns nothing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Toggle(
                        label = "Custom provider",
                        checked = selection.custom,
                        isChain = false,
                        onClick = { onSelection(ProviderSelection(custom = true, default = false)) }
                )
                Toggle(
                        label = "Default provider (Gogolook)",
                        checked = !selection.custom,
                        isChain = false,
                        onClick = { onSelection(ProviderSelection(custom = false, default = true)) }
                )
            }

            if (selection.custom) {
                customContent()
            }
        }
    }
}

@Composable
private fun Toggle(label: String, checked: Boolean, isChain: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (isChain) {
            Checkbox(checked = checked, onCheckedChange = { onClick() })
        } else {
            RadioButton(selected = checked, onClick = onClick)
        }
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
