package com.shaikh.storentry.data.repository

import android.app.Activity
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.shaikh.storentry.domain.model.SubscriptionState
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * SubscriptionRepositoryImpl — Direct data provider implementation for Entitlements.
 * Synchronizes billing statuses from RevenueCat back to the local DataStore cache.
 */
@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : SubscriptionRepository {

    private val _subscriptionState: MutableStateFlow<SubscriptionState>
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Read local cache state synchronously on startup to avoid UI flickering
        val initialState = try {
            runBlocking(Dispatchers.IO) {
                val cachedIsPremium = preferenceRepository.isPremium().first()
                val cachedExpiry = preferenceRepository.getPremiumExpiry().first()
                val cachedEntitlements = preferenceRepository.getActiveEntitlements().first()
                val debugForce = if (com.shaikh.storentry.BuildConfig.DEBUG) {
                    preferenceRepository.isDebugForcePremium().first()
                } else false
                val isPremium = cachedIsPremium || debugForce || SubscriptionConfig.DEBUG_FORCE_PREMIUM
                SubscriptionState(
                    isPremium = isPremium,
                    activeEntitlements = cachedEntitlements,
                    expirationDateMillis = if (cachedExpiry > 0L) cachedExpiry else null
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading cached subscription level on startup")
            SubscriptionState(
                isPremium = SubscriptionConfig.DEBUG_FORCE_PREMIUM,
                activeEntitlements = emptyList(),
                expirationDateMillis = null
            )
        }
        _subscriptionState = MutableStateFlow(initialState)

        // Set up real-time listener to keep cache synced when RevenueCat updates in background
        try {
            Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
                updateSubscriptionCache(customerInfo)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error attaching RevenueCat callback listener")
        }
    }

    override fun observeSubscriptionState(): Flow<SubscriptionState> {
        return _subscriptionState.asStateFlow()
    }

    override suspend fun refreshSubscriptionStatus() {
        val debugForce = if (com.shaikh.storentry.BuildConfig.DEBUG) {
            preferenceRepository.isDebugForcePremium().first()
        } else false

        if (SubscriptionConfig.DEBUG_FORCE_PREMIUM || debugForce) {
            _subscriptionState.value = SubscriptionState(true, listOf(SubscriptionConfig.ENTITLEMENT_PREMIUM), null)
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
                        val entitlements = preferenceRepository.getActiveEntitlements().first()
                        val currentDebugForce = if (com.shaikh.storentry.BuildConfig.DEBUG) {
                            preferenceRepository.isDebugForcePremium().first()
                        } else false
                        _subscriptionState.value = SubscriptionState(
                            isPremium = isPremium || currentDebugForce || SubscriptionConfig.DEBUG_FORCE_PREMIUM,
                            activeEntitlements = entitlements,
                            expirationDateMillis = if (expiry > 0L) expiry else null
                        )
                    }
                }
            })
        } catch (e: Exception) {
            Timber.e(e, "Error accessing billing library API instance")
        }
    }

    override suspend fun purchasePremium(activity: Activity, rcPackage: com.revenuecat.purchases.Package?): Result<Unit> {
        val debugForce = if (com.shaikh.storentry.BuildConfig.DEBUG) {
            preferenceRepository.isDebugForcePremium().first()
        } else false

        if (SubscriptionConfig.DEBUG_FORCE_PREMIUM || debugForce) {
            simulateSuccessfulPurchase()
            return Result.success(Unit)
        }

        if (rcPackage == null) {
            if (com.shaikh.storentry.BuildConfig.DEBUG) {
                Timber.d("Debug build: simulating successful purchase since rcPackage is null.")
                simulateSuccessfulPurchase()
                return Result.success(Unit)
            } else {
                return Result.failure(Exception("Offerings package is unavailable. Please check your network connection."))
            }
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                val purchaseParams = com.revenuecat.purchases.PurchaseParams.Builder(activity, rcPackage).build()
                Purchases.sharedInstance.purchase(
                    purchaseParams,
                    object : com.revenuecat.purchases.interfaces.PurchaseCallback {
                        override fun onCompleted(storeTransaction: com.revenuecat.purchases.models.StoreTransaction, customerInfo: CustomerInfo) {
                            val premiumEntitlement = customerInfo.entitlements[SubscriptionConfig.ENTITLEMENT_PREMIUM]
                            if (premiumEntitlement?.isActive == true) {
                                val expirationDate = premiumEntitlement.expirationDate?.time
                                val activeEntitlements = customerInfo.entitlements.all.filterValues { it.isActive }.keys.toList()
                                
                                repositoryScope.launch {
                                    preferenceRepository.setPremium(true)
                                    preferenceRepository.setPremiumExpiry(expirationDate ?: 0L)
                                    preferenceRepository.setActiveEntitlements(activeEntitlements)
                                    
                                    _subscriptionState.value = SubscriptionState(
                                        isPremium = true,
                                        activeEntitlements = activeEntitlements,
                                        expirationDateMillis = expirationDate
                                    )
                                    continuation.resume(Result.success(Unit))
                                }
                            } else {
                                continuation.resume(Result.failure(Exception("Purchase completed but premium entitlement was not active")))
                            }
                        }

                        override fun onError(error: PurchasesError, userCancelled: Boolean) {
                            if (userCancelled) {
                                continuation.resume(Result.failure(Exception("USER_CANCELLED")))
                            } else {
                                Timber.e("RevenueCat purchase error: ${error.message}")
                                if (com.shaikh.storentry.BuildConfig.DEBUG) {
                                    Timber.d("Debug build: simulating sandbox success on purchase error.")
                                    repositoryScope.launch {
                                        simulateSuccessfulPurchase()
                                        continuation.resume(Result.success(Unit))
                                    }
                                } else {
                                    continuation.resume(Result.failure(Exception(error.message)))
                                }
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    override suspend fun restorePurchases(): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            try {
                Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                    override fun onReceived(customerInfo: CustomerInfo) {
                        val premiumEntitlement = customerInfo.entitlements[SubscriptionConfig.ENTITLEMENT_PREMIUM]
                        if (premiumEntitlement?.isActive == true) {
                            val expirationDate = premiumEntitlement.expirationDate?.time
                            val activeEntitlements = customerInfo.entitlements.all.filterValues { it.isActive }.keys.toList()
                            
                            repositoryScope.launch {
                                preferenceRepository.setPremium(true)
                                preferenceRepository.setPremiumExpiry(expirationDate ?: 0L)
                                preferenceRepository.setActiveEntitlements(activeEntitlements)
                                
                                _subscriptionState.value = SubscriptionState(
                                    isPremium = true,
                                    activeEntitlements = activeEntitlements,
                                    expirationDateMillis = expirationDate
                                )
                                continuation.resume(Result.success(Unit))
                            }
                        } else {
                            continuation.resume(Result.failure(Exception("No active premium entitlement found to restore")))
                        }
                    }

                    override fun onError(error: PurchasesError) {
                        continuation.resume(Result.failure(Exception(error.message)))
                    }
                })
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    override suspend fun isPremium(): Boolean {
        val debugForce = if (com.shaikh.storentry.BuildConfig.DEBUG) {
            preferenceRepository.isDebugForcePremium().first()
        } else false
        if (SubscriptionConfig.DEBUG_FORCE_PREMIUM || debugForce) return true
        return preferenceRepository.isPremium().first()
    }

    /**
     * Extracts entitlement info and commits it directly to the local preferences cache.
     */
    private fun updateSubscriptionCache(customerInfo: CustomerInfo) {
        val premiumEntitlement = customerInfo.entitlements[SubscriptionConfig.ENTITLEMENT_PREMIUM]
        val isActive = premiumEntitlement?.isActive == true
        val expirationDate = premiumEntitlement?.expirationDate?.time
        val activeEntitlements = customerInfo.entitlements.all.filterValues { it.isActive }.keys.toList()

        repositoryScope.launch {
            preferenceRepository.setPremium(isActive)
            preferenceRepository.setPremiumExpiry(expirationDate ?: 0L)
            preferenceRepository.setActiveEntitlements(activeEntitlements)

            val debugForce = if (com.shaikh.storentry.BuildConfig.DEBUG) {
                preferenceRepository.isDebugForcePremium().first()
            } else false

            _subscriptionState.value = SubscriptionState(
                isPremium = isActive || debugForce || SubscriptionConfig.DEBUG_FORCE_PREMIUM,
                activeEntitlements = activeEntitlements,
                expirationDateMillis = expirationDate
            )
            Timber.d("Subscription status cache synced: Premium=$isActive")
        }
    }

    private suspend fun simulateSuccessfulPurchase() {
        val expiryTime = System.currentTimeMillis() + (30L * 24L * 60L * 60L * 1000L) // 30 Days expiration
        val activeEntitlements = listOf(SubscriptionConfig.ENTITLEMENT_PREMIUM)
        
        preferenceRepository.setPremium(true)
        preferenceRepository.setPremiumExpiry(expiryTime)
        preferenceRepository.setActiveEntitlements(activeEntitlements)
        
        _subscriptionState.value = SubscriptionState(
            isPremium = true,
            activeEntitlements = activeEntitlements,
            expirationDateMillis = expiryTime
        )
    }
}
