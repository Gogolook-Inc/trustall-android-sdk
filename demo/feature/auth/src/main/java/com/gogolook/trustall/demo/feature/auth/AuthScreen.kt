package com.gogolook.trustall.demo.feature.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gogolook.trustall.demo.core.util.isNetworkAvailable

@Composable
fun AuthScreen(viewModel: AuthViewModel = viewModel()) {
    LaunchedEffect(Unit) { viewModel.checkStatus() }

    val uiState by viewModel.uiState.collectAsState()

    Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
    ) {
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
            }
            is AuthUiState.NotRegistered -> {
                NotRegisteredContent(onRegisterClick = { viewModel.register() })
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
            InfoItem(label = "Device ID", value = uiState.deviceId)
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
fun NotRegisteredContent(onRegisterClick: () -> Unit) {
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
                text = "Status: Not Registered",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
