package com.shaikh.storentry.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shaikh.storentry.R

/**
 * Navigation items for the Bottom Bar.
 */
sealed class BottomNavItem(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", R.string.nav_home, Icons.Outlined.Home)
    object Products : BottomNavItem("product_list", R.string.nav_products, Icons.Outlined.Inventory2)
    object Alerts : BottomNavItem("alerts", R.string.nav_alerts, Icons.Outlined.Notifications)
    object Settings : BottomNavItem("settings", R.string.nav_settings, Icons.Outlined.Settings)
}

/**
 * A custom BottomBar component for the Storentry app.
 *
 * @param currentRoute The current active route to highlight the selected item.
 * @param onNavItemClick Callback when a navigation item is clicked.
 */
@Composable
fun AppBottomBar(
    currentRoute: String,
    onNavItemClick: (String) -> Unit,
    hasAlerts: Boolean = false,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Products,
        BottomNavItem.Alerts,
        BottomNavItem.Settings
    )

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavItemClick(item.route) },
                icon = {
                    if (item is BottomNavItem.Alerts && hasAlerts) {
                        BadgedBox(badge = { Badge { Text("!") } }) {
                            Icon(item.icon, contentDescription = null)
                        }
                    } else {
                        Icon(item.icon, contentDescription = null)
                    }
                },
                label = { Text(stringResource(id = item.labelResId)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
