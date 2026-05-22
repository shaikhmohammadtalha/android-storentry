package com.shaikh.storentry.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing app preferences.
 */
interface PreferenceRepository {
    /**
     * Returns a flow indicating whether the onboarding has been completed.
     */
    fun isOnboardingCompleted(): Flow<Boolean>

    /**
     * Sets the onboarding completion status.
     * @param completed True if onboarding is completed, false otherwise.
     */
    suspend fun setOnboardingCompleted(completed: Boolean)

    /**
     * Returns a flow of premium subscription state.
     */
    fun isPremium(): Flow<Boolean>

    /**
     * Caches the premium state.
     */
    suspend fun setPremium(isPremium: Boolean)

    /**
     * Returns a flow of premium expiry timestamp (millis).
     */
    fun getPremiumExpiry(): Flow<Long>

    /**
     * Caches the premium expiry timestamp.
     */
    suspend fun setPremiumExpiry(expiry: Long)

    /**
     * Returns a flow of active entitlements list.
     */
    fun getActiveEntitlements(): Flow<List<String>>

    /**
     * Caches the active entitlements list.
     */
    suspend fun setActiveEntitlements(entitlements: List<String>)

    /**
     * Returns a flow indicating if auto sync is enabled.
     */
    fun isAutoSyncEnabled(): Flow<Boolean>

    /**
     * Sets the auto sync status.
     */
    suspend fun setAutoSyncEnabled(enabled: Boolean)

    /**
     * Returns a flow indicating if premium is forced for debugging purposes.
     * Must be ignored in release builds.
     */
    fun isDebugForcePremium(): Flow<Boolean>

    /**
     * Toggles the debug-only premium override.
     */
    suspend fun setDebugForcePremium(enabled: Boolean)
}
