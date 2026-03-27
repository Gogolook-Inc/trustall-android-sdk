package com.gogolook.trustall.demo.feature.calllog

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallMade
import androidx.compose.material.icons.rounded.CallMissed
import androidx.compose.material.icons.rounded.CallReceived
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gogolook.trustall.calllog.model.CallType
import com.gogolook.trustall.calllog.callLog
import com.gogolook.trustall.core.Trustall
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallLogScreen(
    viewModel: CallLogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val autoUploadState by viewModel.autoUploadState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkAndLoadLogs()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is CallLogUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is CallLogUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.checkAndLoadLogs() }) {
                        Text("Retry")
                    }
                }
            }
            is CallLogUiState.NeedPermission -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Call Log Permission Required")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val activity = context as? ComponentActivity
                        if (activity != null) viewModel.requestPermissions(activity)
                    }) {
                        Text("Grant Permission")
                    }
                }
            }
            is CallLogUiState.Success -> {
                if (state.logs.isEmpty()) {
                    Text(
                        text = "No call logs found.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.logs, key = { it.callLog.id }) { item ->
                            CallLogItemCard(
                                item = item,
                                onUpload = { viewModel.uploadCallLog(item.callLog) }
                            )
                        }
                    }
                }
            }
            else -> {}
        }

        AutoUploadFab(
            state = autoUploadState,
            onClick = { viewModel.autoUploadCallLogs() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
fun AutoUploadFab(
    state: UploadState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading = state is UploadState.Loading
    ExtendedFloatingActionButton(
        onClick = { if (!isLoading) onClick() },
        icon = {
            when (state) {
                is UploadState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current,
                )
                is UploadState.Success -> Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                else -> Icon(Icons.Rounded.CloudUpload, contentDescription = null)
            }
        },
        text = {
            Text(
                when (state) {
                    is UploadState.Loading -> "Uploading…"
                    is UploadState.Success -> "Uploaded"
                    is UploadState.Error -> "Failed"
                    else -> "Auto Upload"
                }
            )
        },
        containerColor = when (state) {
            is UploadState.Success -> MaterialTheme.colorScheme.secondaryContainer
            is UploadState.Error -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.primaryContainer
        },
        modifier = modifier,
    )
}

@Composable
fun CallLogItemCard(item: CallLogUiModel, onUpload: () -> Unit) {
    val log = item.callLog
    val info = item.numberInfo

    val isSpam = info != null && info.spamLevel > 0
    val cardColor = if (isSpam) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (log.type) {
                    CallType.INCOMING -> Icons.Rounded.CallReceived
                    CallType.OUTGOING -> Icons.Rounded.CallMade
                    CallType.MISSED -> Icons.Rounded.CallMissed
                    CallType.REJECTED -> Icons.Rounded.Block
                    CallType.BLOCKED -> Icons.Rounded.Block
                    CallType.UNKNOWN -> Icons.Rounded.Call
                }
                val iconTint = when (log.type) {
                    CallType.MISSED, CallType.REJECTED, CallType.BLOCKED -> MaterialTheme.colorScheme.error
                    CallType.INCOMING -> MaterialTheme.colorScheme.primary
                    CallType.OUTGOING -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = iconTint.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Call Type",
                        tint = iconTint,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val displayName = info?.name?.takeIf { it.isNotBlank() }
                        ?: log.cacheName.takeIf { it.isNotBlank() }
                        ?: "Unknown Caller"
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSpam) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = log.number,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSpam) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                    Text(
                        text = dateFormat.format(Date(log.date)),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSpam) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (log.duration > 0 || log.type == CallType.INCOMING || log.type == CallType.OUTGOING) {
                        Text(
                            text = "${log.duration}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSpam) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            if (info != null && (info.isContact || info.bizCategory.isNotBlank() || info.spamCategory.isNotBlank())) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = (if (isSpam) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.outlineVariant).copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (info.isContact) {
                        TagItem(text = "Contact", icon = Icons.Rounded.AccountCircle, isError = false)
                    }
                    if (info.bizCategory.isNotBlank()) {
                        TagItem(text = info.bizCategory, icon = Icons.Rounded.Business, isError = false)
                    }
                    if (info.spamCategory.isNotBlank()) {
                        TagItem(text = info.spamCategory, icon = Icons.Rounded.Warning, isError = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = (if (isSpam) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.outlineVariant).copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(4.dp))

            UploadCta(state = item.uploadState, onUpload = onUpload)
        }
    }
}

@Composable
private fun UploadCta(state: UploadState, onUpload: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (state) {
            is UploadState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Uploading…", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            is UploadState.Success -> {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Uploaded", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            is UploadState.Error -> {
                Text(state.message, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onUpload, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                    Text("Retry", style = MaterialTheme.typography.labelSmall)
                }
            }
            is UploadState.Idle -> {
                TextButton(onClick = onUpload, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                    Icon(Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun TagItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isError: Boolean = false) {
    val containerColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isError) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSecondaryContainer

    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = contentColor)
            Text(text, style = MaterialTheme.typography.labelSmall, color = contentColor)
        }
    }
}
