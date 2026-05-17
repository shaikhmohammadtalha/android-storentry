package com.shaikh.storentry.presentation.screens.notifications

import com.shaikh.storentry.data.local.entity.ReminderEntity

data class NotificationsUiState(
    val upcoming: List<ReminderEntity> = emptyList(),
    val history: List<ReminderEntity> = emptyList(),
    val isLoading: Boolean = false
)
