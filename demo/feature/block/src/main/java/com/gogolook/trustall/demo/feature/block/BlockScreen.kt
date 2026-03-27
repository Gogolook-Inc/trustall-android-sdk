package com.gogolook.trustall.demo.feature.block

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockScreen(viewModel: BlockViewModel = viewModel(), onNavigateToCallerId: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()
    var numberInput by remember { mutableStateOf("") }
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentCheckPermissions by rememberUpdatedState { viewModel.checkPermissions() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentCheckPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!uiState.isCallScreeningRoleHeld) {
            PermissionWarningBanner(
                    message = "Call Screening Role is required for incoming call blocking to work.",
                    onClick = onNavigateToCallerId
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!uiState.isCallRedirectionRoleHeld) {
            PermissionWarningBanner(
                    message =
                            "Call Redirection Role is required for outgoing call blocking to work.",
                    onClick = onNavigateToCallerId
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Input Section
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                        text = "Manage Block List",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                        value = numberInput,
                        onValueChange = { numberInput = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedButton(
                            onClick = { viewModel.addBlock(numberInput) },
                            modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Block")
                    }

                    ElevatedButton(
                            onClick = { viewModel.checkBlockStatus(numberInput) },
                            modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Check")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ElevatedButton(
                        onClick = { viewModel.removeBlock(numberInput) },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                ButtonDefaults.elevatedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                )
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Unblock")
                }
            }
        }

        // Check Result Display
        if (uiState.checkResult != null) {
            val (num, isBlocked) = uiState.checkResult!!
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                    text = "Status for $num: ${if (isBlocked) "BLOCKED 🚫" else "ALLOWED ✅"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color =
                            if (isBlocked) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Blocked List
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = "Blocked Numbers (${uiState.blockedNumbers.size})",
                    style = MaterialTheme.typography.titleSmall
            )

            TextButton(
                    onClick = { viewModel.clearAll() },
                    enabled = uiState.blockedNumbers.isNotEmpty()
            ) { Text("Clear All") }
        }

        LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.blockedNumbers) { blockInfo ->
                BlockedItem(
                        number = blockInfo.e164,
                        onDelete = { viewModel.removeBlock(blockInfo.e164) }
                )
            }
        }
    }
}

@Composable
fun BlockedItem(number: String, onDelete: () -> Unit) {
    OutlinedCard(shape = RoundedCornerShape(8.dp)) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = number, style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = onDelete) {
                Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun PermissionWarningBanner(message: String, onClick: () -> Unit) {
    OutlinedCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
            )
        }
    }
}
