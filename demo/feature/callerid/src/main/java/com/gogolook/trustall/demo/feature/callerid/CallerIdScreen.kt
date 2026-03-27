package com.gogolook.trustall.demo.feature.callerid

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun CallerIdScreen(
    viewModel: CallerIdViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentCheckPermissions by rememberUpdatedState { viewModel.checkPermissions() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentCheckPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (uiState.permissionDialogRequest != null) {
        val request = uiState.permissionDialogRequest!!
        PermissionRationaleDialog(
            type = request.type,
            openSettings = request.openSettings,
            onDismiss = { viewModel.dismissDialog() },
            onConfirm = {
                viewModel.dismissDialog()
                if (request.openSettings) {
                    activity?.let {
                        when (request.type) {
                            PermissionDialogType.PHONE,
                            PermissionDialogType.CALL_LOG,
                            PermissionDialogType.CONTACT -> {
                                viewModel.launchAppDetailsSettings(it)
                            }
                            PermissionDialogType.SCREENING,
                            PermissionDialogType.REDIRECTION -> {
                                viewModel.launchManageDefaultAppsSettings(it)
                            }
                        }
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        // Permission Status Section
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Permissions & Roles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                PermissionItem(
                    label = "Phone Permissions",
                    isGranted = uiState.hasPhonePermissions,
                    onRequest = { activity?.let { viewModel.requestPhonePermissions(it) } }
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                PermissionItem(
                    label = "Call Log Permissions",
                    isGranted = uiState.hasCallLogPermissions,
                    onRequest = { activity?.let { viewModel.requestCallLogPermissions(it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PermissionItem(
                    label = "Contact Permission",
                    isGranted = uiState.hasContactPermission,
                    onRequest = { activity?.let { viewModel.requestContactPermission(it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PermissionItem(
                    label = "Call Screening Role",
                    isGranted = uiState.isCallScreeningRoleHeld,
                    onRequest = { activity?.let { viewModel.requestCallScreeningRole(it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PermissionItem(
                    label = "Call Redirection Role",
                    isGranted = uiState.isCallRedirectionRoleHeld,
                    onRequest = { activity?.let { viewModel.requestCallRedirectionRole(it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PermissionItem(
                    label = "Overlay Permission",
                    isGranted = uiState.hasOverlayPermission,
                    onRequest = { activity?.let { viewModel.requestOverlayPermission(it) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logs Section
        Text(
            text = "Call Event Logs",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.logs) { log ->
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                
                if (uiState.logs.isEmpty()) {
                    item {
                        Text(
                            text = "Waiting for events...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRationaleDialog(
    type: PermissionDialogType,
    openSettings: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val title = when (type) {
        PermissionDialogType.PHONE -> "Phone Permission Required"
        PermissionDialogType.CALL_LOG -> "Call Log Permission Required"
        PermissionDialogType.CONTACT -> "Contact Permission Required"
        PermissionDialogType.SCREENING -> "Call Screening Role Required"
        PermissionDialogType.REDIRECTION -> "Call Redirection Role Required"
    }

    val text = when (type) {
        PermissionDialogType.PHONE -> "This app needs Phone access to identify callers.${if (openSettings) " Please grant this permission in Settings." else ""}"
        PermissionDialogType.CALL_LOG -> "This app needs Call Log access to history.${if (openSettings) " Please grant this permission in Settings." else ""}"
        PermissionDialogType.CONTACT -> "This app needs Contacts access to show caller names.${if (openSettings) " Please grant this permission in Settings." else ""}"
        PermissionDialogType.SCREENING -> "This app needs to be the default Call Screening app to block spam.${if (openSettings) " Please set it in Default Apps." else ""}"
        PermissionDialogType.REDIRECTION -> "This app needs Call Redirection role to manage calls.${if (openSettings) " Please set it in Default Apps." else ""}"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(if (openSettings) "Go to Settings" else "OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PermissionItem(
    label: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (isGranted) "Granted" else "Missing",
                style = MaterialTheme.typography.labelSmall,
                color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
        
        if (!isGranted) {
            ElevatedButton(
                onClick = onRequest,
                modifier = Modifier.height(36.dp)
            ) {
                Text("Request")
            }
        } else {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Granted",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
