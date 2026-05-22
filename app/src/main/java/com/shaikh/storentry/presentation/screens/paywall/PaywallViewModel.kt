package com.shaikh.storentry.presentation.screens.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.shaikh.storentry.domain.repository.SubscriptionRepository
import com.shaikh.storentry.utils.SubscriptionConfig
import com.shaikh.storentry.util.analytics.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * PaywallUiState — Clean presentation tier states representing loading, success (with real/mock packaging), or errors.
 */
sealed class PaywallUiState {
    object Idle : PaywallUiState()
    object Loading : PaywallUiState()
    data class Success(
        val priceText: String,
        val availablePackage: Package? = null
    ) : PaywallUiState()
    data class Error(val message: String) : PaywallUiState()
    object Empty : PaywallUiState()
}

/**
 * PaywallViewModel — Coordinates product selection, purchases, restorations, and mock billing fallbacks.
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaywallUiState>(PaywallUiState.Idle)
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    private val _purchaseSuccessEvent = MutableSharedFlow<Unit>()
    val purchaseSuccessEvent: SharedFlow<Unit> = _purchaseSuccessEvent.asSharedFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    init {
        loadOfferings()
        analyticsManager.logEvent(AnalyticsManager.Events.PAYWALL_VIEWED)
        viewModelScope.launch {
            subscriptionRepository.observeSubscriptionState().collect { state ->
                _isPremium.value = state.isPremium
            }
        }
    }

    /**
     * Attempts to query packages from RevenueCat, falling back to a mock local bundle
     * if the billing API key is blank/invalid, or if the device is completely offline.
     */
    fun loadOfferings() {
        _uiState.value = PaywallUiState.Loading

        try {
            Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
                override fun onReceived(offerings: Offerings) {
                    val defaultOffering = offerings[SubscriptionConfig.OFFERING_DEFAULT]
                    val monthlyPackage = defaultOffering?.monthly

                    if (monthlyPackage != null) {
                        _uiState.value = PaywallUiState.Success(
                            priceText = monthlyPackage.product.price.formatted,
                            availablePackage = monthlyPackage
                        )
                    } else {
                        // Fall back to offline mock if no monthly package configured in dashboard
                        loadMockOffering()
                    }
                }

                override fun onError(error: PurchasesError) {
                    Timber.w("RevenueCat getOfferings failed (Code: ${error.code}): ${error.message}. Falling back to offline mock.")
                    loadMockOffering()
                }
            })
        } catch (e: Exception) {
            Timber.e(e, "RevenueCat getOfferings initialization exception. Falling back to offline mock.")
            loadMockOffering()
        }
    }

    private fun loadMockOffering() {
        // Fallback to offline mock pricing (₹199/month standard)
        _uiState.value = PaywallUiState.Success(
            priceText = "₹199"
        )
    }

    /**
     * Executes the Google Play purchase flow for the selected package.
     */
    fun purchase(activity: Activity) {
        val currentState = _uiState.value
        if (currentState !is PaywallUiState.Success) return

        _uiState.value = PaywallUiState.Loading
        val rcPackage = currentState.availablePackage

        viewModelScope.launch {
            val result = subscriptionRepository.purchasePremium(activity, rcPackage)
            result.fold(
                onSuccess = {
                    analyticsManager.logEvent(AnalyticsManager.Events.PREMIUM_PURCHASED)
                    _purchaseSuccessEvent.emit(Unit)
                },
                onFailure = { error ->
                    if (error.message == "USER_CANCELLED") {
                        loadOfferings() // Put back success pricing state
                    } else {
                        _uiState.value = PaywallUiState.Error(error.message ?: "Purchase failed")
                    }
                }
            )
        }
    }

    /**
     * Restores previously purchased store licences.
     */
    fun restorePurchases() {
        _uiState.value = PaywallUiState.Loading

        viewModelScope.launch {
            val result = subscriptionRepository.restorePurchases()
            result.fold(
                onSuccess = {
                    _purchaseSuccessEvent.emit(Unit)
                },
                onFailure = { error ->
                    _uiState.value = PaywallUiState.Error(error.message ?: "Failed to restore purchases")
                }
            )
        }
    }
}
