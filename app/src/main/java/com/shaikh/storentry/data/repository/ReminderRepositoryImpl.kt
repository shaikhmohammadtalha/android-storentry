package com.shaikh.storentry.data.repository

import androidx.work.WorkManager
import com.shaikh.storentry.data.local.dao.ReminderDao
import com.shaikh.storentry.data.local.entity.ReminderEntity
import com.shaikh.storentry.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao,
    private val workManager: WorkManager
) : ReminderRepository {
    override fun getAllReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    override fun getUpcomingReminders(): Flow<List<ReminderEntity>> = reminderDao.getUpcomingReminders()

    override suspend fun insertReminder(reminder: ReminderEntity): Long = reminderDao.insertReminder(reminder)

    override suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)

    override suspend fun cancelReminder(workId: String) {
        workManager.cancelWorkById(UUID.fromString(workId))
        reminderDao.updateStatusByWorkId(workId, "CANCELLED")
    }

    override suspend fun markCompleted(workId: String) {
        reminderDao.updateStatusByWorkId(workId, "COMPLETED")
    }
}
