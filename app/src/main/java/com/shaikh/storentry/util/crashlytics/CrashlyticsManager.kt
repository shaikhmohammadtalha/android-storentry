package com.shaikh.storentry.util.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CrashlyticsManager — Production-ready crash reporting layer.
 * Handles fatal crashes, non-fatal exceptions, and breadcrumbs.
 */
@Singleton
class CrashlyticsManager @Inject constructor() {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    /**
     * Records a non-fatal exception.
     */
    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    /**
     * Logs a custom message to be included in the next crash report.
     */
    fun log(message: String) {
        crashlytics.log(message)
    }

    /**
     * Sets a custom key-value pair to be included in the crash report.
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Sets a user ID for crash reports.
     */
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    /**
     * Records a network failure with details.
     */
    fun logNetworkError(url: String, code: Int, message: String) {
        log("Network Error: $url, Code: $code, Message: $message")
        setCustomKey("last_failed_url", url)
        setCustomKey("last_failed_code", code)
    }

    /**
     * Records a UI interaction breadcrumb.
     */
    fun logUIAction(action: String) {
        log("UI Action: $action")
    }
}
