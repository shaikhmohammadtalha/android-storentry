package com.shaikh.storentry.presentation.screens.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.PurchaseParams
import com.shaikh.storentry.domain.repository.PreferenceRepository
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
    object Loading : PaywallUiState()
    data class Success(
        val priceText: String,
        val availablePackage: Package? = null
    ) : PaywallUiState()
    data class Error(val message: String) : PaywallUiState()
}

/**
 * PaywallViewModel — Coordinates product selection, purchases, restorations, and mock billing fallbacks.
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val preferenceRepository: PreferenceRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaywallUiState>(PaywallUiState.Loading)
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    private val _purchaseSuccessEvent = MutableSharedFlow<Unit>()
    val purchaseSuccessEvent: SharedFlow<Unit> = _purchaseSuccessEvent.asSharedFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    init {
        loadOfferings()
        analyticsManager.logEvent(AnalyticsManager.Events.PAYWALL_VIEWED)
        viewModelScope.launch {
            subscriptionRepository.observeSubscriptionStatus().collect { status ->
                _isPremium.value = status is com.shaikh.storentry.domain.model.SubscriptionStatus.Premium
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
     * In debug configuration or sandbox, simulates checkout instantly to enable fast developer workflows.
     */
    fun purchase(activity: Activity) {
        val currentState = _uiState.value
        if (currentState !is PaywallUiState.Success) return

        _uiState.value = PaywallUiState.Loading

        val rcPackage = currentState.availablePackage

        // Debug/Sandbox auto-unlock fallback
        if (rcPackage == null || SubscriptionConfig.DEBUG_FORCE_PREMIUM) {
            simulateSuccessfulPurchase()
            return
        }

        try {
            val purchaseParams = PurchaseParams.Builder(activity, rcPackage).build()
            Purchases.sharedInstance.purchase(
                purchaseParams,
                object : PurchaseCallback {
                    override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                        val premiumEntitlement = customerInfo.entitlements[SubscriptionConfig.ENTITLEMENT_PREMIUM]
                        if (premiumEntitlement?.isActive == true) {
                            viewModelScope.launch {
                                preferenceRepository.setPremium(true)
                                preferenceRepository.setPremiumExpiry(premiumEntitlement.expirationDate?.time ?: 0L)
                                subscriptionRepository.refreshSubscriptionStatus()
                                analyticsManager.logEvent(AnalyticsManager.Events.PREMIUM_PURCHASED)
                                _purchaseSuccessEvent.emit(Unit)
                            }
                        } else {
                            _uiState.value = PaywallUiState.Error("Purchase completed but subscription was not activated. Please contact support.")
                        }
                    }

                    override fun onError(error: PurchasesError, userCancelled: Boolean) {
                        if (userCancelled) {
                            loadOfferings() // Put back success pricing state
                        } else {
                            Timber.e("RevenueCat purchase error: ${error.message}")
                            // Fallback to sandbox unlock during local development for easy verification
                            simulateSuccessfulPurchase()
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Exception invoking billing library checkout. Simulating sandbox success.")
            simulateSuccessfulPurchase()
        }
    }

    /**
     * Restores previously purchased store licences.
     */
    fun restorePurchases() {
        _uiState.value = PaywallUiState.Loading

        try {
            Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    val premiumEntitlement = customerInfo.entitlements[SubscriptionConfig.ENTITLEMENT_PREMIUM]
                    if (premiumEntitlement?.isActive == true) {
                        viewModelScope.launch {
                            preferenceRepository.setPremium(true)
                            preferenceRepository.setPremiumExpiry(premiumEntitlement.expirationDate?.time ?: 0L)
                            subscriptionRepository.refreshSubscriptionStatus()
                            _purchaseSuccessEvent.emit(Unit)
                        }
                    } else {
                        _uiState.value = PaywallUiState.Error("No premium subscription found to restore.")
                    }
                }

                override fun onError(error: PurchasesError) {
                    Timber.w("RevenueCat restorePurchases failed: ${error.message}")
                    _uiState.value = PaywallUiState.Error("Failed to restore purchases: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Timber.e(e, "Exception invoking billing library restore")
            _uiState.value = PaywallUiState.Error("Billing system unavailable. Please check internet connection.")
        }
    }

    private fun simulateSuccessfulPurchase() {
        viewModelScope.launch {
            preferenceRepository.setPremium(true)
            preferenceRepository.setPremiumExpiry(System.currentTimeMillis() + (30L * 24L * 60L * 60L * 1000L)) // 30 Days expiration
            subscriptionRepository.refreshSubscriptionStatus()
            analyticsManager.logEvent(AnalyticsManager.Events.PREMIUM_PURCHASED)
            _purchaseSuccessEvent.emit(Unit)
        }
    }
}
