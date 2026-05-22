package com.shaikh.storentry.domain.model

/**
 * SubscriptionState — Immutable model representing the user's active billing status,
 * entitlements, and purchase details. Cached locally for offline support.
 */
data class SubscriptionState(
    val isPremium: Boolean,
    val activeEntitlements: List<String>,
    val expirationDateMillis: Long?
)
