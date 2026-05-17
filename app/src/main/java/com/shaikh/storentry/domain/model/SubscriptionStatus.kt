package com.shaikh.storentry.domain.model

/**
 * SubscriptionStatus — Exhaustive sealed class representing the user's current
 * subscription entitlement level.
 */
sealed class SubscriptionStatus {

    /**
     * User is on the FREE tier of the application (Default).
     */
    object Free : SubscriptionStatus()

    /**
     * User has an active PREMIUM subscription.
     * @property expiryDate Expiry timestamp in milliseconds, if applicable.
     */
    data class Premium(val expiryDate: Long? = null) : SubscriptionStatus()

    /**
     * System is currently loading subscription states from local cache or server.
     */
    object Loading : SubscriptionStatus()
}
