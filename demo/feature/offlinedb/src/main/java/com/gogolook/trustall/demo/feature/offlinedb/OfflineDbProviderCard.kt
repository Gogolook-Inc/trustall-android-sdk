package com.gogolook.trustall.demo.feature.offlinedb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.gogolook.trustall.core.offlinedb.model.OfflineNumberInfo
import com.gogolook.trustall.demo.core.ui.ProviderField
import com.gogolook.trustall.demo.core.ui.ProviderRow
import com.gogolook.trustall.demo.core.ui.ProviderRowsEditor
import com.gogolook.trustall.demo.core.ui.ProviderSection

/** The provider picker for the offline database, shown at the top of the screen. */
@Composable
fun OfflineDbProviderCard() {
    val context = LocalContext.current
    var selection by remember { mutableStateOf(OfflineDbProviders.selection(context)) }
    var entries by remember { mutableStateOf(OfflineDbProviders.entries(context)) }

    ProviderSection(
            isChain = false,
            selection = selection,
            onSelection = {
                OfflineDbProviders.setSelection(context, it)
                selection = it
            }
    ) {
        ProviderRowsEditor(
                rows = entries.map { entry -> ProviderRow(entry.number, entry.summary()) },
                fields = OFFLINE_FIELDS,
                addLabel = "Add number",
                onAdd = { values ->
                    OfflineDbProviders.add(
                            context,
                            CustomOfflineEntry(
                                    number = values["number"].orEmpty(),
                                    name = values["name"].orEmpty(),
                                    spamCategory = values["spamCategory"].orEmpty(),
                                    spamLevel = OfflineNumberInfo.SpamLevel.entries
                                            .first { it.name == values["spamLevel"] },
                            )
                    )
                    entries = OfflineDbProviders.entries(context)
                },
                onRemove = { number ->
                    OfflineDbProviders.remove(context, number)
                    entries = OfflineDbProviders.entries(context)
                }
        )
    }
}

private fun CustomOfflineEntry.summary(): String =
        listOf(name.ifBlank { "(no name)" }, spamLevel.name, spamCategory)
                .filter { it.isNotBlank() }
                .joinToString("  |  ")

private val OFFLINE_FIELDS = listOf(
        ProviderField.Text(
                key = "number",
                label = "Phone number",
                placeholder = "+886912345678",
                required = true,
        ),
        ProviderField.Text(key = "name", label = "Name", placeholder = "Acme Delivery"),
        ProviderField.Text(
                key = "spamCategory",
                label = "Spam category",
                placeholder = "Telemarketing",
        ),
        ProviderField.Choice(
                key = "spamLevel",
                label = "Spam level",
                options = OfflineNumberInfo.SpamLevel.entries.map { it.name },
        ),
)
