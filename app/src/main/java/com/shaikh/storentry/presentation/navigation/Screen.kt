package com.shaikh.storentry.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Sealed class defining all app screens and routes.
 */
sealed class Screen {
    @Serializable data object Splash : Screen()
    @Serializable data object Welcome : Screen()
    @Serializable data object Onboarding : Screen()
    @Serializable data object Home : Screen()
    @Serializable data object ProductList : Screen()
    @Serializable data class ProductDetails(val productId: String) : Screen()
    @Serializable data object AddProduct : Screen()
    @Serializable data object LowStockAlerts : Screen()
    @Serializable data object Reports : Screen()
    @Serializable data object Settings : Screen()
    @Serializable data object ActivityHistory : Screen()
    @Serializable data class EditProduct(val productId: String) : Screen()
    @Serializable data object Notifications : Screen()
    @Serializable data object Paywall : Screen()
    @Serializable data object HelpSupport : Screen()
    @Serializable data object AboutStorentry : Screen()
}
