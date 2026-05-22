package com.shaikh.storentry.presentation.screens.product_details

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.shaikh.storentry.R
import com.shaikh.storentry.presentation.components.AppCard

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shaikh.storentry.utils.UiState
import com.shaikh.storentry.presentation.components.LoadingView
import com.shaikh.storentry.presentation.components.ErrorView
import com.shaikh.storentry.domain.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToPaywall: () -> Unit,
    viewModel: ProductDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPremiumGateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        viewModel.fetchProduct(productId)
    }

    val reminderSetMsg = stringResource(R.string.reminder_set_msg)
    LaunchedEffect(key1 = true) {
        viewModel.reminderEvent.collect { event ->
            when (event) {
                "SUCCESS" -> {
                    android.widget.Toast.makeText(context, reminderSetMsg, android.widget.Toast.LENGTH_SHORT).show()
                }
                "LIMIT_REACHED" -> {
                    showPremiumGateDialog = true
                }
                else -> {
                    android.widget.Toast.makeText(context, "Failed to schedule reminder", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.product_details_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(productId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Product")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { pv ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(message = state.message, onRetry = { viewModel.fetchProduct(productId) })
                is UiState.Success<Product> -> {
                    val product = state.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .consumeWindowInsets(pv)
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = pv.calculateTopPadding() + 16.dp,
                                bottom = pv.calculateBottomPadding() + 16.dp
                            ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Product hero card
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.padding(top = 6.dp)) {
                                    Text(product.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                        }

                        // Stock level card
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(stringResource(R.string.stock_level), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.height(16.dp))
                                // Manual Input
                                var textValue by remember(product.quantity) { mutableStateOf(product.quantity.toString()) }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Surface(
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(52.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            ) {
                                                BasicTextField(
                                                    value = textValue,
                                                    onValueChange = { newValue ->
                                                        if (newValue.all { it.isDigit() } && newValue.length <= 6) {
                                                            textValue = newValue
                                                        }
                                                    },
                                                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Number,
                                                        imeAction = ImeAction.Done
                                                    ),
                                                    singleLine = true
                                                )
                                            }
                                        }
                                    }

                                    // Adjustment Buttons
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { 
                                                if (product.quantity > 0) {
                                                    viewModel.updateStock(product.id, product.quantity - 1)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f), CircleShape)
                                        ) {
                                            Text("-", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, textAlign = TextAlign.Center)
                                        }
                                        IconButton(
                                            onClick = { 
                                                viewModel.updateStock(product.id, product.quantity + 1)
                                            },
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        ) {
                                            Text("+", style = MaterialTheme.typography.headlineMedium, color = Color.White, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                                
                                // Label Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp, top = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val initialQuantity = remember(product.id) { product.quantity }
                                    
                                    Column {
                                        Text(
                                            text = stringResource(R.string.qty_units_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        if (product.quantity != initialQuantity) {
                                            Text(
                                                text = "Initial: $initialQuantity",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Save & Reset Buttons when editing
                                if (textValue != product.quantity.toString()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                val qty = textValue.toIntOrNull() ?: 0
                                                viewModel.updateStock(product.id, qty)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(vertical = 10.dp)
                                        ) {
                                            Text(text = stringResource(id = R.string.save_btn), fontWeight = FontWeight.Bold)
                                        }
                                        
                                        OutlinedButton(
                                            onClick = {
                                                textValue = product.quantity.toString()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(vertical = 10.dp)
                                        ) {
                                            Text(text = stringResource(id = R.string.reset_btn), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        stringResource(R.string.low_stock_threshold_label, product.lowStockThreshold),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        // Price info card
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(stringResource(R.string.price), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    PriceInfoItem(icon = Icons.Outlined.ShoppingCart, label = stringResource(R.string.purchase_price), value = "₹${String.format("%.2f", product.purchasePrice)}", modifier = Modifier.weight(1f))
                                    PriceInfoItem(icon = Icons.Outlined.LocalOffer, label = stringResource(R.string.selling_price), value = "₹${String.format("%.2f", product.sellingPrice)}", modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        // Reminder card
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val reminderSetMsg = stringResource(R.string.reminder_set_msg)
                        
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.reminders_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(stringResource(R.string.remind_to_update), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ReminderOptionChip(
                                        label = stringResource(R.string.remind_in_1h),
                                        onClick = { 
                                            viewModel.scheduleReminder(product.id, product.name, 60)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    ReminderOptionChip(
                                        label = stringResource(R.string.remind_in_4h),
                                        onClick = { 
                                            viewModel.scheduleReminder(product.id, product.name, 240)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    ReminderOptionChip(
                                        label = stringResource(R.string.remind_tomorrow),
                                        onClick = { 
                                            viewModel.scheduleReminder(product.id, product.name, 1440)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }
                else -> {}
            }
        }
    }

    if (showPremiumGateDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showPremiumGateDialog = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
                    .padding(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    com.shaikh.storentry.presentation.components.PremiumGateCard(
                        title = stringResource(id = R.string.reminders_limit_reached),
                        description = stringResource(id = R.string.reminders_limit_desc),
                        onUpgradeClick = {
                            showPremiumGateDialog = false
                            onNavigateToPaywall()
                        }
                    )
                    TextButton(onClick = { showPremiumGateDialog = false }) {
                        Text(
                            text = stringResource(id = android.R.string.cancel),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderOptionChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PriceInfoItem(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}
