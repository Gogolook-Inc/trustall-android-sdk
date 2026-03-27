package com.gogolook.trustall.demo.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gogolook.trustall.callerid.model.NumberInfo

@Composable
fun CallerIdOverlayCard(
    info: NumberInfo?,
    number: String,
    onClose: () -> Unit
) {
    val isSpam = (info?.spamLevel ?: 0) > 0
    val cardColor = if (isSpam) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val onCardColor = if (isSpam) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer

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
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon and Info
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = onCardColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        val icon: ImageVector = when {
                            isSpam -> Icons.Default.Warning
                            info?.isContact == true -> Icons.Default.AccountCircle
                            else -> Icons.Default.Person
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = onCardColor
                        )
                    }

                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = info?.name.takeUnless { it.isNullOrEmpty() } ?: "Unknown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = number,
                            style = MaterialTheme.typography.bodyMedium,
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

            val tags = buildList {
                if (info?.isContact == true) add("Contact")
                info?.bizCategory?.takeIf { it.isNotEmpty() }?.let { add(it) }
                info?.spamCategory?.takeIf { it.isNotEmpty() }?.let { add(it) }
            }

            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = onCardColor.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
