package com.shaikh.storentry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shaikh.storentry.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(historyEntity: HistoryEntity)

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE actionType = :actionType ORDER BY timestamp DESC")
    fun getHistoryByType(actionType: String): Flow<List<HistoryEntity>>

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)
}
