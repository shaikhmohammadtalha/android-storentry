package com.shaikh.storentry.domain.repository

import com.shaikh.storentry.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAllReminders(): Flow<List<ReminderEntity>>
    fun getUpcomingReminders(): Flow<List<ReminderEntity>>
    suspend fun insertReminder(reminder: ReminderEntity): Long
    suspend fun updateReminder(reminder: ReminderEntity)
    suspend fun cancelReminder(workId: String)
    suspend fun markCompleted(workId: String)
}
