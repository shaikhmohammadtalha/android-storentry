package com.shaikh.storentry.presentation.screens.history

import com.shaikh.storentry.domain.model.HistoryActionType
import com.shaikh.storentry.domain.model.HistoryRecord

data class ActivityHistoryUiState(
    val records: List<HistoryRecord> = emptyList(),
    val filteredRecords: List<HistoryRecord> = emptyList(),
    val groupedRecords: Map<String, List<HistoryRecord>> = emptyMap(),
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class HistoryFilter(val label: String) {
    ALL("All"),
    STOCK_ADDED("Stock Added"),
    STOCK_REMOVED("Stock Removed"),
    PRICE_CHANGED("Price Changed"),
    PRODUCT_UPDATED("Updated")
}

fun HistoryActionType.toFilter(): HistoryFilter? {
    return when(this) {
        HistoryActionType.STOCK_ADDED -> HistoryFilter.STOCK_ADDED
        HistoryActionType.STOCK_REMOVED -> HistoryFilter.STOCK_REMOVED
        HistoryActionType.PRICE_CHANGED -> HistoryFilter.PRICE_CHANGED
        HistoryActionType.PRODUCT_ADDED, 
        HistoryActionType.PRODUCT_UPDATED, 
        HistoryActionType.PRODUCT_DELETED,
        HistoryActionType.CATEGORY_CHANGED -> HistoryFilter.PRODUCT_UPDATED
    }
}
