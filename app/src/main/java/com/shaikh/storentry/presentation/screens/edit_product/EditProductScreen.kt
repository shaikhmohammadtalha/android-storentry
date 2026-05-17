package com.shaikh.storentry.presentation.screens.edit_product

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaikh.storentry.R
import com.shaikh.storentry.presentation.components.AppButton
import com.shaikh.storentry.presentation.components.AppCard
import com.shaikh.storentry.presentation.components.ErrorView
import com.shaikh.storentry.presentation.components.LoadingView
import com.shaikh.storentry.utils.UiState

import androidx.hilt.navigation.compose.hiltViewModel
import com.shaikh.storentry.domain.model.Product

/**
 * EditProductScreen — Premium UI for editing product details.
 * Recreated from the provided Stitch design system.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onProductDeleted: () -> Unit = {},
    viewModel: EditProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()
    val allCategories by viewModel.categories.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Form state
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var stockUnits by remember { mutableStateOf("") }
    var lowThreshold by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }

    LaunchedEffect(productId) {
        viewModel.fetchProduct(productId)
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            val product = (uiState as UiState.Success<Product>).data
            name = product.name
            category = product.category
            stockUnits = product.quantity.toString()
            lowThreshold = product.lowStockThreshold.toString()
            purchasePrice = product.purchasePrice.toString()
            sellingPrice = product.sellingPrice.toString()
        }
    }

    LaunchedEffect(saveStatus) {
        if (saveStatus is UiState.Success) {
            onNavigateBack()
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.productDeleted.collect {
            onProductDeleted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Edit Product", 
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
                    val product = (uiState as? UiState.Success)?.data
                    if (product != null) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete, 
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header Info Card
                AppCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.Center)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            Text(
                                text = "PRODUCT SKU: ${if (productId.isNotBlank()) "AM-${productId.take(5).uppercase()}" else "N/A"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = name.ifBlank { "Product Name" },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ID: #$productId",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Form Section: Name
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Product Name",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    EditProductTextField(
                        value = name,
                        onValueChange = { name = it }
                    )
                }

                // Form Section: Category with Suggestions
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    EditProductTextField(
                        value = category,
                        onValueChange = { category = it }
                    )
                    
                    // Suggestions Chips
                    val suggestions = remember(category, allCategories) {
                        if (category.isBlank()) {
                            allCategories.take(5)
                        } else {
                            allCategories.filter { it.contains(category, ignoreCase = true) }.take(5)
                        }
                    }

                    if (suggestions.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestions.forEach { suggestion ->
                                FilterChip(
                                    selected = category == suggestion,
                                    onClick = { category = suggestion },
                                    label = { Text(suggestion) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }

                // Form Section: Units and Threshold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Stock Units",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        EditProductTextField(
                            value = stockUnits,
                            onValueChange = { stockUnits = it },
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Low Threshold",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        EditProductTextField(
                            value = lowThreshold,
                            onValueChange = { lowThreshold = it },
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                // Pricing Section Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Text(
                        text = "PRICING DETAILS",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                // Pricing Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Purchase Price",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        EditProductTextField(
                            value = purchasePrice,
                            onValueChange = { purchasePrice = it },
                            prefix = "₹ ",
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Selling Price",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        EditProductTextField(
                            value = sellingPrice,
                            onValueChange = { sellingPrice = it },
                            prefix = "₹ ",
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                }

                // Status Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isInStock = (stockUnits.toIntOrNull() ?: 0) > (lowThreshold.toIntOrNull() ?: 0)
                    StatusChip(
                        text = if (isInStock) "In Stock" else "Out of Stock",
                        icon = if (isInStock) Icons.Default.CheckCircle else Icons.Outlined.ErrorOutline,
                        backgroundColor = if (isInStock) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        contentColor = if (isInStock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                AppButton(
                    text = "Save Changes",
                    onClick = { 
                        viewModel.updateProduct(
                            productId, name, category, stockUnits, purchasePrice, sellingPrice, lowThreshold
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Overlay States
            if (uiState is UiState.Loading || saveStatus is UiState.Loading) {
                LoadingView()
            }

            if (uiState is UiState.Error) {
                ErrorView(
                    message = (uiState as UiState.Error).message,
                    onRetry = { viewModel.fetchProduct(productId) }
                )
            }

            if (saveStatus is UiState.Error) {
                ErrorView(
                    message = (saveStatus as UiState.Error).message,
                    onRetry = { viewModel.resetSaveStatus() }
                )
            }
            if (showDeleteConfirmDialog) {
                val product = (uiState as? UiState.Success)?.data
                if (product != null) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirmDialog = false },
                        title = { Text(text = stringResource(id = R.string.delete_confirm_title), fontWeight = FontWeight.Bold) },
                        text = { Text(text = stringResource(id = R.string.delete_confirm_message, product.name)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteConfirmDialog = false
                                    viewModel.deleteProduct(product)
                                }
                            ) {
                                Text(
                                    text = stringResource(id = R.string.delete),
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirmDialog = false }) {
                                Text(text = stringResource(id = R.string.cancel))
                            }
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditProductTextField(
    value: String,
    onValueChange: (String) -> Unit,
    prefix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        ),
        prefix = if (prefix != null) { { Text(prefix) } } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@Composable
fun EditProductDropdownField(
    value: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    contentColor: Color
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
