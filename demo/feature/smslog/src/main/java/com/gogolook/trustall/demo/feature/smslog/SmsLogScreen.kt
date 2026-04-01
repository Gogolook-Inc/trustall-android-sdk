package com.gogolook.trustall.demo.feature.smslog

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CallReceived
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Mms
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gogolook.trustall.callerid.model.NumberInfo
import com.gogolook.trustall.core.Trustall
import com.gogolook.trustall.msgfilter.model.FilterType
import com.gogolook.trustall.smslog.model.SmsLog
import com.gogolook.trustall.smslog.smsLog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SmsLogScreen(
    viewModel: SmsLogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkAndLoadLogs()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is SmsLogUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is SmsLogUiState.Error -> {
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
            is SmsLogUiState.NeedPermission -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "SMS Log Permission Required")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val activity = context as? ComponentActivity
                        if (activity != null) viewModel.requestPermissions(activity)
                    }) {
                        Text("Grant Permission")
                    }
                }
            }
            is SmsLogUiState.Success -> {
                if (state.logs.isEmpty()) {
                    Text(
                        text = "No SMS logs found.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.logs, key = { it.smsLog.id }) { item ->
                            SmsLogItemCard(item)
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun SmsLogItemCard(item: SmsLogUiModel) {
    val log = item.smsLog
    val info = item.numberInfo
    val filterType = item.filterType

    val isSpamNumber = info?.spamLevel != NumberInfo.SpamLevel.UNLIKELY
    val isSpamMessage = filterType == FilterType.SPAM
    val isSpam = isSpamNumber || isSpamMessage
    
    val cardColor = if (isSpam) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon based on SMS type
                val icon = if (log.isIncoming) Icons.Rounded.CallReceived
                    else if (log.isOutgoing) Icons.Rounded.Send
                    else Icons.Rounded.Sms
                val iconTint = if (log.isIncoming) MaterialTheme.colorScheme.primary
                    else if (log.isOutgoing) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = iconTint.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "SMS Type",
                        tint = iconTint,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val displayName = info?.name?.takeIf { it.isNotBlank() } ?: log.displayAddress
                    
                    Text(
                        text = displayName, 
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSpam) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                    )
                    if (displayName != log.displayAddress) {
                        Text(
                            text = log.displayAddress, 
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSpam) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                    Text(
                        text = dateFormat.format(Date(log.date)), 
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSpam) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = log.displayBody,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSpam) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if ((info != null && (info.isContact || info.bizCategory.isNotBlank() || info.spamCategory.isNotBlank())) ||
                (filterType != null && filterType != FilterType.UNKNOWN) ||
                (log is SmsLog.Mms)) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = (if (isSpam) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.outlineVariant).copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (info?.isContact == true) {
                        TagItem(text = "Contact", icon = Icons.Rounded.AccountCircle, isError = false)
                    }
                    if (info?.bizCategory?.isNotBlank() == true) {
                        TagItem(text = info.bizCategory, icon = Icons.Rounded.Business, isError = false)
                    }
                    if (info?.spamCategory?.isNotBlank() == true) {
                        TagItem(text = info.spamCategory, icon = Icons.Rounded.Warning, isError = true)
                    }
                    
                    if (filterType != null && filterType != FilterType.UNKNOWN) {
                        when (filterType) {
                            FilterType.NORMAL -> TagItem(text = "Normal", icon = Icons.Rounded.CheckCircle, isError = false)
                            FilterType.SPAM -> TagItem(text = "Spam Message", icon = Icons.Rounded.Block, isError = true)
                            FilterType.PROMOTION -> TagItem(text = "Promotion", icon = Icons.Rounded.LocalOffer, isError = false)
                            FilterType.TRANSACTION -> TagItem(text = "Transaction", icon = Icons.Rounded.Receipt, isError = false)
                            else -> {}
                        }
                    }
                    
                    if (log is SmsLog.Mms) {
                        TagItem(text = "MMS", icon = Icons.Rounded.Mms, isError = false)
                    }
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
