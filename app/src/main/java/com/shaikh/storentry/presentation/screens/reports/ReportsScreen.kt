package com.shaikh.storentry.presentation.screens.reports

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
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shaikh.storentry.R
import com.shaikh.storentry.presentation.components.AppCard
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shaikh.storentry.domain.model.Product
import com.shaikh.storentry.presentation.components.EmptyStateView
import com.shaikh.storentry.presentation.components.LoadingView
import com.shaikh.storentry.utils.UiState
import com.shaikh.storentry.utils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductDetails: (String) -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reports_title), fontWeight = FontWeight.Bold) },
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
                .background(MaterialTheme.colorScheme.background)
                .padding(pv)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> LoadingView()
                is UiState.Empty -> EmptyStateView(
                    title = stringResource(R.string.no_reports_yet),
                    subtitle = stringResource(R.string.no_products_reports_description),
                    buttonText = stringResource(R.string.add_product_title)
                )
                is UiState.Success -> {
                    val data = state.data
                    
                    val topCategories = remember(data.categoryDistribution) {
                        data.categoryDistribution.toList().take(5)
                    }
                    
                    val topValuedProductsIndexed = remember(data.topValuedProducts) {
                        data.topValuedProducts.withIndex().toList()
                    }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Summary Stats
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ReportStatCard(
                                    label = stringResource(R.string.total_products),
                                    value = "${data.totalProducts}",
                                    modifier = Modifier.weight(1f)
                                )
                                ReportStatCard(
                                    label = stringResource(R.string.inventory_value),
                                    value = "₹${formatCurrency(data.totalInventoryValue)}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Inventory Health Summary
                        item {
                            Text(
                                text = stringResource(R.string.stock_movement),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    StockMovementBar(
                                        label = stringResource(R.string.stock_in),
                                        value = data.inStockCount,
                                        total = data.totalProducts,
                                        color = Color(0xFF22C55E)
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    StockMovementBar(
                                        label = stringResource(R.string.adjustments),
                                        value = data.lowStockCount,
                                        total = data.totalProducts,
                                        color = Color(0xFFF59E0B)
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    StockMovementBar(
                                        label = stringResource(R.string.stock_out),
                                        value = data.outOfStockCount,
                                        total = data.totalProducts,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        // Category Distribution
                        if (topCategories.isNotEmpty()) {
                            val totalValue = data.totalInventoryValue
                            item {
                                Text(
                                    text = stringResource(R.string.category_distribution),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            item {
                                AppCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        topCategories.forEachIndexed { index, (category, value) ->
                                            val proportion = if (totalValue > 0) (value / totalValue).toFloat() else 0f
                                            val categoryColor = getCategoryColor(index)
                                            
                                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(10.dp)
                                                                .clip(CircleShape)
                                                                .background(categoryColor)
                                                        )
                                                        Spacer(Modifier.width(8.dp))
                                                        Text(
                                                            text = category,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    Text(
                                                        text = "₹${formatCurrency(value)}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Spacer(Modifier.height(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(proportion)
                                                            .height(6.dp)
                                                            .clip(CircleShape)
                                                            .background(categoryColor)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Top Valued Products
                        if (topValuedProductsIndexed.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.top_moving_products),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            items(topValuedProductsIndexed, key = { it.value.id }) { (index, product) ->
                                TopProductRow(
                                    rank = index + 1,
                                    product = product,
                                    onClick = { onNavigateToProductDetails(product.id) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

private fun getCategoryColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFFEC4899), // Pink
        Color(0xFFF59E0B), // Amber
        Color(0xFF10B981), // Emerald
        Color(0xFF3B82F6), // Blue
    )
    return colors[index % colors.size]
}

@Composable
fun ReportStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StockMovementBar(label: String, value: Int, total: Int, color: Color) {
    val fraction = if (total > 0) (value.toFloat() / total).coerceIn(0f, 1f) else 0f
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$value items", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
        }
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))) {
            Box(modifier = Modifier.fillMaxWidth(fraction).height(8.dp).background(color, RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
fun TopProductRow(rank: Int, product: Product, onClick: () -> Unit) {
    val totalValue = product.sellingPrice * product.quantity
    AppCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (rank == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("#$rank", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        color = if (rank == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("${product.quantity} units · ${product.category}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${formatCurrency(totalValue)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    stringResource(R.string.inventory_value),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
