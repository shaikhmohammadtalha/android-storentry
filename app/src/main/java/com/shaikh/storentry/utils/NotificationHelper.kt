package com.shaikh.storentry.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.shaikh.storentry.MainActivity
import com.shaikh.storentry.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {
    companion object {
        const val REMINDER_CHANNEL_ID = "stock_reminder_channel"
        const val LOW_STOCK_CHANNEL_ID = "low_stock_channel"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Stock Update Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications to remind you to update product stock"
            }

            val lowStockChannel = NotificationChannel(
                LOW_STOCK_CHANNEL_ID,
                "Low Stock Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when product stock falls below threshold"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(reminderChannel)
            manager.createNotificationChannel(lowStockChannel)
        }
    }

    fun showStockReminder(productId: String, productName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("PRODUCT_ID", productId) // We can use this to navigate to product details
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            productId.hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Use app icon
            .setContentTitle("Update Stock: $productName")
            .setContentText("It's time to check and update the stock for $productName.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(productId, 0, builder.build())
            } catch (e: SecurityException) {
                // Handle missing permission
            }
        }
    }
}
