package com.gogolook.trustall.demo.feature.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gogolook.trustall.demo.core.util.SdkBootstrap
import com.gogolook.trustall.demo.core.util.isNetworkAvailable

@Composable
fun AuthScreen(viewModel: AuthViewModel = viewModel()) {
    LaunchedEffect(Unit) { viewModel.checkStatus() }

    val uiState by viewModel.uiState.collectAsState()
    val notice by viewModel.notice.collectAsState()
    val scrollState = rememberScrollState()

    Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
    ) {
        notice?.let { text ->
            ReinitNotice(text = text, onDismiss = { viewModel.dismissNotice() })
            Spacer(modifier = Modifier.height(16.dp))
        }

        when (val state = uiState) {
            is AuthUiState.Loading -> {
                CircularProgressIndicator()
            }
            is AuthUiState.Registered -> {
                RegisteredInfoCard(uiState = state)
                Spacer(modifier = Modifier.height(24.dp))
                MemberIdModificationSection(
                        onUpdateMemberId = { newId -> viewModel.updateMemberId(newId) }
                )
                Spacer(modifier = Modifier.height(24.dp))
                DeviceIdModificationSection(
                        onGenerate = { viewModel.randomDeviceId() },
                        onApply = { newId -> viewModel.applyDeviceId(newId) }
                )

            }
            is AuthUiState.NotRegistered -> {
                NotRegisteredContent(
                        uiState = state,
                        onRegisterClick = { viewModel.register() }
                )
                Spacer(modifier = Modifier.height(24.dp))
                // Also offered here, not only once registered. Changing the device ID is what
                // lands the screen in this state, so leaving the editor behind in the other
                // branch would make the change a one-way door until the user registers again.
                DeviceIdModificationSection(
                        onGenerate = { viewModel.randomDeviceId() },
                        onApply = { newId -> viewModel.applyDeviceId(newId) }
                )
            }
            is AuthUiState.Error -> {
                ErrorContent(error = state.message, onRetry = { viewModel.dismissError() })
            }
        }
    }
}

@Composable
fun RegisteredInfoCard(uiState: AuthUiState.Registered) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                    text = "Registration Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                    text = "Registered",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
            )

            InfoItem(label = "Region", value = uiState.region)
            DeviceIdInfo(deviceId = uiState.deviceId, authDeviceId = uiState.authDeviceId)
            InfoItem(label = "Member ID", value = uiState.memberId)
            InfoItem(label = "User ID", value = uiState.userId)
        }
    }
}

@Composable
fun MemberIdModificationSection(onUpdateMemberId: (String) -> Unit) {
    var newMemberId by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
                text = "Modify Member ID",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
                value = newMemberId,
                onValueChange = { newMemberId = it },
                label = { Text("New Member ID") },
                placeholder = { Text("Enter custom ID") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedButton(
                onClick = {
                    if (newMemberId.isNotBlank()) {
                        onUpdateMemberId(newMemberId)
                        newMemberId = "" // Reset field
                    }
                },
                enabled = newMemberId.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
        ) { Text("Update Member ID") }
    }
}

@Composable
fun ReinitNotice(text: String, onDismiss: () -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}

@Composable
fun DeviceIdModificationSection(onGenerate: () -> String, onApply: (String) -> Unit) {
    var newDeviceId by remember { mutableStateOf("") }
    val trimmed = newDeviceId.trim()
    val isValid = SdkBootstrap.DEVICE_ID_FORMAT.matches(trimmed)
    val showFormatError = trimmed.isNotEmpty() && !isValid

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
                text = "Modify Device ID",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
                text =
                        "Supplied through SdkConfig.deviceId, which the SDK reads once per " +
                                "Trustall.initialize(). Applying runs initialize() again right " +
                                "here, so the new ID takes effect without a relaunch. It also " +
                                "discards the registration made under the old ID, and this " +
                                "screen registers again straight after. What a second " +
                                "initialize does not do is rebuild providers or the network " +
                                "stacks behind them — use the restart button in the top bar to " +
                                "compare against a clean start.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
                value = newDeviceId,
                onValueChange = { newDeviceId = it },
                label = { Text("New Device ID") },
                placeholder = { Text("32 lowercase hex characters") },
                singleLine = true,
                isError = showFormatError,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
        )

        if (showFormatError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                    text =
                            "Needs exactly 32 lowercase hex characters, a UUID without its " +
                                    "dashes. Entered ${trimmed.length}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                    onClick = { newDeviceId = onGenerate() },
                    modifier = Modifier.weight(1f)
            ) { Text("Generate") }

            ElevatedButton(
                    onClick = {
                        onApply(trimmed)
                        newDeviceId = ""
                    },
                    enabled = isValid,
                    modifier = Modifier.weight(1f)
            ) { Text("Apply and re-initialize") }
        }
    }
}

@Composable
fun NotRegisteredContent(uiState: AuthUiState.NotRegistered, onRegisterClick: () -> Unit) {
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                        text = "Registration Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                        text = "Not registered",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                )

                DeviceIdInfo(
                        deviceId = uiState.deviceId,
                        authDeviceId = uiState.authDeviceId
                )
            }
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
                        onRegisterClick()
                    }
                }
        ) { Text("Register Now") }
    }
}

@Composable
fun ErrorContent(error: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedButton(onClick = onRetry) { Text("Retry / Dismiss") }
    }
}

/**
 * The device identifier, and what the auth layer would actually send if the two ever disagree.
 *
 * The second row is a canary rather than normal information. `TrustallAuth` snapshots the
 * identifier when it is built, and the SDK drops that instance whenever it discards a
 * registration, so the snapshot should always be current. If this row appears, that stopped
 * being true.
 */
@Composable
private fun DeviceIdInfo(deviceId: String, authDeviceId: String) {
    InfoItem(label = "Device ID", value = deviceId)
    if (authDeviceId != deviceId) {
        InfoItem(label = "Device ID (auth layer sends)", value = authDeviceId)
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
