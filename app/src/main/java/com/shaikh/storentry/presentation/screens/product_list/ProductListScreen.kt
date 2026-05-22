package com.shaikh.storentry.presentation.screens.product_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shaikh.storentry.R
import com.shaikh.storentry.presentation.components.*
import com.shaikh.storentry.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductDetails: (String) -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasAlerts by viewModel.hasAlerts.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.products_title),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                currentRoute = "product_list",
                onNavItemClick = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "product_list" -> {}
                        "alerts" -> onNavigateToAlerts()
                        "settings" -> onNavigateToSettings()
                    }
                },
                hasAlerts = hasAlerts
            )
        }
    ) { pv ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> LoadingView()
                is UiState.Empty -> EmptyStateView(
                    title = stringResource(R.string.empty_products_title),
                    subtitle = stringResource(R.string.empty_products_description),
                    buttonText = stringResource(R.string.add_product_title),
                    onButtonClick = { onNavigateBack() }
                )
                is UiState.Error -> ErrorView(message = state.message, onRetry = { viewModel.fetchProducts() })
                is UiState.Success -> {
                    val products = state.data

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .consumeWindowInsets(pv),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = pv.calculateTopPadding() + 8.dp,
                            bottom = pv.calculateBottomPadding() + 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Search Field - isolated state collection to prevent full LazyColumn recomposition
                        item {
                            ProductSearchBar(
                                viewModel = viewModel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }

                        if (products.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.no_search_results),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        } else {
                            items(products, key = { it.id }) { product ->
                                ProductItem(
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

/**
 * Isolated search bar composable to prevent typing inputs from causing parent recompositions.
 */
@Composable
fun ProductSearchBar(
    viewModel: ProductListViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.onSearchQueryChange(it) },
        modifier = modifier,
        placeholder = { Text(stringResource(id = R.string.search_products)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        singleLine = true
    )
}

@Composable
fun ProductItem(product: com.shaikh.storentry.domain.model.Product, onClick: () -> Unit) {
    val isLowStock = product.quantity <= product.lowStockThreshold
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isLowStock) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) 
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
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
                if (isLowStock) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.low_stock_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format("%.2f", product.sellingPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.stock_qty, product.quantity),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    fontWeight = if (isLowStock) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
