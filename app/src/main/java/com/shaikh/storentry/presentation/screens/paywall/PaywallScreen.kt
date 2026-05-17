package com.shaikh.storentry.presentation.screens.paywall

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shaikh.storentry.R
import com.shaikh.storentry.presentation.components.AppButton
import com.shaikh.storentry.presentation.components.ErrorView
import com.shaikh.storentry.presentation.components.LoadingView
import kotlinx.coroutines.flow.collectLatest

/**
 * PaywallScreen — Displays premium membership options, features checklist, and processes local sandboxed checkout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onNavigateBack: () -> Unit,
    onPurchaseSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.purchaseSuccessEvent.collectLatest {
            Toast.makeText(context, "Successfully upgraded to Premium!", Toast.LENGTH_LONG).show()
            onPurchaseSuccess()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is PaywallUiState.Loading -> {
                    LoadingView()
                }
                is PaywallUiState.Success -> {
                    val isPremium by viewModel.isPremium.collectAsState()
                    PaywallContent(
                        isPremium = isPremium,
                        priceText = state.priceText,
                        onSubscribeClick = {
                            val activity = context as? Activity
                            if (activity != null) {
                                viewModel.purchase(activity)
                            } else {
                                Toast.makeText(context, "Billing error: Could not identify host container.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onRestoreClick = {
                            viewModel.restorePurchases()
                        }
                    )
                }
                is PaywallUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = {
                            viewModel.loadOfferings()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaywallContent(
    isPremium: Boolean,
    priceText: String,
    onSubscribeClick: () -> Unit,
    onRestoreClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPremium) Icons.Default.WorkspacePremium else Icons.Default.Star,
                contentDescription = null,
                tint = if (isPremium) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = R.string.paywall_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.paywall_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                BenefitItem(
                    icon = Icons.Default.AllInclusive,
                    title = stringResource(id = R.string.paywall_benefits_1_title),
                    description = stringResource(id = R.string.paywall_benefits_1_desc)
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                BenefitItem(
                    icon = Icons.Default.NotificationsActive,
                    title = stringResource(id = R.string.paywall_benefits_3_title),
                    description = stringResource(id = R.string.paywall_benefits_3_desc)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (isPremium) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Premium Active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "/ month",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text = stringResource(id = R.string.paywall_cta),
                onClick = onSubscribeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.cancel_subscription_tip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onRestoreClick) {
                Text(
                    text = stringResource(id = R.string.paywall_restore),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun BenefitItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
        }
    }
}
