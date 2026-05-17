package com.shaikh.storentry.presentation.screens.add_product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shaikh.storentry.R
import com.shaikh.storentry.utils.UiState
import com.shaikh.storentry.presentation.components.LoadingView
import com.shaikh.storentry.presentation.components.ErrorView
import com.shaikh.storentry.presentation.components.AppButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    viewModel: AddProductViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_product_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { pv ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                    )
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section label
                Text(stringResource(R.string.section_product_info), style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.product_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringResource(R.string.category)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(4.dp))

                Text(stringResource(R.string.section_stock), style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text(stringResource(R.string.quantity)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = threshold,
                        onValueChange = { threshold = it },
                        label = { Text(stringResource(R.string.low_stock_threshold)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(stringResource(R.string.section_pricing), style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        label = { Text(stringResource(R.string.purchase_price)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        prefix = { Text("₹") }
                    )
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it },
                        label = { Text(stringResource(R.string.selling_price)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        prefix = { Text("₹") }
                    )
                }

                Spacer(Modifier.height(8.dp))

                AppButton(
                    text = stringResource(R.string.save_product),
                    onClick = { 
                        viewModel.saveProduct(name, category, quantity, purchasePrice, sellingPrice, threshold)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
            }

            // Handle States
            when (val state = uiState) {
                is UiState.Loading -> LoadingView()
                is UiState.Error -> {
                    if (state.message == "LIMIT_REACHED") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                com.shaikh.storentry.presentation.components.PremiumGateCard(
                                    title = stringResource(id = R.string.premium_limit_reached),
                                    description = stringResource(id = R.string.premium_limit_desc),
                                    onUpgradeClick = {
                                        viewModel.resetState()
                                        onNavigateToPaywall()
                                    }
                                )
                                TextButton(onClick = { viewModel.resetState() }) {
                                    Text(
                                        text = stringResource(id = android.R.string.cancel),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    } else {
                        ErrorView(message = state.message, onRetry = { viewModel.resetState() })
                    }
                }
                else -> {}
            }
        }
    }
}
