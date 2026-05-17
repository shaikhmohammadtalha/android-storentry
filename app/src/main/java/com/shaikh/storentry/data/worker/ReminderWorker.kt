package com.shaikh.storentry.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shaikh.storentry.domain.repository.ReminderRepository
import com.shaikh.storentry.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val reminderRepository: ReminderRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val productId = inputData.getString("PRODUCT_ID")
        val productName = inputData.getString("PRODUCT_NAME") ?: "Product"

        if (productId != null) {
            notificationHelper.showStockReminder(productId, productName)
            // Mark as completed in DB
            reminderRepository.markCompleted(id.toString())
        }

        return Result.success()
    }
}
