package com.gogolook.trustall.demo.feature.urlscan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.gogolook.trustall.core.urlscan.model.Level
import com.gogolook.trustall.demo.core.ui.ProviderField
import com.gogolook.trustall.demo.core.ui.ProviderRow
import com.gogolook.trustall.demo.core.ui.ProviderRowsEditor
import com.gogolook.trustall.demo.core.ui.ProviderSection

/** The provider picker for URL scanning, shown at the top of the screen. */
@Composable
fun UrlScanProviderCard() {
    val context = LocalContext.current
    var selection by remember { mutableStateOf(UrlScanProviders.selection(context)) }
    var entries by remember { mutableStateOf(UrlScanProviders.entries(context)) }

    ProviderSection(
            isChain = true,
            selection = selection,
            onSelection = {
                UrlScanProviders.setSelection(context, it)
                selection = it
            }
    ) {
        ProviderRowsEditor(
                rows = entries.map { entry -> ProviderRow(entry.url, entry.summary()) },
                fields = URL_FIELDS,
                addLabel = "Add URL",
                onAdd = { values ->
                    UrlScanProviders.add(
                            context,
                            CustomUrlEntry(
                                    url = values["url"].orEmpty(),
                                    level = Level.entries.first { it.name == values["level"] },
                                    failWith = values["failWith"].orEmpty(),
                            )
                    )
                    entries = UrlScanProviders.entries(context)
                },
                onRemove = { url ->
                    UrlScanProviders.remove(context, url)
                    entries = UrlScanProviders.entries(context)
                }
        )
    }
}

private fun CustomUrlEntry.summary(): String =
        if (failWith.isNotBlank()) "Error: $failWith" else "Success: ${level.name}"

private val URL_FIELDS = listOf(
        ProviderField.Text(
                key = "url",
                label = "URL",
                placeholder = "https://example.com",
                required = true,
        ),
        ProviderField.Choice(
                key = "level",
                label = "Verdict",
                options = Level.entries.map { it.name },
        ),
        ProviderField.Text(
                key = "failWith",
                label = "Fail instead, with this message",
                placeholder = "Leave empty to answer with the verdict",
        ),
)
