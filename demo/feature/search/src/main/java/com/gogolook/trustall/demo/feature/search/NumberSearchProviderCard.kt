package com.gogolook.trustall.demo.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.gogolook.trustall.core.numbersearch.model.OnlineNumberInfo
import com.gogolook.trustall.demo.core.ui.ProviderField
import com.gogolook.trustall.demo.core.ui.ProviderRow
import com.gogolook.trustall.demo.core.ui.ProviderRowsEditor
import com.gogolook.trustall.demo.core.ui.ProviderSection

/** The provider picker for number search, shown at the top of the screen. */
@Composable
fun NumberSearchProviderCard() {
    val context = LocalContext.current
    var selection by remember { mutableStateOf(NumberSearchProviders.selection(context)) }
    var entries by remember { mutableStateOf(NumberSearchProviders.entries(context)) }

    ProviderSection(
            isChain = true,
            selection = selection,
            onSelection = {
                NumberSearchProviders.setSelection(context, it)
                selection = it
            }
    ) {
        ProviderRowsEditor(
                rows = entries.map { entry -> ProviderRow(entry.number, entry.summary()) },
                fields = NUMBER_FIELDS,
                addLabel = "Add number",
                onAdd = { values ->
                    NumberSearchProviders.add(
                            context,
                            CustomNumberEntry(
                                    number = values["number"].orEmpty(),
                                    name = values["name"].orEmpty(),
                                    bizCategory = values["bizCategory"].orEmpty(),
                                    spamCategory = values["spamCategory"].orEmpty(),
                                    spamLevel = OnlineNumberInfo.SpamLevel.entries
                                            .first { it.name == values["spamLevel"] },
                            )
                    )
                    entries = NumberSearchProviders.entries(context)
                },
                onRemove = { number ->
                    NumberSearchProviders.remove(context, number)
                    entries = NumberSearchProviders.entries(context)
                }
        )
    }
}

private fun CustomNumberEntry.summary(): String =
        listOf(name.ifBlank { "(no name)" }, spamLevel.name, bizCategory, spamCategory)
                .filter { it.isNotBlank() }
                .joinToString("  |  ")

private val NUMBER_FIELDS = listOf(
        ProviderField.Text(
                key = "number",
                label = "Phone number (E.164)",
                placeholder = "+886912345678",
                required = true,
        ),
        ProviderField.Text(key = "name", label = "Name", placeholder = "Acme Delivery"),
        ProviderField.Text(
                key = "bizCategory",
                label = "Business category",
                placeholder = "Courier",
        ),
        ProviderField.Text(
                key = "spamCategory",
                label = "Spam category",
                placeholder = "Telemarketing",
        ),
        ProviderField.Choice(
                key = "spamLevel",
                label = "Spam level",
                options = OnlineNumberInfo.SpamLevel.entries.map { it.name },
        ),
)
