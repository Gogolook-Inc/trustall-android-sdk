package com.gogolook.trustall.demo.feature.offlinedb

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gogolook.trustall.core.offlinedb.model.DownloadState
import com.gogolook.trustall.core.offlinedb.model.OfflineDbProfile
import com.gogolook.trustall.core.offlinedb.model.OfflineNumberInfo
import com.gogolook.trustall.demo.core.util.isNetworkAvailable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineDbScreen(viewModel: OfflineDbViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // --- Database Status Section ---
        Text(
                text = "Database Management",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        DbStatusCard(profile = uiState.dbProfile)

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        val isDownloading = uiState.downloadState is DownloadState.Downloading

        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                            viewModel.downloadDb()
                        }
                    },
                    enabled = !isDownloading,
                    modifier = Modifier.weight(1f)
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (uiState.dbProfile == null) "Download DB" else "Check Update")
            }

            OutlinedButton(
                    onClick = { viewModel.clearDb() },
                    enabled = !isDownloading && uiState.dbProfile != null,
                    colors =
                            ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                            ),
                    modifier = Modifier.weight(1f)
            ) { Text("Clear DB") }
        }

        // Download Status / Progress
        uiState.downloadState?.let { state ->
            Spacer(modifier = Modifier.height(16.dp))
            DownloadStatusDisplay(state)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Offline Search Section ---
        Text(
                text = "Test Offline Search",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
                text = "Search numbers without network connection (Airplane mode).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OfflineSearchSection(uiState = uiState, onSearch = { viewModel.search(it) })

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DbStatusCard(profile: OfflineDbProfile?) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                    text = "Current Database Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (profile != null) {
                StatusItem("Version", profile.version.toString())
                StatusItem("Spam Numbers", profile.spamNumSize.toString())
                StatusItem("Top Numbers", profile.topNumSize.toString())
                StatusItem("Top Spam", profile.toptopSpamSize.toString())

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                            "Database is Active",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Green
                    )
                }
            } else {
                Text("No Offline Database found.", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun DownloadStatusDisplay(state: DownloadState) {
    when (state) {
        is DownloadState.Downloading -> {
            Column {
                Text(
                        "Downloading... ${state.progress}%",
                        style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                        progress = { state.progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        is DownloadState.Finished -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Completed!", color = MaterialTheme.colorScheme.primary)
            }
        }
        is DownloadState.Failed -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Failed: ${state.reason}, ${state.error}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun OfflineSearchSection(uiState: OfflineDbUiState, onSearch: (String) -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }

    Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions =
                        KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Search
                        ),
                keyboardActions = KeyboardActions(onSearch = { onSearch(phoneNumber) })
        )

        ElevatedButton(
                onClick = { onSearch(phoneNumber) },
                enabled = phoneNumber.isNotBlank() && !uiState.isSearching,
                modifier = Modifier.height(56.dp) // Match text field height roughly
        ) { Icon(Icons.Default.Search, contentDescription = "Search") }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (uiState.searchResult != null) {
        OfflineSearchResultCard(info = uiState.searchResult!!)
    }

    if (uiState.searchError != null) {
        Text(uiState.searchError!!, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun OfflineSearchResultCard(info: OfflineNumberInfo) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                    text = "Offline Result",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatusItem("Number", info.number)
            StatusItem("Name", info.name)
            StatusItem("Spam Category", info.spamCategory)
            StatusItem("Spam Level", info.spamLevel.toString())
        }
    }
}

@Composable
fun StatusItem(label: String, value: String) {
    Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
                value.ifEmpty { "N/A" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
        )
    }
}
