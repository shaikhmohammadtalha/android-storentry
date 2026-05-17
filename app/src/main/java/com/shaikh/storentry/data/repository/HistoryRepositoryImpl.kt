package com.shaikh.storentry.data.repository

import com.shaikh.storentry.data.local.dao.HistoryDao
import com.shaikh.storentry.data.local.entity.HistoryEntity
import com.shaikh.storentry.domain.model.HistoryActionType
import com.shaikh.storentry.domain.model.HistoryRecord
import com.shaikh.storentry.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {
    
    override suspend fun addRecord(record: HistoryRecord) {
        historyDao.insert(HistoryEntity.fromDomain(record))
    }

    override fun getAllHistory(): Flow<List<HistoryRecord>> {
        return historyDao.getAllHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHistoryByType(type: HistoryActionType): Flow<List<HistoryRecord>> {
        return historyDao.getHistoryByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    override suspend fun deleteHistoryById(id: Int) {
        historyDao.deleteHistoryById(id)
    }
}
