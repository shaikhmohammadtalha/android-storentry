package com.shaikh.storentry

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.shaikh.storentry.util.analytics.AnalyticsManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Main application class.
 */
@HiltAndroidApp
class MainApplication : Application() {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onCreate() {
        super.onCreate()
        
        // Initialize RevenueCat SDK
        Purchases.configure(
            PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT_API_KEY).build()
        )

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }

        // Log app open event
        analyticsManager.logEvent(FirebaseAnalytics.Event.APP_OPEN)
    }

    /**
     * A Timber tree that logs to Crashlytics in production.
     */
    private class CrashlyticsTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.log(message)
            if (t != null) {
                crashlytics.recordException(t)
            }
        }
    }
}
