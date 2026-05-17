package com.shaikh.storentry.data.repository

import android.app.Activity
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.shaikh.storentry.domain.model.SubscriptionStatus
import com.shaikh.storentry.domain.repository.PreferenceRepository
import com.shaikh.storentry.domain.repository.SubscriptionRepository
import com.shaikh.storentry.utils.SubscriptionConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SubscriptionRepositoryImpl — Direct data provider implementation for Entitlements.
 * Synchronizes billing statuses from RevenueCat back to the local DataStore cache.
 */
@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : SubscriptionRepository {

    private val _status = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Loading)
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Observe local cache state on startup
        repositoryScope.launch {
            try {
                val cachedIsPremium = preferenceRepository.isPremium().first()
                val cachedExpiry = preferenceRepository.getPremiumExpiry().first()

                if (SubscriptionConfig.DEBUG_FORCE_PREMIUM) {
                    _status.value = SubscriptionStatus.Premium()
                } else if (cachedIsPremium) {
                    _status.value = SubscriptionStatus.Premium(cachedExpiry)
                } else {
                    _status.value = SubscriptionStatus.Free
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading cached subscription level")
                _status.value = SubscriptionStatus.Free
            }
        }

        // Set up real-time listener to keep cache synced when RevenueCat updates in background
        try {
            Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
                updateSubscriptionCache(customerInfo)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error attaching RevenueCat callback listener")
        }
    }

    override fun observeSubscriptionStatus(): Flow<SubscriptionStatus> {
        return _status.asStateFlow()
    }

    override suspend fun refreshSubscriptionStatus() {
        if (SubscriptionConfig.DEBUG_FORCE_PREMIUM) {
            _status.value = SubscriptionStatus.Premium()
            return
        }

        try {
            Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    updateSubscriptionCache(customerInfo)
                }

                override fun onError(error: PurchasesError) {
                    Timber.w("Failed to refresh client information from billing server: ${error.message}")
                    // On failure (e.g. offline), safely fall back to our local DataStore cached state
                    repositoryScope.launch {
                        val isPremium = preferenceRepository.isPremium().first()
                        val expiry = preferenceRepository.getPremiumExpiry().first()
                        _status.value = if (isPremium) SubscriptionStatus.Premium(expiry) else SubscriptionStatus.Free
                    }
                }
            })
        } catch (e: Exception) {
            Timber.e(e, "Error accessing billing library API instance")
        }
    }

    override suspend fun purchasePremium(activity: Activity): Result<Unit> {
        // Handled via PaywallViewModel where full context and UI state is available
        return Result.success(Unit)
    }

    override suspend fun restorePurchases(): Result<Unit> {
        // Handled via PaywallViewModel where full context and UI state is available
        return Result.success(Unit)
    }

    override suspend fun isPremium(): Boolean {
        if (SubscriptionConfig.DEBUG_FORCE_PREMIUM) return true
        return preferenceRepository.isPremium().first()
    }

    /**
     * Extracts entitlement info and commits it directly to the local preferences cache.
     */
    private fun updateSubscriptionCache(customerInfo: CustomerInfo) {
        val premiumEntitlement = customerInfo.entitlements[SubscriptionConfig.ENTITLEMENT_PREMIUM]
        val isActive = premiumEntitlement?.isActive == true
        val expirationDate = premiumEntitlement?.expirationDate?.time

        repositoryScope.launch {
            preferenceRepository.setPremium(isActive)
            preferenceRepository.setPremiumExpiry(expirationDate ?: 0L)

            _status.value = if (isActive) {
                SubscriptionStatus.Premium(expirationDate)
            } else {
                SubscriptionStatus.Free
            }
            Timber.d("Subscription status cache synced: Premium=$isActive")
        }
    }
}
