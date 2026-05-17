package com.shaikh.storentry.util.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AnalyticsManager — Production-ready analytics layer.
 * Centralized event logging with type-safe parameters.
 */
@Singleton
class AnalyticsManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    /**
     * Logs a custom event with parameters.
     */
    fun logEvent(name: String, params: Bundle? = null) {
        firebaseAnalytics.logEvent(name, params)
    }

    /**
     * Sets a user property for all subsequent events.
     */
    fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    /**
     * Sets the user ID for this session.
     */
    fun setUserId(id: String?) {
        firebaseAnalytics.setUserId(id)
    }

    /**
     * Tracks a screen view.
     */
    fun trackScreenView(screenName: String, screenClass: String? = null) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass ?: screenName)
        }
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    /**
     * Analytics Constants for events and parameters.
     */
    object Events {
        const val APP_OPEN = "app_open"
        const val LOGIN_SUCCESS = "login_success"
        const val SIGNUP_COMPLETED = "signup_completed"
        const val PRODUCT_VIEWED = "product_viewed"
        const val ADD_TO_CART = "add_to_cart"
        const val PURCHASE_COMPLETED = "purchase_completed"
        const val BUTTON_CLICKED = "button_clicked"
        const val NOTIFICATION_OPENED = "notification_opened"
        
        // Subscription telemetry
        const val PAYWALL_VIEWED = "paywall_viewed"
        const val PREMIUM_PURCHASED = "premium_purchased"
        const val FEATURES_GATED = "features_gated"
    }

    object Params {
        const val PRODUCT_ID = "product_id"
        const val PRODUCT_NAME = "product_name"
        const val BUTTON_NAME = "button_name"
        const val SCREEN_NAME = "screen_name"
        const val STATUS = "status"
        const val FEATURE_NAME = "feature_name"
        const val SOURCE = "source"
    }
}
