package com.gogolook.trustall.demo.feature.urlscan

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gogolook.trustall.core.urlscan.model.Level
import com.gogolook.trustall.core.urlscan.model.UrlScanResult
import com.gogolook.trustall.demo.core.util.isNetworkAvailable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UrlScanScreen(viewModel: UrlScanViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var url by remember { mutableStateOf("") }
    var allowCache by remember { mutableStateOf(false) }
    var cacheDuration by remember { mutableStateOf("1") }
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(expanded) {
        if (expanded) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }

    Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = {}) {
            OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL to Scan") },
                    placeholder = { Text("e.g. google.com") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    keyboardOptions =
                            KeyboardOptions(
                                    keyboardType = KeyboardType.Uri,
                                    imeAction = ImeAction.Go
                            ),
                    keyboardActions =
                            KeyboardActions(
                                    onGo = {
                                        if (!context.isNetworkAvailable()) {
                                            Toast.makeText(
                                                            context,
                                                            "No network connection, please check your network settings.",
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                        } else {
                                            viewModel.scan(
                                                    url,
                                                    allowCache,
                                                    cacheDuration.toIntOrNull()
                                            )
                                        }
                                    }
                            ),
                    trailingIcon = {
                        IconButton(
                                onClick = {
                                    focusManager.clearFocus(force = true)
                                    keyboardController?.hide()
                                    expanded = !expanded
                                }
                        ) { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                SampleUrls.urls.forEach { sampleUrl ->
                    DropdownMenuItem(
                            text = { Text(sampleUrl) },
                            onClick = {
                                if (!context.isNetworkAvailable()) {
                                    Toast.makeText(
                                                    context,
                                                    "No network connection, please check your network settings.",
                                                    Toast.LENGTH_SHORT
                                            )
                                            .show()
                                } else {
                                    url = sampleUrl
                                    expanded = false
                                    viewModel.scan(
                                            sampleUrl,
                                            allowCache,
                                            cacheDuration.toIntOrNull()
                                    )
                                }
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Allow Cache", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = allowCache, onCheckedChange = { allowCache = it })
        }

        if (allowCache) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                    value = cacheDuration,
                    onValueChange = { if (it.all { char -> char.isDigit() }) cacheDuration = it },
                    label = { Text("Cache Duration (minutes)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
            )
        }

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
                        viewModel.scan(url, allowCache, cacheDuration.toIntOrNull())
                    }
                },
                enabled = url.isNotBlank() && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Scan URL")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Spacer(modifier = Modifier.height(32.dp))

        // Result Section
        if (uiState.result != null) {
            ScanResultCard(result = uiState.result!!)
        }

        if (uiState.error != null) {
            ErrorDisplay(error = uiState.error!!)
        }
    }
}

@Composable
fun ScanResultCard(result: UrlScanResult.Success) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                    text = "Scan Result",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            InfoItem("URL", result.url)
            Spacer(modifier = Modifier.height(8.dp))

            val levelColor = getLevelColor(result.level)
            Column {
                Text(
                        "Risk Level",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                )
                Text(
                        text = result.level.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                )
            }
        }
    }
}

private fun getLevelColor(level: Level): Color {
    return when (level) {
        Level.SAFE -> Color.Green
        Level.MALICIOUS -> Color.Red
        Level.SUSPICIOUS -> Color(0xFFFFA500) // Orange
        Level.UNDEFINED -> Color.Gray
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
        )
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
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
                imageVector = Icons.Default.Warning,
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
