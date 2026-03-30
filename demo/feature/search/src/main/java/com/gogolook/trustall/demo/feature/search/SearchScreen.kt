package com.gogolook.trustall.demo.feature.search

import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gogolook.trustall.callerid.model.NumberInfo
import com.gogolook.trustall.core.numbersearch.model.OnlineNumberInfo
import com.gogolook.trustall.demo.core.ui.CallerIdOverlay
import com.gogolook.trustall.demo.core.util.isNetworkAvailable
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel = viewModel(), onNavigateToCallerId: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()
    var phoneNumber by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    // Instantiate Overlay
    val overlay = remember { CallerIdOverlay(context) }

    // Permission dialog state
    var showPermissionDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) { onDispose { overlay.onDestroy() } }

    if (showPermissionDialog) {
        AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Permission Required") },
                text = {
                    Text(
                            "Displaying the Caller ID overlay requires 'Draw over other apps' permission. Please enable it in the Caller ID tab."
                    )
                },
                confirmButton = {
                    TextButton(
                            onClick = {
                                showPermissionDialog = false
                                onNavigateToCallerId()
                            }
                    ) { Text("Go to Caller ID") }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) { Text("Cancel") }
                }
        )
    }

    val isLoading = uiState is SearchUiState.Loading

    Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions =
                        KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Search
                        ),
                keyboardActions =
                        KeyboardActions(
                                onSearch = {
                                    if (!context.isNetworkAvailable()) {
                                        Toast.makeText(
                                                        context,
                                                        "No network connection, please check your network settings.",
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                    } else if (phoneNumber.isNotBlank() && !isLoading) {
                                        viewModel.search(phoneNumber)
                                    }
                                }
                        ),
                modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedButton(
                onClick = {
                    if (!context.isNetworkAvailable()) {
                        Toast.makeText(
                                        context,
                                        "No network connection, please check your network settings.",
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                    } else {
                        viewModel.search(phoneNumber)
                    }
                },
                enabled = phoneNumber.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
            if (isLoading) {
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // State-dependent Content
        when (val state = uiState) {
            is SearchUiState.Idle, is SearchUiState.Loading -> {
                // No result or error to show (Loading indicator is in button)
            }
            is SearchUiState.Success -> {
                SearchResultCard(
                        info = state.result,
                        onShowDialog = {
                            if (Settings.canDrawOverlays(context)) {
                                val r = state.result
                                val info = NumberInfo(
                                    number = r.number,
                                    name = r.name,
                                    bizCategory = r.bizCategory,
                                    spamCategory = r.spamCategory,
                                    spamLevel = when (r.spamLevel) {
                                        OnlineNumberInfo.SpamLevel.NONE -> NumberInfo.SpamLevel.NONE
                                        OnlineNumberInfo.SpamLevel.SUSPICIOUS -> NumberInfo.SpamLevel.SUSPICIOUS
                                        OnlineNumberInfo.SpamLevel.TOP -> NumberInfo.SpamLevel.TOP
                                    },
                                    isContact = false
                                )
                                overlay.show(MutableStateFlow(info), r.number)
                            } else {
                                showPermissionDialog = true
                            }
                        }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            is SearchUiState.Error -> {
                ErrorDisplay(error = state.message)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Samples Section (Always visible)
        val sampleInfo = uiState.sampleInfo
        if (sampleInfo.availableCountries.isNotEmpty()) {
            Text(
                    text = "Try a sample number",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Country Chips
            LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
            ) {
                items(sampleInfo.availableCountries) { country ->
                    FilterChip(
                            selected = sampleInfo.selectedCountry == country,
                            onClick = { viewModel.selectCountry(country) },
                            label = { Text(country) },
                            leadingIcon =
                                    if (sampleInfo.selectedCountry == country) {
                                        {
                                            Icon(
                                                    Icons.Rounded.Done,
                                                    contentDescription = null,
                                                    modifier = Modifier.height(18.dp)
                                            )
                                        }
                                    } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sample Numbers
            FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sampleInfo.filteredSamples.forEach { sample ->
                    SampleItem(sample = sample) {
                        phoneNumber = sample.number
                        if (!context.isNetworkAvailable()) {
                            Toast.makeText(
                                            context,
                                            "No network connection, please check your network settings.",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                        } else {
                            viewModel.search(sample.number)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SampleItem(sample: SampleNumber, onClick: () -> Unit) {
    Surface(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                    text = sample.number,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (sample.category.isNotEmpty()) {
                Text(
                        text = sample.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun SearchResultCard(info: OnlineNumberInfo, onShowDialog: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Search Result",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                androidx.compose.material3.TextButton(onClick = onShowDialog) {
                    Text("Show call dialog")
                }
            }

            InfoItem(label = "Number", value = info.number)
            InfoItem(label = "Name", value = info.name)
            InfoItem(label = "Biz Category", value = info.bizCategory)
            InfoItem(label = "Spam Category", value = info.spamCategory)
            InfoItem(label = "Spam Level", value = info.spamLevel.toString())
        }
    }
}

@Composable
fun ErrorDisplay(error: String) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
    ) {
        Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error
        )
        Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
        )
        Text(
                text = value.ifEmpty { "N/A" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
        )
    }
}
