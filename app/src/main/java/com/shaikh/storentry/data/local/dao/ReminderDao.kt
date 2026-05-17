package com.shaikh.storentry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shaikh.storentry.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders ORDER BY scheduledTime DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE status = 'SCHEDULED' ORDER BY scheduledTime ASC")
    fun getUpcomingReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE workId = :workId")
    suspend fun getReminderByWorkId(workId: String): ReminderEntity?

    @Query("UPDATE reminders SET status = :status WHERE workId = :workId")
    suspend fun updateStatusByWorkId(workId: String, status: String)
}
