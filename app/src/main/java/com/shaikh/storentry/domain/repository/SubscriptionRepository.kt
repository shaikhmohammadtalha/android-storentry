package com.shaikh.storentry.domain.repository

import android.app.Activity
import com.shaikh.storentry.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

/**
 * SubscriptionRepository — Domain contract governing all user entitlements and purchase interactions.
 * Decouples presentation ViewModels from backend platforms like RevenueCat.
 */
interface SubscriptionRepository {

    /**
     * Emits the current subscription level state streams in real-time.
     */
    fun observeSubscriptionStatus(): Flow<SubscriptionStatus>

    /**
     * Triggers in-background validation checks against RevenueCat APIs or local stores.
     */
    suspend fun refreshSubscriptionStatus()

    /**
     * Executes subscription payment flows using Google Play and RevenueCat billing APIs.
     */
    suspend fun purchasePremium(activity: Activity): Result<Unit>

    /**
     * Restores previously purchased premium licenses.
     */
    suspend fun restorePurchases(): Result<Unit>

    /**
     * Returns whether the current user is active premium. Optimized for high-speed synchronous calls.
     */
    suspend fun isPremium(): Boolean
}
