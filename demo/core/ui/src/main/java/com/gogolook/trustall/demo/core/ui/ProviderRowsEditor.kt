package com.gogolook.trustall.demo.core.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * One field of a custom provider's answer.
 *
 * The seams answer with different models — a phone number carries a name, a business category and
 * a spam level; a URL carries a verdict — so the dialog is described rather than hard-coded, and
 * each feature converts the filled-in map back into its own type.
 */
sealed interface ProviderField {

    /** The map key this field fills in. */
    val key: String

    /** What the dialog calls it. */
    val label: String

    /** Free text. [required] fields gate the confirm button. */
    data class Text(
            override val key: String,
            override val label: String,
            val placeholder: String = "",
            val required: Boolean = false,
    ) : ProviderField

    /** A fixed set, rendered as chips. The first option is the default. */
    data class Choice(
            override val key: String,
            override val label: String,
            val options: List<String>,
    ) : ProviderField
}

/**
 * A row the custom provider serves.
 *
 * [key] is what the SDK looks up and what removal addresses; [summary] is the one-line rendering
 * of everything else the row carries.
 */
data class ProviderRow(val key: String, val summary: String)

/**
 * Lists the rows a custom provider answers from, and adds new ones through a dialog.
 *
 * Rows are read on every lookup, so an edit here reaches the very next one.
 *
 * @param fields what the add dialog asks for; the first is the row's key
 */
@Composable
fun ProviderRowsEditor(
        rows: List<ProviderRow>,
        fields: List<ProviderField>,
        addLabel: String = "Add entry",
        onAdd: (Map<String, String>) -> Unit,
        onRemove: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider()

        Text(
                text = "Rows the custom provider answers from",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
        )

        if (rows.isEmpty()) {
            Text(
                    text = "Empty, so it answers nothing and every lookup falls through.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            rows.forEach { row ->
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = row.key, style = MaterialTheme.typography.bodyMedium)
                        Text(
                                text = row.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onRemove(row.key) }) {
                        Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Remove")
                    }
                }
            }
        }

        ElevatedButton(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
        ) { Text(addLabel) }
    }

    if (showDialog) {
        ProviderEntryDialog(
                title = addLabel,
                fields = fields,
                onDismiss = { showDialog = false },
                onConfirm = { values ->
                    showDialog = false
                    onAdd(values)
                }
        )
    }
}

@Composable
private fun ProviderEntryDialog(
        title: String,
        fields: List<ProviderField>,
        onDismiss: () -> Unit,
        onConfirm: (Map<String, String>) -> Unit,
) {
    // Seeded so a Choice always has a value even if the user never touches its chips.
    val values = remember(fields) {
        mutableStateMapOf<String, String>().apply {
            fields.forEach { field ->
                put(field.key, if (field is ProviderField.Choice) field.options.first() else "")
            }
        }
    }

    val canConfirm = fields.all { field ->
        field !is ProviderField.Text || !field.required || values[field.key]?.isNotBlank() == true
    }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                Column(
                        modifier = Modifier.fillMaxWidth()
                                .heightIn(max = 420.dp)
                                .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    fields.forEach { field ->
                        when (field) {
                            is ProviderField.Text ->
                                OutlinedTextField(
                                        value = values[field.key].orEmpty(),
                                        onValueChange = { values[field.key] = it },
                                        label = { Text(field.label) },
                                        placeholder = {
                                            if (field.placeholder.isNotEmpty()) {
                                                Text(field.placeholder)
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                )
                            is ProviderField.Choice ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                            text = field.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.horizontalScroll(
                                                    rememberScrollState()
                                            )
                                    ) {
                                        field.options.forEach { option ->
                                            FilterChip(
                                                    selected = values[field.key] == option,
                                                    onClick = { values[field.key] = option },
                                                    label = { Text(option) }
                                            )
                                        }
                                    }
                                }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                        onClick = { onConfirm(values.mapValues { it.value.trim() }) },
                        enabled = canConfirm
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
