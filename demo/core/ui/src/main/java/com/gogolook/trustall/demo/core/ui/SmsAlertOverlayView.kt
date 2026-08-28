package com.gogolook.trustall.demo.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gogolook.trustall.core.urlscan.model.Level
import com.gogolook.trustall.core.urlscan.model.UrlScanResult
import com.gogolook.trustall.msgfilter.model.FilterType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SmsAlertOverlayCard(
    item: ScannedSms,
    onClose: () -> Unit
) {
    val hasThreat = item.filterType == FilterType.SPAM || item.urlResults.any {
        it is UrlScanResult.Success && (it.level == Level.MALICIOUS || it.level == Level.SUSPICIOUS)
    }
    val cardColor = if (hasThreat) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.primaryContainer
    val onCardColor = if (hasThreat) MaterialTheme.colorScheme.onErrorContainer
        else MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
            contentColor = onCardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = onCardColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (hasThreat) Icons.Default.Warning else Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = onCardColor
                        )
                    }

                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = item.sms.sender.ifBlank { "Unknown" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                .format(Date(item.sms.timestampMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            color = onCardColor.copy(alpha = 0.8f)
                        )
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = onCardColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.sms.body,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))

            item.filterType?.takeIf { it != FilterType.UNKNOWN }?.let { type ->
                FilterTypeTag(type)
                Spacer(modifier = Modifier.height(4.dp))
            }

            when {
                item.isScanning -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = onCardColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scanning URLs…",
                            style = MaterialTheme.typography.labelMedium,
                            color = onCardColor.copy(alpha = 0.8f)
                        )
                    }
                }
                item.urlResults.isEmpty() -> {
                    Text(
                        text = "No URLs detected",
                        style = MaterialTheme.typography.labelMedium,
                        color = onCardColor.copy(alpha = 0.8f)
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        item.urlResults.forEach { result -> UrlScanResultRow(result) }
                    }
                }
            }
        }
    }
}

/** A single URL scan verdict row: a colored level chip followed by the URL. */
@Composable
fun UrlScanResultRow(result: UrlScanResult) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val (label, color) = when (result) {
            is UrlScanResult.Success -> result.level.name to urlScanLevelColor(result.level)
            is UrlScanResult.Error -> "ERROR" to Color.Gray
        }
        val url = when (result) {
            is UrlScanResult.Success -> result.url
            is UrlScanResult.Error -> result.url
        }
        Surface(
            shape = MaterialTheme.shapes.small,
            color = color.copy(alpha = 0.15f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = url,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun urlScanLevelColor(level: Level): Color {
    return when (level) {
        Level.SAFE -> Color(0xFF2E7D32)
        Level.MALICIOUS -> Color(0xFFC62828)
        Level.SUSPICIOUS -> Color(0xFFFFA500)
        Level.UNDEFINED -> Color.Gray
    }
}

/** A colored chip for the message filter classification. */
@Composable
fun FilterTypeTag(type: FilterType) {
    val (label, color) = when (type) {
        FilterType.SPAM -> "SPAM" to Color(0xFFC62828)
        FilterType.PROMOTION -> "PROMOTION" to Color(0xFF1565C0)
        FilterType.TRANSACTION -> "TRANSACTION" to Color(0xFF6A1B9A)
        FilterType.NORMAL -> "NORMAL" to Color(0xFF2E7D32)
        else -> type.name to Color.Gray
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
