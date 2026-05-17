package com.shaikh.storentry.utils

import com.shaikh.storentry.BuildConfig

/**
 * SubscriptionConfig — Centralized business rules, limits, and developer toggles
 * for the Free and Premium subscription tiers.
 *
 * Keeping this as a single file makes it extremely easy to adjust limits or toggle developer overrides.
 */
object SubscriptionConfig {

    /**
     * Maximum products a user on the FREE tier can add.
     */
    const val FREE_PRODUCT_LIMIT = 100

    /**
     * Maximum reminders a user on the FREE tier can schedule.
     */
    const val FREE_REMINDER_LIMIT = 5

    /**
     * RevenueCat entitlement identifier as configured on the RevenueCat Dashboard.
     */
    const val ENTITLEMENT_PREMIUM = "premium"

    /**
     * RevenueCat default monthly offering identifier.
     */
    const val OFFERING_DEFAULT = "default"

    /**
     * DEBUG OVERRIDE: Flipped to true during local development, this forces all
     * premium checks to succeed. Safely checks BuildConfig.DEBUG to ensure this
     * can never leak into production builds.
     */
    val DEBUG_FORCE_PREMIUM: Boolean = BuildConfig.DEBUG && false // Flipped to true for development testing
}
