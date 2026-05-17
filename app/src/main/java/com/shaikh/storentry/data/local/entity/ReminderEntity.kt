package com.shaikh.storentry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: String,
    val productName: String,
    val scheduledTime: Long,
    val status: String, // SCHEDULED, COMPLETED, CANCELLED
    val workId: String
)
