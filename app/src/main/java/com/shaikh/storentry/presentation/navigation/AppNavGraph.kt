package com.shaikh.storentry.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.shaikh.storentry.domain.model.SubscriptionStatus
import com.shaikh.storentry.presentation.screens.splash.SplashScreen
import com.shaikh.storentry.presentation.screens.home.HomeScreen
import com.shaikh.storentry.presentation.screens.add_product.AddProductScreen
import com.shaikh.storentry.presentation.screens.onboarding.OnboardingScreen
import com.shaikh.storentry.presentation.screens.product_details.ProductDetailsScreen
import com.shaikh.storentry.presentation.screens.product_list.ProductListScreen
import com.shaikh.storentry.presentation.screens.low_stock.LowStockAlertsScreen
import com.shaikh.storentry.presentation.screens.reports.ReportsScreen
import com.shaikh.storentry.presentation.screens.settings.SettingsScreen
import com.shaikh.storentry.presentation.screens.settings.SettingsViewModel
import com.shaikh.storentry.presentation.screens.settings.SubscriptionViewModel
import com.shaikh.storentry.presentation.screens.paywall.PaywallScreen
import com.shaikh.storentry.presentation.screens.edit_product.EditProductScreen
import com.shaikh.storentry.presentation.screens.history.ActivityHistoryScreen
import com.shaikh.storentry.presentation.screens.notifications.NotificationsScreen
import com.shaikh.storentry.presentation.screens.welcome.WelcomeScreen
import com.shaikh.storentry.presentation.screens.help.HelpSupportScreen
import com.shaikh.storentry.presentation.screens.about.AboutStorentryScreen

/**
 * Main navigation graph for the application.
 */
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val subscriptionStatus by subscriptionViewModel.subscriptionStatus.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                }
            )
        }
        
        composable<Screen.Welcome> {
            WelcomeScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding) {
                        popUpTo(Screen.Welcome) { inclusive = true }
                    }
                }
            )
        }
        
        composable<Screen.Onboarding> {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Onboarding) { inclusive = true }
                    }
                }
            )
        }
        
        composable<Screen.Home> {
            HomeScreen(
                onNavigateToAddProduct = {
                    navController.navigate(Screen.AddProduct)
                },
                onNavigateToProductList = {
                    navController.navigate(Screen.ProductList)
                },
                onNavigateToAlerts = {
                    navController.navigate(Screen.LowStockAlerts)
                },
                onNavigateToReports = {
                    navController.navigate(Screen.Reports)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications)
                },
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Screen.ProductDetails(productId))
                }
            )
        }
        
        composable<Screen.AddProduct> {
            AddProductScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToPaywall = { navController.navigate(Screen.Paywall) }
            )
        }
        
        composable<Screen.ProductDetails> { backStackEntry ->
            val details: Screen.ProductDetails = backStackEntry.toRoute()
            ProductDetailsScreen(
                productId = details.productId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToEdit = { productId ->
                    navController.navigate(Screen.EditProduct(productId))
                },
                onNavigateToPaywall = { navController.navigate(Screen.Paywall) }
            )
        }
        
        composable<Screen.ProductList> {
            ProductListScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Screen.ProductDetails(productId))
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                },
                onNavigateToAlerts = {
                    navController.navigate(Screen.LowStockAlerts)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings)
                }
            )
        }
        
        composable<Screen.LowStockAlerts> {
            LowStockAlertsScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Screen.ProductDetails(productId))
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                },
                onNavigateToProducts = {
                    navController.navigate(Screen.ProductList)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings)
                }
            )
        }
        
        composable<Screen.Reports> {
            ReportsScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Screen.ProductDetails(productId))
                }
            )
        }
        
        composable<Screen.Settings> {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val user by settingsViewModel.currentUser.collectAsState()
            val autoSyncEnabled by settingsViewModel.autoSyncEnabled.collectAsState()
            val isSyncing by settingsViewModel.isSyncing.collectAsState()
            val hasAlerts by settingsViewModel.hasAlerts.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current
            
            SettingsScreen(
                subscriptionStatus = subscriptionStatus,
                user = user,
                autoSyncEnabled = autoSyncEnabled,
                isSyncing = isSyncing,
                hasAlerts = hasAlerts,
                onAutoSyncToggle = { enabled ->
                    settingsViewModel.setAutoSyncEnabled(enabled)
                },
                onManualSyncClick = {
                    settingsViewModel.triggerManualSync()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                },
                onNavigateToProducts = {
                    navController.navigate(Screen.ProductList)
                },
                onNavigateToAlerts = {
                    navController.navigate(Screen.LowStockAlerts)
                },
                onNavigateToReports = {
                    navController.navigate(Screen.Reports)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.ActivityHistory)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications)
                },
                onNavigateToPaywall = {
                    navController.navigate(Screen.Paywall)
                },
                onSignInClick = {
                    settingsViewModel.signInWithGoogle(context)
                },
                onLogout = {
                    settingsViewModel.signOut()
                    navController.navigate(Screen.Welcome) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToHelpSupport = {
                    navController.navigate(Screen.HelpSupport)
                },
                onNavigateToAboutStorentry = {
                    navController.navigate(Screen.AboutStorentry)
                }
            )
        }

        composable<Screen.ActivityHistory> {
            ActivityHistoryScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable<Screen.Notifications> {
            NotificationsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable<Screen.EditProduct> { backStackEntry ->
            val edit: Screen.EditProduct = backStackEntry.toRoute()
            EditProductScreen(
                productId = edit.productId,
                onNavigateBack = { navController.navigateUp() },
                onProductDeleted = {
                    navController.popBackStack()
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.Paywall> {
            PaywallScreen(
                onNavigateBack = { navController.navigateUp() },
                onPurchaseSuccess = {
                    navController.navigateUp()
                }
            )
        }

        composable<Screen.HelpSupport> {
            HelpSupportScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<Screen.AboutStorentry> {
            AboutStorentryScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}
