package com.shaikh.storentry.presentation.screens.product_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.shaikh.storentry.data.local.entity.ReminderEntity
import com.shaikh.storentry.data.worker.ReminderWorker
import com.shaikh.storentry.domain.model.Product
import com.shaikh.storentry.domain.repository.ProductRepository
import com.shaikh.storentry.domain.repository.ReminderRepository
import com.shaikh.storentry.domain.repository.SubscriptionRepository
import com.shaikh.storentry.util.analytics.AnalyticsManager
import com.shaikh.storentry.util.crashlytics.CrashlyticsManager
import com.shaikh.storentry.utils.SubscriptionConfig
import com.shaikh.storentry.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * ProductDetailsViewModel — Coordinates product details data extraction,
 * stock increments/decrements, and stock-update reminders scheduling.
 */
@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val reminderRepository: ReminderRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val workManager: WorkManager,
    private val analyticsManager: AnalyticsManager,
    private val crashlyticsManager: CrashlyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Product>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _reminderEvent = MutableSharedFlow<String>()
    val reminderEvent: SharedFlow<String> = _reminderEvent.asSharedFlow()

    fun fetchProduct(id: String, refreshOnly: Boolean = false) {
        viewModelScope.launch {
            if (!refreshOnly) {
                _uiState.value = UiState.Loading
            }
            try {
                val product = repository.getProductById(id)
                if (product != null) {
                    _uiState.value = UiState.Success(product)
                    
                    // Analytics: track product view
                    analyticsManager.logEvent(AnalyticsManager.Events.PRODUCT_VIEWED, android.os.Bundle().apply {
                        putString(AnalyticsManager.Params.PRODUCT_ID, product.id)
                        putString(AnalyticsManager.Params.PRODUCT_NAME, product.name)
                    })
                } else {
                    _uiState.value = UiState.Error("Product not found")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching product")
                crashlyticsManager.recordException(e)
                _uiState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun updateStock(productId: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                repository.updateStock(productId, newQuantity)
                fetchProduct(productId, refreshOnly = true)
            } catch (e: Exception) {
                Timber.e(e, "Error updating stock")
                crashlyticsManager.recordException(e)
            }
        }
    }

    /**
     * Gated reminder scheduling. Limits non-paying accounts to 5 active upcoming alarms.
     */
    fun scheduleReminder(productId: String, productName: String, delayMinutes: Long) {
        viewModelScope.launch {
            try {
                val isPremium = subscriptionRepository.isPremium()
                val activeRemindersCount = reminderRepository.getUpcomingReminders().first().size

                if (!isPremium && activeRemindersCount >= SubscriptionConfig.FREE_REMINDER_LIMIT) {
                    val bundle = android.os.Bundle().apply {
                        putString(AnalyticsManager.Params.FEATURE_NAME, "schedule_reminder")
                        putString(AnalyticsManager.Params.SOURCE, "limit_reached")
                    }
                    analyticsManager.logEvent(AnalyticsManager.Events.FEATURES_GATED, bundle)
                    _reminderEvent.emit("LIMIT_REACHED")
                    return@launch
                }

                val data = Data.Builder()
                    .putString("PRODUCT_ID", productId)
                    .putString("PRODUCT_NAME", productName)
                    .build()

                val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInputData(data)
                    .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                    .addTag("reminder_$productId")
                    .build()

                workManager.enqueue(request)

                // Save to DB
                reminderRepository.insertReminder(
                    ReminderEntity(
                        productId = productId,
                        productName = productName,
                        scheduledTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(delayMinutes),
                        status = "SCHEDULED",
                        workId = request.id.toString()
                    )
                )

                // Analytics
                analyticsManager.logEvent("reminder_scheduled", android.os.Bundle().apply {
                    putString(AnalyticsManager.Params.PRODUCT_ID, productId)
                    putLong("delay_minutes", delayMinutes)
                })

                _reminderEvent.emit("SUCCESS")
            } catch (e: Exception) {
                Timber.e(e, "Error scheduling stock update alarm")
                crashlyticsManager.recordException(e)
                _reminderEvent.emit("ERROR")
            }
        }
    }
}
