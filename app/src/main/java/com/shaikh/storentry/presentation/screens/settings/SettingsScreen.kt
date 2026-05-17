package com.shaikh.storentry.presentation.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaikh.storentry.R
import com.shaikh.storentry.presentation.components.AppCard
import coil.compose.AsyncImage
import com.shaikh.storentry.domain.model.SubscriptionStatus
import com.shaikh.storentry.domain.model.User

/**
 * SettingsScreen — Premium UI for managing shop settings and account info.
 * Recreated from the provided design, with Reports integrated as a setting item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    subscriptionStatus: SubscriptionStatus,
    user: User?,
    autoSyncEnabled: Boolean,
    isSyncing: Boolean,
    hasAlerts: Boolean,
    onAutoSyncToggle: (Boolean) -> Unit,
    onManualSyncClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onNavigateToHelpSupport: () -> Unit,
    onNavigateToAboutStorentry: () -> Unit,
    onSignInClick: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.settings_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            com.shaikh.storentry.presentation.components.AppBottomBar(
                currentRoute = "settings",
                onNavItemClick = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "product_list" -> onNavigateToProducts()
                        "alerts" -> onNavigateToAlerts()
                        "settings" -> {}
                    }
                },
                hasAlerts = hasAlerts
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Section
            if (user != null) {
                // Logged In Card
                AppCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Image
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (user.photoUrl != null) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.onboarding_hero), // Placeholder
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.displayName ?: stringResource(id = R.string.profile_name),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = user.email ?: stringResource(id = R.string.profile_phone),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        // Edit Profile Button
                        Surface(
                            onClick = { /* TODO: Edit Profile */ },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else {
                // Not Logged In Card
                AppCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.sign_in_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.sign_in_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onSignInClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(id = R.string.sign_in_with_google))
                        }
                    }
                }
            }

            // Settings Sections
            SettingsSection(title = stringResource(id = R.string.section_account)) {
                val subText = when (subscriptionStatus) {
                    is SubscriptionStatus.Premium -> "Storentry Premium (Active)"
                    is SubscriptionStatus.Free -> "Storentry Free (Tap to Upgrade)"
                    else -> "Checking Status..."
                }
                val subIcon = if (subscriptionStatus is SubscriptionStatus.Premium) Icons.Default.Stars else Icons.Default.CardMembership
                SettingsItem(
                    icon = subIcon,
                    title = "Subscription Tier",
                    subtitle = subText,
                    onClick = onNavigateToPaywall
                )
                val isPremium = subscriptionStatus is SubscriptionStatus.Premium
                
                // Auto Sync Toggle
                SettingsToggleItem(
                    icon = Icons.Default.Sync,
                    title = stringResource(id = R.string.settings_auto_sync),
                    subtitle = if (isPremium) stringResource(id = R.string.settings_auto_sync_subtitle_premium) else stringResource(id = R.string.settings_auto_sync_subtitle_free),
                    checked = if (isPremium) autoSyncEnabled else false,
                    onCheckedChange = { checked ->
                        if (isPremium) {
                            onAutoSyncToggle(checked)
                        }
                    },
                    enabled = isPremium,
                    onClick = onNavigateToPaywall,
                    trailingBadge = {
                        if (!isPremium) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                )

                // Manual Sync Button
                SettingsActionItem(
                    icon = Icons.Default.CloudSync,
                    title = stringResource(id = R.string.settings_sync_now),
                    subtitle = if (user != null) stringResource(id = R.string.settings_sync_now_subtitle_signed_in) else stringResource(id = R.string.settings_sync_now_subtitle_guest),
                    isLoading = isSyncing,
                    onClick = {
                        onManualSyncClick()
                    },
                    enabled = user != null
                )
                SettingsItem(
                    icon = Icons.Default.History,
                    title = "Activity History",
                    subtitle = "View stock updates and edits",
                    onClick = onNavigateToHistory
                )
            }

            SettingsSection(title = stringResource(id = R.string.section_app)) {
                // INTEGRATED REPORTS ITEM
                SettingsItem(
                    icon = Icons.Default.BarChart,
                    title = stringResource(id = R.string.reports_title),
                    subtitle = "Inventory analytics & charts",
                    onClick = onNavigateToReports
                )
                SettingsItem(
                    icon = Icons.Default.NotificationsNone,
                    title = stringResource(id = R.string.notifications),
                    subtitle = "Manage stock update reminders",
                    onClick = onNavigateToNotifications
                )
            }

            SettingsSection(title = stringResource(id = R.string.section_support)) {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = stringResource(id = R.string.help_support),
                    onClick = onNavigateToHelpSupport
                )
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(id = R.string.about_storentry),
                    onClick = onNavigateToAboutStorentry
                )
            }

            // Logout Button (Only show if logged in)
            if (user != null) {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(id = R.string.logout),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Version Info
            val context = androidx.compose.ui.platform.LocalContext.current
            val packageInfo = remember(context) {
                try {
                    context.packageManager.getPackageInfo(context.packageName, 0)
                } catch (e: Exception) {
                    null
                }
            }
            val versionName = packageInfo?.versionName ?: "1.0.0"

            Text(
                text = "Version $versionName",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Wrapper
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    trailingBadge: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = {
            if (!enabled && onClick != null) {
                onClick()
            }
        },
        enabled = !enabled && onClick != null,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                    if (trailingBadge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        trailingBadge()
                    }
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@Composable
fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = if (isLoading || !enabled) ({}) else onClick,
        color = Color.Transparent,
        enabled = enabled && !isLoading
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (!isLoading) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
