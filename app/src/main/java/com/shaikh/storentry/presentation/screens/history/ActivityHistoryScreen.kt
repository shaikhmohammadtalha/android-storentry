package com.shaikh.storentry.presentation.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shaikh.storentry.R
import com.shaikh.storentry.domain.model.HistoryActionType
import com.shaikh.storentry.domain.model.HistoryRecord
import com.shaikh.storentry.presentation.components.AppCard
import com.shaikh.storentry.presentation.components.EmptyStateView
import com.shaikh.storentry.presentation.components.LoadingView
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: ActivityHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.undoEvent.collect { productName ->
            val message = context.getString(R.string.product_restored_toast, productName)
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Activity History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Filter Menu */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HistoryFilter.values()) { filter ->
                    FilterChip(
                        selected = uiState.selectedFilter == filter,
                        onClick = { viewModel.onFilterSelected(filter) },
                        label = { Text(filter.label) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.selectedFilter == filter,
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            if (uiState.isLoading) {
                LoadingView()
            } else if (uiState.filteredRecords.isEmpty()) {
                EmptyStateView(
                    title = "No activity found",
                    subtitle = "When you update stock or edit products, the changes will appear here.",
                    icon = Icons.Outlined.History
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val groupedRecords = uiState.groupedRecords
                    groupedRecords.forEach { (dateHeader, records) ->
                        item(key = dateHeader) {
                            DateHeader(text = dateHeader)
                        }
                        items(records, key = { it.id }) { record ->
                            HistoryItem(
                                record = record,
                                onUndoClick = { viewModel.undoDelete(record) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateHeader(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun HistoryItem(
    record: HistoryRecord,
    onUndoClick: () -> Unit = {}
) {
    val iconInfo = getIconInfo(record.actionType)
    val formattedTime = remember(record.timestamp) { formatTime(record.timestamp) }
    
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Wrapper
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconInfo.backgroundColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconInfo.icon,
                    contentDescription = null,
                    tint = iconInfo.backgroundColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (record.metadata != null && record.actionType != HistoryActionType.PRODUCT_DELETED) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = record.metadata,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (record.metadata.contains("Low Stock", ignoreCase = true)) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                }

                if (record.actionType == HistoryActionType.PRODUCT_DELETED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onUndoClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Undo Delete",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

data class IconInfo(val icon: ImageVector, val backgroundColor: Color)

@Composable
fun getIconInfo(type: HistoryActionType): IconInfo {
    return when (type) {
        HistoryActionType.STOCK_ADDED -> IconInfo(Icons.Default.Add, Color(0xFF22C55E))
        HistoryActionType.STOCK_REMOVED -> IconInfo(Icons.Default.Remove, Color(0xFFEF4444))
        HistoryActionType.PRICE_CHANGED -> IconInfo(Icons.Default.LocalOffer, Color(0xFFF59E0B))
        HistoryActionType.PRODUCT_ADDED -> IconInfo(Icons.Default.AddCircleOutline, Color(0xFF6366F1))
        HistoryActionType.PRODUCT_UPDATED -> IconInfo(Icons.Default.Edit, Color(0xFF3B82F6))
        HistoryActionType.PRODUCT_DELETED -> IconInfo(Icons.Default.DeleteOutline, Color(0xFFEF4444))
        HistoryActionType.CATEGORY_CHANGED -> IconInfo(Icons.Default.Category, Color(0xFF8B5CF6))
    }
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun groupRecordsByDate(records: List<HistoryRecord>): Map<String, List<HistoryRecord>> {
    val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
    val yesterday = today - 86400000
    
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    return records.groupBy { record ->
        when {
            record.timestamp >= today -> "Today"
            record.timestamp >= yesterday -> "Yesterday"
            else -> sdf.format(Date(record.timestamp))
        }
    }
}
