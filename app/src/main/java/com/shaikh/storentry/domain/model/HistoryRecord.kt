package com.shaikh.storentry.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HistoryRecord(
    val id: Int = 0,
    val productId: String,
    val productName: String,
    val actionType: HistoryActionType,
    val description: String,
    val timestamp: Long,
    val metadata: String? = null // For things like "Total: 150 units" or "Low Stock Alert"
)

enum class HistoryActionType {
    STOCK_ADDED,
    STOCK_REMOVED,
    PRICE_CHANGED,
    PRODUCT_ADDED,
    PRODUCT_UPDATED,
    PRODUCT_DELETED,
    CATEGORY_CHANGED
}
