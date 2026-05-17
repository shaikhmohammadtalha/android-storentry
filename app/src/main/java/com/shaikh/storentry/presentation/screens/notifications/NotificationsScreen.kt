package com.shaikh.storentry.presentation.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.hilt.navigation.compose.hiltViewModel
import com.shaikh.storentry.R
import com.shaikh.storentry.data.local.entity.ReminderEntity
import com.shaikh.storentry.presentation.components.AppCard
import com.shaikh.storentry.presentation.components.EmptyStateView
import com.shaikh.storentry.presentation.components.LoadingView
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { pv ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (uiState.isLoading) {
                LoadingView()
            } else if (uiState.upcoming.isEmpty() && uiState.history.isEmpty()) {
                EmptyStateView(
                    title = "No Notifications",
                    subtitle = "You haven't set any stock update reminders yet.",
                    buttonText = "Back",
                    onButtonClick = onNavigateBack
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.upcoming.isNotEmpty()) {
                        item {
                            Text(
                                "Upcoming Reminders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(uiState.upcoming) { reminder ->
                            ReminderItem(
                                reminder = reminder,
                                onCancel = { viewModel.cancelReminder(reminder.workId) }
                            )
                        }
                    }

                    if (uiState.history.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Notification History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(uiState.history) { reminder ->
                            ReminderHistoryItem(reminder = reminder)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderItem(
    reminder: ReminderEntity,
    onCancel: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reminder.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    formatTimestamp(reminder.scheduledTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = "Cancel Reminder",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ReminderHistoryItem(reminder: ReminderEntity) {
    val statusColor = when (reminder.status) {
        "COMPLETED" -> MaterialTheme.colorScheme.primary
        "CANCELLED" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    AppCard(modifier = Modifier.fillMaxWidth().alpha(0.7f)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (reminder.status == "COMPLETED") Icons.Default.NotificationsNone else Icons.Default.NotificationsOff,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reminder.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${reminder.status.lowercase().replaceFirstChar { it.uppercase() }} at ${formatTimestamp(reminder.scheduledTime)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
