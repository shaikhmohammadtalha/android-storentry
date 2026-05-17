package com.shaikh.storentry.domain.repository

import com.shaikh.storentry.domain.model.HistoryActionType
import com.shaikh.storentry.domain.model.HistoryRecord
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun addRecord(record: HistoryRecord)
    fun getAllHistory(): Flow<List<HistoryRecord>>
    fun getHistoryByType(type: HistoryActionType): Flow<List<HistoryRecord>>
    suspend fun clearHistory()
    suspend fun deleteHistoryById(id: Int)
}
