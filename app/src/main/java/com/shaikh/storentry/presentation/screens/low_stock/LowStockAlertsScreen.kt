package com.shaikh.storentry.presentation.screens.low_stock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shaikh.storentry.R
import com.shaikh.storentry.presentation.components.*
import com.shaikh.storentry.utils.UiState

/**
 * LowStockAlertsScreen — shows items requiring immediate attention.
 * Matches the Stitch "Low Stock Alerts" design.
 *@OptIn(ExperimentalMaterial3Api::class)

 **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LowStockAlertsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductDetails: (String) -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToProducts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: LowStockAlertsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasAlerts by viewModel.hasAlerts.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.low_stock_alerts_title),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        bottomBar = {
            AppBottomBar(
                currentRoute = "alerts",
                onNavItemClick = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "product_list" -> onNavigateToProducts()
                        "alerts" -> {}
                        "settings" -> onNavigateToSettings()
                    }
                },
                hasAlerts = hasAlerts
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> LoadingView()
                is UiState.Empty -> EmptyStateView(
                    title = stringResource(R.string.no_data_title),
                    subtitle = stringResource(R.string.items_below_threshold),
                    buttonText = stringResource(R.string.back),
                    onButtonClick = onNavigateBack
                )

                is UiState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { viewModel.fetchLowStockProducts() })

                is UiState.Success -> {
                    val alertItems = state.data
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .consumeWindowInsets(paddingValues),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = paddingValues.calculateTopPadding() + 16.dp,
                            bottom = paddingValues.calculateBottomPadding() + 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header info card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(
                                        alpha = 0.4f
                                    )
                                ),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.NotificationsActive,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = stringResource(id = R.string.items_below_threshold),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = stringResource(
                                                id = R.string.low_stock_count,
                                                alertItems.size
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = stringResource(id = R.string.items_requiring_attention),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        items(alertItems) { product ->
                            LowStockAlertCard(
                                product = product,
                                onClick = { onNavigateToProductDetails(product.id) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun LowStockAlertCard(
    product: com.shaikh.storentry.domain.model.Product,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container with background
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (product.quantity == 0)
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else
                    Color(0xFFFEF3C7) // Amber light
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (product.quantity == 0)
                            Icons.Outlined.NotificationsActive
                        else
                            Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = if (product.quantity == 0)
                            MaterialTheme.colorScheme.error
                        else
                            Color(0xFFD97706), // Amber dark
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (product.quantity == 0)
                            stringResource(id = R.string.out_of_stock)
                        else
                            stringResource(id = R.string.qty_left, product.quantity),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (product.quantity == 0)
                            MaterialTheme.colorScheme.error
                        else
                            Color(0xFFD97706),
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = " / min ${product.lowStockThreshold}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Simple, clean chevron for navigation
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
